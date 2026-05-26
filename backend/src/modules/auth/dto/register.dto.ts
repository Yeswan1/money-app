import { IsEmail, IsString, MinLength, IsOptional, IsEnum } from 'class-validator';
import { Role } from '@prisma/client';

export class RegisterDto {
  @IsEmail({}, { message: 'Please provide a valid email address' })
  email!: string;

  @IsString()
  @MinLength(6, { message: 'Password must be at least 6 characters long' })
  password!: string;

  @IsString()
  name!: string;

  @IsOptional()
  @IsEnum(Role, { message: 'Invalid user role' })
  role?: Role;

  @IsOptional()
  @IsString()
  currency?: string;
}
