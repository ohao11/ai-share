export interface User {
  id: number
  uuid: string
  username: string
  email: string
  avatar?: string
  role: number
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
  authorId: number
  authorName?: string
  categoryId?: number
  categoryName?: string
  status: number
  viewCount: number
  likeCount: number
  commentCount: number
  publishedAt?: string
  createdAt: string
  updatedAt: string
}

export interface Category {
  id: number
  name: string
  slug: string
  description?: string
  parentId?: number
  sortOrder: number
}

export interface Tag {
  id: number
  name: string
  slug: string
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
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}
