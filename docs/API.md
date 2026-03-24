# ai-share API 接口文档

## 基础信息

- **Base URL**: `http://localhost:8080/api`
- **认证方式**: JWT Bearer Token
- **数据格式**: JSON

## 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1234567890
}
```

## 认证模块

### 用户登录

**接口**: `POST /api/auth/login`

**请求体**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "uuid": "xxx",
      "username": "user",
      "email": "user@example.com",
      "avatar": null,
      "role": 1,
      "provider": "local",
      "createdAt": "2026-03-24T10:00:00Z"
    }
  }
}
```

### 用户注册

**接口**: `POST /api/auth/register`

**请求体**:
```json
{
  "username": "newuser",
  "email": "user@example.com",
  "password": "password123"
}
```

### 获取当前用户

**接口**: `GET /api/auth/me`

**请求头**:
```
Authorization: Bearer <token>
```

### Google OAuth2 登录

**接口**: `GET /api/auth/google`

**说明**: 浏览器直接访问，跳转到 Google 授权页面

### GitHub OAuth2 登录

**接口**: `GET /api/auth/github`

**说明**: 浏览器直接访问，跳转到 GitHub 授权页面

---

## 文章模块

### 获取文章列表

**接口**: `GET /api/articles`

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页数量，默认 10 |
| categoryId | long | 否 | 分类 ID |
| keyword | string | 否 | 搜索关键词 |

**响应**:
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "title": "文章标题",
        "slug": "article-slug",
        "summary": "文章摘要",
        "content": "文章内容",
        "coverImage": "http://...",
        "authorId": 1,
        "categoryId": 1,
        "status": 1,
        "viewCount": 100,
        "likeCount": 50,
        "commentCount": 10,
        "publishedAt": "2026-03-24T10:00:00Z",
        "createdAt": "2026-03-24T10:00:00Z",
        "updatedAt": "2026-03-24T10:00:00Z"
      }
    ],
    "total": 100,
    "current": 1,
    "size": 10,
    "pages": 10
  }
}
```

### 获取文章详情

**接口**: `GET /api/articles/{id}`

**响应**: 返回单篇文章详情

### 创建文章

**接口**: `POST /api/articles`

**请求头**:
```
Authorization: Bearer <token>
```

**请求体**:
```json
{
  "title": "文章标题",
  "slug": "article-slug",
  "summary": "文章摘要",
  "content": "文章内容 (Markdown)",
  "coverImage": "http://...",
  "categoryId": 1,
  "status": 0
}
```

### 更新文章

**接口**: `PUT /api/articles/{id}`

**请求体**: 同创建文章

### 删除文章

**接口**: `DELETE /api/articles/{id}`

### 发布文章

**接口**: `POST /api/articles/{id}/publish`

### 点赞文章

**接口**: `POST /api/articles/{id}/like`

---

## 分类模块

### 获取分类列表

**接口**: `GET /api/categories`

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "技术专栏",
      "slug": "tech",
      "description": "技术相关文章",
      "parentId": null,
      "sortOrder": 1
    }
  ]
}
```

### 根据 Slug 获取分类

**接口**: `GET /api/categories/{slug}`

---

## 标签模块

### 获取标签列表

**接口**: `GET /api/tags`

### 获取热门标签

**接口**: `GET /api/tags/hot`

**查询参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| limit | int | 否 | 数量限制，默认 10 |

### 根据 Slug 获取标签

**接口**: `GET /api/tags/{slug}`

---

## 评论模块

### 获取文章评论

**接口**: `GET /api/articles/{articleId}/comments`

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "content": "评论内容",
      "articleId": 1,
      "userId": 1,
      "parentId": null,
      "status": 1,
      "createdAt": "2026-03-24T10:00:00Z"
    }
  ]
}
```

### 发表评论

**接口**: `POST /api/articles/{articleId}/comments`

**请求头**:
```
Authorization: Bearer <token>
```

**请求体**:
```json
{
  "content": "评论内容",
  "parentId": null
}
```

---

## 文件上传模块

### 获取预签名上传 URL

**接口**: `POST /api/upload/presigned-url`

**请求头**:
```
Authorization: Bearer <token>
```

**请求体**:
```json
{
  "fileName": "image.png",
  "fileSize": 102400,
  "mimeType": "image/png",
  "folder": "articles"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "获取上传 URL 成功",
  "data": "http://minio:9000/ai-share/xxx?X-Amz-Algorithm=..."
}
```

**说明**: 获取 URL 后，使用 PUT 方法直接上传文件到 MinIO

### 确认上传完成

**接口**: `POST /api/upload/confirm`

**请求体**:
```json
{
  "fileName": "image.png",
  "fileSize": 102400,
  "mimeType": "image/png"
}
```

### 删除文件

**接口**: `DELETE /api/upload/{fileId}`

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权，需要登录 |
| 403 | 禁止访问，权限不足 |
| 404 | 资源不存在 |
| 405 | 请求方法不支持 |
| 500 | 服务器内部错误 |

## 认证说明

需要认证的接口，请在请求头中携带 JWT Token：

```
Authorization: Bearer <your-jwt-token>
```

Token 过期或无效时，接口将返回 401 错误。
