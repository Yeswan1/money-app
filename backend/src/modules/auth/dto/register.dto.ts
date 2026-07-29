import { IsString, MinLength, IsOptional, IsEnum } from 'class-validator';
import { Role } from '@prisma/client';

export class RegisterDto {
  @IsString()
  email!: string;

  @IsString()
  @MinLength(4, { message: 'Password must be at least 4 characters long' })
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
