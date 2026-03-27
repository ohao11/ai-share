# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

### Docker Deployment
```bash
docker compose up -d --build    # Build and start all services
docker compose logs -f          # View logs
docker compose down             # Stop services
docker compose ps               # Check service status
```

### Backend (Spring Boot 4 + JDK 21)
```bash
cd backend
mvn spring-boot:run             # Run locally
mvn clean package -DskipTests   # Build JAR
```

### Frontend (Vue 3 + Vite)
```bash
cd frontend
npm install                     # Install dependencies
npm run dev                     # Start dev server
npm run build                   # Production build
npm run lint                    # Run ESLint
```

## Architecture Overview

**Full-stack content sharing platform** (Spring Boot 4 + Vue 3)

### Tech Stack
- **Backend**: Spring Boot 4.0.4, JDK 21, MyBatis-Plus 3.5.16, Spring Security 7, PostgreSQL 16, Redis 7, MinIO
- **Frontend**: Vue 3, TypeScript, Vite 5, Element Plus, Pinia, Vue Router

### Project Structure
```
ai-share/
├── backend/           # Spring Boot API server (port 8080)
├── frontend/          # Vue 3 SPA (served via Nginx on port 80)
├── docs/              # API.md, DEPLOYMENT.md, DESIGN.md
├── docker-compose.yml # All services: postgres, redis, minio, backend, frontend
└── .env               # Environment config (JWT_SECRET, OAuth2 credentials)
```

### Backend Modules
- **controller**: AuthController, ArticleController, CategoryController, TagController, CommentController, UploadController, AdminController
- **service/impl**: Service implementations with business logic
- **mapper**: MyBatis-Plus mappers (UserMapper, ArticleMapper, etc.)
- **entity**: JPA entities (User, Article, Category, Tag, Comment, ArticleLike, ArticleTag, File)
- **dto/request|response**: Data transfer objects
- **config**: SecurityConfig, MinioConfig, RedisConfig, MybatisPlusConfig
- **security**: JwtTokenProvider, JwtAuthenticationFilter, OAuth2AuthenticationSuccessHandler
- **common/result**: Unified response format (Result, PageResult)
- **common/exception**: GlobalExceptionHandler, BusinessException

### Key Features
- **Auth**: JWT tokens, Google/GitHub OAuth2 login
- **Articles**: CRUD, publish, like, category/tag association
- **Comments**: Nested comments with parent_id
- **Upload**: MinIO presigned URLs for file storage
- **Admin**: User management, content moderation, stats dashboard

### API Conventions
- Base URL: `/api`
- Response format: `{ code, message, data, timestamp }`
- Auth: `Authorization: Bearer <jwt-token>`
- Error codes: 200=success, 400=bad request, 401=unauthorized, 403=forbidden, 404=not found, 500=server error

### Environment Variables (required in .env)
- `JWT_SECRET` - JWT signing key (must customize)
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` - Google OAuth2
- `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET` - GitHub OAuth2
