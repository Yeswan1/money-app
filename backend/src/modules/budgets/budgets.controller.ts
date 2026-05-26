import { Controller, Get, Post, Body, Query, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '@/common/guards/jwt-auth.guard';
import { CurrentUser } from '@/common/decorators/current-user.decorator';
import { BudgetsService } from './budgets.service';
import { SetBudgetDto } from './dto/set-budget.dto';

@Controller('budgets')
@UseGuards(JwtAuthGuard)
export class BudgetsController {
  constructor(private budgetsService: BudgetsService) {}

  @Get()
  async findAll(
    @CurrentUser() user: any,
    @Query('month') month: number,
    @Query('year') year: number,
  ) {
    const activeMonth = month ? Number(month) : new Date().getMonth() + 1;
    const activeYear = year ? Number(year) : new Date().getFullYear();
    return this.budgetsService.findAll(user.id, activeMonth, activeYear);
  }

  @Post()
  async setBudget(
    @CurrentUser() user: any,
    @Body() dto: SetBudgetDto,
  ) {
    return this.budgetsService.setBudget(user.id, dto);
  }

  @Get('summary')
  async getSummary(
    @CurrentUser() user: any,
    @Query('month') month: number,
    @Query('year') year: number,
  ) {
    const activeMonth = month ? Number(month) : new Date().getMonth() + 1;
    const activeYear = year ? Number(year) : new Date().getFullYear();
    return this.budgetsService.getSummary(user.id, activeMonth, activeYear);
  }
}
