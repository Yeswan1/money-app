import { Injectable, ConflictException, UnauthorizedException, BadRequestException } from '@nestjs/common';
import { PrismaService } from '@/common/prisma/prisma.service';
import { JwtService } from '@nestjs/jwt';
import { ConfigService } from '@nestjs/config';
import * as argon2 from 'argon2';
import { RegisterDto } from './dto/register.dto';
import { LoginDto } from './dto/login.dto';

@Injectable()
export class AuthService {
  private jwtAccessSecret: string;
  private jwtAccessExp: string;
  private jwtRefreshSecret: string;
  private jwtRefreshExp: string;

  constructor(
    private prisma: PrismaService,
    private jwtService: JwtService,
    private configService: ConfigService,
  ) {
    this.jwtAccessSecret = this.configService.get<string>('JWT_ACCESS_SECRET') || 'moneymap-access-secret-change-in-production-minimum-32-characters';
    this.jwtAccessExp = this.configService.get<string>('JWT_ACCESS_EXPIRATION') || '15m';
    this.jwtRefreshSecret = this.configService.get<string>('JWT_REFRESH_SECRET') || 'moneymap-refresh-secret-change-in-production-minimum-32-characters';
    this.jwtRefreshExp = this.configService.get<string>('JWT_REFRESH_EXPIRATION') || '7d';
  }

  async register(registerDto: RegisterDto) {
    const existing = await this.prisma.user.findUnique({
      where: { email: registerDto.email.toLowerCase() },
    });

    if (existing) {
      throw new ConflictException('A user with this email address already exists');
    }

    const passwordHash = await argon2.hash(registerDto.password);

    return await this.prisma.$transaction(async (tx) => {
      const user = await tx.user.create({
        data: {
          email: registerDto.email.toLowerCase(),
          passwordHash,
          name: registerDto.name,
          role: registerDto.role || 'PERSONAL',
          currency: registerDto.currency || 'USD',
          emailVerified: false,
          isActive: true,
        },
      });

      // Create initial User Profile
      await tx.userProfile.create({
        data: {
          userId: user.id,
          onboardingCompleted: false,
          notificationsEnabled: false,
        },
      });

      // Create initial Notification Preferences
      await tx.notificationPreference.create({
        data: {
          userId: user.id,
          budgetAlerts: true,
          goalReminders: true,
          subscriptionReminders: true,
          weeklyReport: true,
          monthlyReport: true,
        },
      });

      // Return clean user object
      return {
        id: user.id,
        email: user.email,
        name: user.name,
        role: user.role,
        currency: user.currency,
        createdAt: user.createdAt,
      };
    });
  }

  async login(loginDto: LoginDto, ipAddress?: string, deviceInfo?: string) {
    const user = await this.prisma.user.findUnique({
      where: { email: loginDto.email.toLowerCase(), deletedAt: null },
    });

    if (!user || !user.isActive) {
      throw new UnauthorizedException('Invalid email or password credentials');
    }

    const match = await argon2.verify(user.passwordHash, loginDto.password);
    if (!match) {
      throw new UnauthorizedException('Invalid email or password credentials');
    }

    const tokens = await this.generateTokens(user.id, user.email);

    // Save refresh token hash to DB
    const tokenHash = await argon2.hash(tokens.refreshToken);
    const expiresAt = new Date();
    expiresAt.setDate(expiresAt.getDate() + 7); // 7 days

    await this.prisma.refreshToken.create({
      data: {
        userId: user.id,
        tokenHash,
        ipAddress,
        deviceInfo,
        expiresAt,
      },
    });

    return {
      user: {
        id: user.id,
        email: user.email,
        name: user.name,
        role: user.role,
        currency: user.currency,
      },
      ...tokens,
    };
  }

  async refresh(refreshToken: string, ipAddress?: string, deviceInfo?: string) {
    let payload: any;
    try {
      payload = this.jwtService.verify(refreshToken, {
        secret: this.jwtRefreshSecret,
      });
    } catch (e) {
      throw new UnauthorizedException('Invalid or expired refresh token');
    }

    const activeTokens = await this.prisma.refreshToken.findMany({
      where: {
        userId: payload.sub,
        isRevoked: false,
        expiresAt: { gt: new Date() },
      },
    });

    let verifiedTokenRecord = null;
    for (const record of activeTokens) {
      const match = await argon2.verify(record.tokenHash, refreshToken);
      if (match) {
        verifiedTokenRecord = record;
        break;
      }
    }

    if (!verifiedTokenRecord) {
      throw new UnauthorizedException('Invalid or revoked refresh token');
    }

    // Revoke old token record (implement token rotation)
    await this.prisma.refreshToken.update({
      where: { id: verifiedTokenRecord.id },
      data: { isRevoked: true },
    });

    const user = await this.prisma.user.findUnique({
      where: { id: payload.sub, deletedAt: null },
    });

    if (!user || !user.isActive) {
      throw new UnauthorizedException('User no longer exists or is disabled');
    }

    const tokens = await this.generateTokens(user.id, user.email);
    
    // Save new refresh token hash
    const tokenHash = await argon2.hash(tokens.refreshToken);
    const expiresAt = new Date();
    expiresAt.setDate(expiresAt.getDate() + 7);

    await this.prisma.refreshToken.create({
      data: {
        userId: user.id,
        tokenHash,
        ipAddress,
        deviceInfo,
        expiresAt,
      },
    });

    return tokens;
  }

  async logout(refreshToken: string) {
    if (!refreshToken) {
      throw new BadRequestException('Refresh token is required to log out');
    }

    let payload: any;
    try {
      payload = this.jwtService.verify(refreshToken, {
        secret: this.jwtRefreshSecret,
      });
    } catch (e) {
      // If token verification failed, we can't look it up, just return
      return { message: 'Logged out successfully' };
    }

    const activeTokens = await this.prisma.refreshToken.findMany({
      where: {
        userId: payload.sub,
        isRevoked: false,
      },
    });

    for (const record of activeTokens) {
      const match = await argon2.verify(record.tokenHash, refreshToken);
      if (match) {
        await this.prisma.refreshToken.update({
          where: { id: record.id },
          data: { isRevoked: true },
        });
        break;
      }
    }

    return { message: 'Logged out successfully' };
  }

  private async generateTokens(userId: string, email: string) {
    const payload = { sub: userId, email };

    const accessToken = this.jwtService.sign(payload, {
      secret: this.jwtAccessSecret,
      expiresIn: this.jwtAccessExp as any,
    });

    const refreshToken = this.jwtService.sign(payload, {
      secret: this.jwtRefreshSecret,
      expiresIn: this.jwtRefreshExp as any,
    });

    return {
      accessToken,
      refreshToken,
    };
  }
}
