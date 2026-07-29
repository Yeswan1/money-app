import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';

interface OtpData {
  otp: string;
  expiresAt: number;
}

@Injectable()
export class OtpService {
  private readonly logger = new Logger(OtpService.name);
  private otps = new Map<string, OtpData>();

  constructor(private configService: ConfigService) {}

  async generateOtp(email: string): Promise<string> {
    // Generate a secure 6-digit OTP
    const otp = Math.floor(100000 + Math.random() * 900000).toString();
    const expiresAt = Date.now() + 5 * 60 * 1000; // 5 minutes expiration
    this.otps.set(email.toLowerCase(), { otp, expiresAt });

    const nodeEnv = this.configService.get<string>('NODE_ENV') || 'development';
    if (nodeEnv === 'development') {
      console.log(
        `Forgot Password OTP\nEmail: ${email}\nOTP: ${otp}`
      );
    }

    return otp;
  }

  async verifyOtp(email: string, otp: string): Promise<boolean> {
    const data = this.otps.get(email.toLowerCase());
    if (!data) return false;
    if (Date.now() > data.expiresAt) {
      this.otps.delete(email.toLowerCase());
      return false;
    }
    const isValid = data.otp === otp;
    if (isValid) {
      this.otps.delete(email.toLowerCase()); // Burn OTP on successful verification
    }
    return isValid;
  }
}
