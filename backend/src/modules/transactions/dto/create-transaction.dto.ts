import { IsString, IsNotEmpty, IsNumber, IsEnum, IsOptional, IsArray, IsUUID } from 'class-validator';
import { TransactionType } from '@prisma/client';

export class CreateTransactionDto {
  @IsUUID('4', { message: 'Invalid category ID' })
  @IsNotEmpty()
  categoryId!: string;

  @IsNumber({}, { message: 'Amount must be a number' })
  @IsNotEmpty()
  amount!: number;

  @IsEnum(TransactionType, { message: 'Type must be either INCOME or EXPENSE' })
  @IsNotEmpty()
  type!: TransactionType;

  @IsOptional()
  @IsString()
  description?: string;

  @IsString()
  @IsNotEmpty()
  transactionDate!: string; // ISO date string e.g. "2026-05-25"

  @IsOptional()
  @IsArray()
  @IsString({ each: true })
  tags?: string[];
}
