import { IsString, IsNotEmpty, IsOptional, IsUUID } from 'class-validator';

export class SendMessageDto {
  @IsOptional()
  @IsUUID('4', { message: 'Invalid session ID' })
  sessionId?: string;

  @IsString()
  @IsNotEmpty({ message: 'Message content cannot be empty' })
  content!: string;
}
