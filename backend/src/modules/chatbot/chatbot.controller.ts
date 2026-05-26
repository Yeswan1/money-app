import { Controller, Get, Post, Param, Body, UseGuards, ParseUUIDPipe } from '@nestjs/common';
import { JwtAuthGuard } from '@/common/guards/jwt-auth.guard';
import { CurrentUser } from '@/common/decorators/current-user.decorator';
import { ChatbotService } from './chatbot.service';
import { SendMessageDto } from './dto/send-message.dto';

@Controller('chatbot')
@UseGuards(JwtAuthGuard)
export class ChatbotController {
  constructor(private chatbotService: ChatbotService) {}

  @Get('sessions')
  async getSessions(@CurrentUser() user: any) {
    return this.chatbotService.getSessions(user.id);
  }

  @Get('sessions/:id/messages')
  async getMessages(
    @CurrentUser() user: any,
    @Param('id', ParseUUIDPipe) id: string,
  ) {
    return this.chatbotService.getMessages(user.id, id);
  }

  @Post('message')
  async sendMessage(
    @CurrentUser() user: any,
    @Body() dto: SendMessageDto,
  ) {
    return this.chatbotService.sendMessage(user.id, dto.sessionId, dto.content);
  }
}
