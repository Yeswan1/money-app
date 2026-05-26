import { Controller, Get, Post, Patch, Delete, Param, Body, UseGuards, ParseUUIDPipe } from '@nestjs/common';
import { JwtAuthGuard } from '@/common/guards/jwt-auth.guard';
import { CurrentUser } from '@/common/decorators/current-user.decorator';
import { SubscriptionsService } from './subscriptions.service';
import { CreateSubscriptionDto } from './dto/create-subscription.dto';
import { UpdateSubscriptionDto } from './dto/update-subscription.dto';

@Controller('subscriptions')
@UseGuards(JwtAuthGuard)
export class SubscriptionsController {
  constructor(private subscriptionsService: SubscriptionsService) {}

  @Get()
  async findAll(@CurrentUser() user: any) {
    return this.subscriptionsService.findAll(user.id);
  }

  @Get(':id')
  async findOne(
    @CurrentUser() user: any,
    @Param('id', ParseUUIDPipe) id: string,
  ) {
    return this.subscriptionsService.findOne(user.id, id);
  }

  @Post()
  async create(
    @CurrentUser() user: any,
    @Body() dto: CreateSubscriptionDto,
  ) {
    return this.subscriptionsService.create(user.id, dto);
  }

  @Patch(':id')
  async update(
    @CurrentUser() user: any,
    @Param('id', ParseUUIDPipe) id: string,
    @Body() dto: UpdateSubscriptionDto,
  ) {
    return this.subscriptionsService.update(user.id, id, dto);
  }

  @Delete(':id')
  async remove(
    @CurrentUser() user: any,
    @Param('id', ParseUUIDPipe) id: string,
  ) {
    return this.subscriptionsService.remove(user.id, id);
  }
}
