import { Controller, Get, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '@/common/guards/jwt-auth.guard';
import { CurrentUser } from '@/common/decorators/current-user.decorator';
import { ReportsService } from './reports.service';

@Controller('reports')
@UseGuards(JwtAuthGuard)
export class ReportsController {
  constructor(private reportsService: ReportsService) {}

  @Get('dashboard')
  async getDashboardStats(@CurrentUser() user: any) {
    return this.reportsService.getDashboardStats(user.id);
  }

  @Get('weekly')
  async getWeeklyReport(@CurrentUser() user: any) {
    return this.reportsService.getWeeklyReport(user.id);
  }

  @Get('monthly')
  async getMonthlyReport(@CurrentUser() user: any) {
    return this.reportsService.getMonthlyReport(user.id);
  }

  @Get('trends')
  async getSpendingTrends(@CurrentUser() user: any) {
    return this.reportsService.getSpendingTrends(user.id);
  }
}
