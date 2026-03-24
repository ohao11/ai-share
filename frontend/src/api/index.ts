import { get, post, put, del } from './request'
import type { LoginResponse, User, Article, Category, Tag, Comment, PageResult } from '@/types'

/**
 * 认证相关 API
 */
export function loginApi(email: string, password: string) {
  return post<LoginResponse>('/api/auth/login', { email, password })
}

export function registerApi(username: string, email: string, password: string) {
  return post<LoginResponse>('/api/auth/register', { username, email, password })
}

export function getCurrentUserApi() {
  return get<User>('/api/auth/me')
}

export function logoutApi() {
  return post('/api/auth/logout')
}

/**
 * 文章相关 API
 */
export function getArticlesApi(page: number, size: number, categoryId?: number, keyword?: string) {
  return get<PageResult<Article>>('/api/articles', { page, size, categoryId, keyword })
}

export function getArticleApi(id: number) {
  return get<Article>(`/api/articles/${id}`)
}

export function createArticleApi(data: Partial<Article>) {
  return post<Article>('/api/articles', data)
}

export function updateArticleApi(id: number, data: Partial<Article>) {
  return put<Article>(`/api/articles/${id}`, data)
}

export function deleteArticleApi(id: number) {
  return del(`/api/articles/${id}`)
}

export function publishArticleApi(id: number) {
  return post(`/api/articles/${id}/publish`)
}

export function likeArticleApi(id: number) {
  return post(`/api/articles/${id}/like`)
}

/**
 * 分类相关 API
 */
export function getCategoriesApi() {
  return get<Category[]>('/api/categories')
}

export function getCategoryBySlugApi(slug: string) {
  return get<Category>(`/api/categories/${slug}`)
}

/**
 * 标签相关 API
 */
export function getTagsApi() {
  return get<Tag[]>('/api/tags')
}

export function getHotTagsApi(limit: number) {
  return get<Tag[]>('/api/tags/hot', { limit })
}

/**
 * 评论相关 API
 */
export function getCommentsApi(articleId: number) {
  return get<Comment[]>(`/api/articles/${articleId}/comments`)
}

export function createCommentApi(articleId: number, content: string, parentId?: number) {
  return post<Comment>(`/api/articles/${articleId}/comments`, { content, parentId })
}

/**
 * 上传相关 API
 */
export function getPresignedUrlApi(fileName: string, fileSize: number, mimeType: string, folder?: string) {
  return post<string>('/api/upload/presigned-url', { fileName, fileSize, mimeType, folder })
}

export function confirmUploadApi(fileName: string, fileSize: number, mimeType: string) {
  return post('/api/upload/confirm', { fileName, fileSize, mimeType })
}

export function deleteFileApi(fileId: number) {
  return del(`/api/upload/${fileId}`)
}
