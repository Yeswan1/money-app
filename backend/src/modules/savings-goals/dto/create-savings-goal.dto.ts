import { IsString, IsNotEmpty, IsNumber, Min, IsOptional, IsHexColor } from 'class-validator';

export class CreateSavingsGoalDto {
  @IsString()
  @IsNotEmpty()
  name!: string;

  @IsNumber({}, { message: 'Target amount must be a number' })
  @Min(0.01)
  @IsNotEmpty()
  targetAmount!: number;

  @IsOptional()
  @IsNumber()
  @Min(0)
  currentAmount?: number;

  @IsOptional()
  @IsString()
  targetDate?: string; // ISO date string

  @IsOptional()
  @IsString()
  icon?: string;

  @IsOptional()
  @IsString()
  color?: string;
}
