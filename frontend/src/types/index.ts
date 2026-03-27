export interface User {
  id: number
  uuid: string
  username: string
  email: string
  avatar?: string
  role: number
  status?: number
  provider: string
  createdAt: string
}

export interface LoginResponse {
  accessToken: string
  tokenType: string
  user: User
}

export interface Article {
  id: number
  title: string
  slug?: string
  summary?: string
  content: string
  coverImage?: string
  authorId?: number
  authorName?: string
  categoryId?: number
  categoryName?: string
  status: number
  viewCount?: number
  likeCount?: number
  commentCount?: number
  tagIds?: number[]
  tags?: Tag[]
  publishedAt?: string
  createdAt?: string
  updatedAt?: string
}

export interface Category {
  id: number
  name: string
  slug: string
  description?: string
  parentId?: number
  sortOrder: number
  articleCount?: number
  createdAt: string
}

export interface Tag {
  id: number
  name: string
  slug: string
  createdAt: string
}

export interface Comment {
  id: number
  content: string
  articleId: number
  userId: number
  parentId?: number
  status: number
  createdAt: string
  updatedAt: string
}

export interface PageResult<T> {
  code: number
  message: string
  data: T[]
  total: number
  current: number
  size: number
  pages: number
  timestamp: number
}

export interface PresignedUrlResponse {
  presignedUrl: string
  objectName: string
}

export interface UploadResponse {
  fileId: string
  fileName: string
  fileUrl: string
  fileSize: number
  mimeType: string
}
