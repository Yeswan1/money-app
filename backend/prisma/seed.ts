import { PrismaClient, Role } from '@prisma/client';

const prisma = new PrismaClient();

const SYSTEM_CATEGORIES = [
  { name: 'Food', color: '#FF7A00', icon: 'restaurant' },
  { name: 'Transport', color: '#3B82F6', icon: 'directions_car' },
  { name: 'Shopping', color: '#EC4899', icon: 'shopping_bag' },
  { name: 'Bills', color: '#EAB308', icon: 'flash_on' },
  { name: 'Entertainment', color: '#A855F7', icon: 'movie' },
  { name: 'Health', color: '#EF4444', icon: 'medical_services' },
  { name: 'Education', color: '#6366F1', icon: 'school' },
  { name: 'Groceries', color: '#22C55E', icon: 'local_grocery_store' },
  { name: 'Utilities', color: '#14B8A6', icon: 'bolt' },
  { name: 'Healthcare', color: '#F43F5E', icon: 'health_and_safety' },
  { name: 'Income', color: '#10B981', icon: 'account_balance' },
  { name: 'Other', color: '#64748B', icon: 'more_horiz' },
];

async function main() {
  console.log('🌱 Seeding database...');

  // Create system categories
  for (const cat of SYSTEM_CATEGORIES) {
    await prisma.category.upsert({
      where: {
        name_userId: {
          name: cat.name,
          userId: '00000000-0000-0000-0000-000000000000', // system sentinel — won't match any real user
        },
      },
      update: {},
      create: {
        name: cat.name,
        color: cat.color,
        icon: cat.icon,
        isSystem: true,
        userId: null,
      },
    });
  }

  console.log(`✅ Created ${SYSTEM_CATEGORIES.length} system categories`);
  console.log('🌱 Seeding complete!');
}

main()
  .catch((e) => {
    console.error('❌ Seed error:', e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
