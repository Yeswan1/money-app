import { PrismaClient } from '@prisma/client';
import * as argon2 from 'argon2';

const prisma = new PrismaClient();

async function main() {
  const user = await prisma.user.findFirst({
    where: { email: { in: ['student@gmail.com', 'student'] } }
  });

  if (!user) {
    console.error('Could not find student user in database');
    return;
  }

  const passwordHash = await argon2.hash('password');
  await prisma.user.update({
    where: { id: user.id },
    data: { passwordHash }
  });

  console.log(`Successfully reset password for ${user.email} to "password"`);
}

main()
  .catch(console.error)
  .finally(() => prisma.$disconnect());
