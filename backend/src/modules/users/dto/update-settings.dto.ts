import { IsString, IsOptional, IsBoolean, IsEnum } from 'class-validator';
import { Role } from '@prisma/client';

export class UpdateSettingsDto {
  @IsOptional()
  @IsString()
  name?: string;

  @IsOptional()
  @IsString()
  currency?: string;

  @IsOptional()
  @IsEnum(Role, { message: 'Invalid user role' })
  role?: Role;

  @IsOptional()
  @IsBoolean()
  notificationsEnabled?: boolean;

  // Preferences sub-fields
  @IsOptional()
  @IsBoolean()
  budgetAlerts?: boolean;

  @IsOptional()
  @IsBoolean()
  goalReminders?: boolean;

  @IsOptional()
  @IsBoolean()
  subscriptionReminders?: boolean;

  @IsOptional()
  @IsBoolean()
  weeklyReport?: boolean;

  @IsOptional()
  @IsBoolean()
  monthlyReport?: boolean;
}
