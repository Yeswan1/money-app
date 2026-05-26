import { IsString, IsNotEmpty, IsNumber, Min, IsEnum, IsOptional, IsBoolean } from 'class-validator';
import { BillingCycle } from '@prisma/client';

export class CreateSubscriptionDto {
  @IsString()
  @IsNotEmpty()
  name!: string;

  @IsNumber({}, { message: 'Amount must be a number' })
  @Min(0.01)
  @IsNotEmpty()
  amount!: number;

  @IsEnum(BillingCycle, { message: 'Billing cycle must be WEEKLY, MONTHLY, QUARTERLY or YEARLY' })
  @IsNotEmpty()
  billingCycle!: BillingCycle;

  @IsString()
  @IsNotEmpty()
  nextBillingDate!: string; // ISO date format e.g. "2026-06-01"

  @IsOptional()
  @IsString()
  color?: string;

  @IsOptional()
  @IsBoolean()
  isActive?: boolean;
}
