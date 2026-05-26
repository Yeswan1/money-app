import { IsUUID, IsNotEmpty, IsNumber, Min, Max } from 'class-validator';

export class SetBudgetDto {
  @IsUUID('4', { message: 'Invalid category ID' })
  @IsNotEmpty()
  categoryId!: string;

  @IsNumber({}, { message: 'Amount must be a number' })
  @Min(0, { message: 'Amount cannot be negative' })
  @IsNotEmpty()
  amount!: number;

  @IsNumber({}, { message: 'Month must be a number' })
  @Min(1)
  @Max(12)
  @IsNotEmpty()
  month!: number;

  @IsNumber({}, { message: 'Year must be a number' })
  @Min(2000)
  @Max(2100)
  @IsNotEmpty()
  year!: number;
}
