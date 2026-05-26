import { Injectable, ConflictException, NotFoundException, BadRequestException } from '@nestjs/common';
import { PrismaService } from '@/common/prisma/prisma.service';
import { CreateCategoryDto } from './dto/create-category.dto';

@Injectable()
export class CategoriesService {
  constructor(private prisma: PrismaService) {}

  async findAll(userId: string) {
    return this.prisma.category.findMany({
      where: {
        OR: [
          { isSystem: true },
          { userId },
        ],
      },
      orderBy: {
        name: 'asc',
      },
    });
  }

  async create(userId: string, dto: CreateCategoryDto) {
    const nameLower = dto.name.trim();

    // Check if category name already exists as system category or for this user
    const existing = await this.prisma.category.findFirst({
      where: {
        name: { equals: nameLower, mode: 'insensitive' },
        OR: [
          { isSystem: true },
          { userId },
        ],
      },
    });

    if (existing) {
      throw new ConflictException(`Category '${dto.name}' already exists.`);
    }

    return this.prisma.category.create({
      data: {
        name: dto.name,
        color: dto.color,
        icon: dto.icon,
        isSystem: false,
        userId,
      },
    });
  }

  async remove(userId: string, id: string) {
    const category = await this.prisma.category.findUnique({
      where: { id },
    });

    if (!category) {
      throw new NotFoundException('Category not found');
    }

    if (category.isSystem) {
      throw new BadRequestException('System categories cannot be deleted');
    }

    if (category.userId !== userId) {
      throw new BadRequestException('You do not have permission to delete this category');
    }

    return this.prisma.category.delete({
      where: { id },
    });
  }
}
