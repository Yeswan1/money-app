import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '@/common/prisma/prisma.service';
import { UpdateProfileDto } from './dto/update-profile.dto';
import { UpdateSettingsDto } from './dto/update-settings.dto';

@Injectable()
export class UsersService {
  constructor(private prisma: PrismaService) {}

  async getProfile(userId: string) {
    const user = await this.prisma.user.findUnique({
      where: { id: userId, deletedAt: null },
      select: {
        id: true,
        email: true,
        name: true,
        role: true,
        currency: true,
        createdAt: true,
        profile: true,
        notificationPref: true,
      },
    });

    if (!user) {
      throw new NotFoundException('User profile not found');
    }

    return user;
  }

  async updateProfile(userId: string, dto: UpdateProfileDto) {
    // Ensure profile exists
    let profile = await this.prisma.userProfile.findUnique({
      where: { userId },
    });

    if (!profile) {
      profile = await this.prisma.userProfile.create({
        data: { userId },
      });
    }

    const {
      institution,
      yearOfStudy,
      monthlyAllowance,
      companyName,
      jobTitle,
      monthlyIncome,
      householdSize,
      monthlyBudget,
      primaryCategories,
      financialGoal,
      onboardingCompleted,
    } = dto;

    const updatedProfile = await this.prisma.userProfile.update({
      where: { userId },
      data: {
        institution: institution !== undefined ? institution : undefined,
        yearOfStudy: yearOfStudy !== undefined ? yearOfStudy : undefined,
        monthlyAllowance: monthlyAllowance !== undefined ? monthlyAllowance : undefined,
        companyName: companyName !== undefined ? companyName : undefined,
        jobTitle: jobTitle !== undefined ? jobTitle : undefined,
        monthlyIncome: monthlyIncome !== undefined ? monthlyIncome : undefined,
        householdSize: householdSize !== undefined ? householdSize : undefined,
        monthlyBudget: monthlyBudget !== undefined ? monthlyBudget : undefined,
        primaryCategories: primaryCategories !== undefined ? JSON.stringify(primaryCategories) : undefined,
        financialGoal: financialGoal !== undefined ? financialGoal : undefined,
        onboardingCompleted: onboardingCompleted !== undefined ? onboardingCompleted : undefined,
      },
    });

    return updatedProfile;
  }

  async updateSettings(userId: string, dto: UpdateSettingsDto) {
    const {
      name,
      currency,
      notificationsEnabled,
      budgetAlerts,
      goalReminders,
      subscriptionReminders,
      weeklyReport,
      monthlyReport,
    } = dto;

    return await this.prisma.$transaction(async (tx) => {
      // Update general user fields
      if (name !== undefined || currency !== undefined) {
        await tx.user.update({
          where: { id: userId },
          data: {
            name: name || undefined,
            currency: currency || undefined,
          },
        });
      }

      // Update notificationsEnabled in UserProfile
      if (notificationsEnabled !== undefined) {
        await tx.userProfile.upsert({
          where: { userId },
          update: { notificationsEnabled },
          create: { userId, notificationsEnabled },
        });
      }

      // Update specific preferences
      const preferencesData: any = {};
      if (budgetAlerts !== undefined) preferencesData.budgetAlerts = budgetAlerts;
      if (goalReminders !== undefined) preferencesData.goalReminders = goalReminders;
      if (subscriptionReminders !== undefined) preferencesData.subscriptionReminders = subscriptionReminders;
      if (weeklyReport !== undefined) preferencesData.weeklyReport = weeklyReport;
      if (monthlyReport !== undefined) preferencesData.monthlyReport = monthlyReport;

      if (Object.keys(preferencesData).length > 0) {
        await tx.notificationPreference.upsert({
          where: { userId },
          update: preferencesData,
          create: { userId, ...preferencesData },
        });
      }

      return this.getProfile(userId);
    });
  }
}
