import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { ValidationPipe } from '@nestjs/common';
import { HttpExceptionFilter } from './common/filters/http-exception.filter';
import { TransformInterceptor } from './common/interceptors/transform.interceptor';
import helmet from 'helmet';
import * as compression from 'compression';
import * as cookieParser from 'cookie-parser';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  // 1. Set global route prefix
  const apiPrefix = process.env.API_PREFIX || 'api/v1';
  app.setGlobalPrefix(apiPrefix);

  // 2. Enable standard middle-wares for production security/efficiency
  app.use(helmet());
  app.use(compression());
  app.use(cookieParser());

  // 3. Configure CORS policy
  const corsOrigins = process.env.CORS_ORIGINS || '*';
  app.enableCors({
    origin: corsOrigins === '*' ? true : corsOrigins.split(','),
    credentials: true,
    methods: 'GET,HEAD,PUT,PATCH,POST,DELETE,OPTIONS',
    allowedHeaders: 'Content-Type,Accept,Authorization',
  });

  // 4. Global Validation, Exceptions, and Response wrappers
  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      transform: true,
      forbidNonWhitelisted: true,
      transformOptions: {
        enableImplicitConversion: true,
      },
    }),
  );

  app.useGlobalFilters(new HttpExceptionFilter());
  app.useGlobalInterceptors(new TransformInterceptor());

  // 5. Open API (Swagger) documentation setup
  const config = new DocumentBuilder()
    .setTitle('MoneyMap API')
    .setDescription('MoneyMap — Complete, high-performance financial management backend API endpoints')
    .setVersion('1.0.0')
    .addBearerAuth()
    .build();
  const document = SwaggerModule.createDocument(app, config);
  SwaggerModule.setup(`${apiPrefix}/docs`, app, document);

  // 6. Listen on designated port
  const port = process.env.PORT || 3000;
  await app.listen(port);
  console.log(`🚀 MoneyMap Backend starting on: http://localhost:${port}/${apiPrefix}`);
  console.log(`📚 Swagger documentation at: http://localhost:${port}/${apiPrefix}/docs`);
}
bootstrap();
