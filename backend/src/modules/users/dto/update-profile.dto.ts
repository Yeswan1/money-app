import { IsString, IsOptional, IsNumber, IsBoolean, IsArray } from 'class-validator';

export class UpdateProfileDto {
  // Student fields
  @IsOptional()
  @IsString()
  institution?: string;

  @IsOptional()
  @IsString()
  yearOfStudy?: string;

  @IsOptional()
  @IsNumber()
  monthlyAllowance?: number;

  // Professional fields
  @IsOptional()
  @IsString()
  companyName?: string;

  @IsOptional()
  @IsString()
  jobTitle?: string;

  @IsOptional()
  @IsNumber()
  monthlyIncome?: number;

  // Homemaker fields
  @IsOptional()
  @IsNumber()
  householdSize?: number;

  @IsOptional()
  @IsNumber()
  monthlyBudget?: number;

  @IsOptional()
  @IsArray()
  primaryCategories?: string[];

  // General fields
  @IsOptional()
  @IsString()
  financialGoal?: string;

  @IsOptional()
  @IsBoolean()
  onboardingCompleted?: boolean;
}
