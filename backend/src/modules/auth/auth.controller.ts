import { Controller, Post, Body, Req, UnauthorizedException, BadRequestException } from '@nestjs/common';
import { AuthService } from './auth.service';
import { RegisterDto } from './dto/register.dto';
import { LoginDto } from './dto/login.dto';
import { GoogleSignInDto } from './dto/google-signin.dto';
import { Request } from 'express';

@Controller('auth')
export class AuthController {
  constructor(private authService: AuthService) {}

  @Post('signup')
  async signup(@Body() registerDto: RegisterDto) {
    return this.authService.register(registerDto);
  }

  @Post('login')
  async login(@Body() loginDto: LoginDto, @Req() req: Request) {
    const ipAddress = req.ip || req.socket.remoteAddress;
    const deviceInfo = req.headers['user-agent'];
    return this.authService.login(loginDto, ipAddress, deviceInfo);
  }

  @Post('refresh')
  async refresh(@Body('refreshToken') refreshToken: string, @Req() req: Request) {
    if (!refreshToken) {
      throw new UnauthorizedException('Refresh token is required');
    }
    const ipAddress = req.ip || req.socket.remoteAddress;
    const deviceInfo = req.headers['user-agent'];
    return this.authService.refresh(refreshToken, ipAddress, deviceInfo);
  }

  @Post('logout')
  async logout(@Body('refreshToken') refreshToken: string) {
    return this.authService.logout(refreshToken);
  }

  @Post('google')
  async googleSignIn(@Body() dto: GoogleSignInDto, @Req() req: Request) {
    const ipAddress = req.ip || req.socket.remoteAddress;
    const deviceInfo = req.headers['user-agent'];
    return this.authService.googleSignIn(dto.idToken, ipAddress, deviceInfo);
  }

  @Post('forgot-password')
  async forgotPassword(@Body('email') email: string) {
    if (!email) {
      throw new BadRequestException('Email is required');
    }
    return this.authService.forgotPassword(email);
  }

  @Post('reset-password')
  async resetPassword(@Body() resetDto: any) {
    if (!resetDto.email || !resetDto.otp || !resetDto.newPassword) {
      throw new BadRequestException('Email, OTP, and newPassword are required');
    }
    return this.authService.resetPassword(resetDto);
  }
}
