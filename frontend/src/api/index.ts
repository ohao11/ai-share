import { get, post, put, del, getPage } from './request'
import type { LoginResponse, User, Article, Category, Tag, Comment } from '@/types'

/**
 * 认证相关 API
 */
export function loginApi(email: string, password: string) {
  return post<LoginResponse>('/auth/login', { email, password })
}

export function registerApi(username: string, email: string, password: string) {
  return post<LoginResponse>('/auth/register', { username, email, password })
}

export function getCurrentUserApi() {
  return get<User>('/auth/me')
}

export function logoutApi() {
  return post('/auth/logout')
}

/**
 * 文章相关 API
 */
export function getArticlesApi(page: number, size: number, categoryId?: number, keyword?: string) {
  return getPage<Article>('/articles', { page, size, categoryId, keyword })
}

export function getArticlesByAuthorApi(authorId: number, page: number, size: number) {
  return getPage<Article>(`/articles/author/${authorId}`, { page, size })
}

export function getArticleApi(id: number) {
  return get<Article>(`/articles/${id}`)
}

export function createArticleApi(data: Partial<Article>) {
  return post<Article>('/articles', data)
}

export function updateArticleApi(id: number, data: Partial<Article>) {
  return put<Article>(`/articles/${id}`, data)
}

export function deleteArticleApi(id: number) {
  return del(`/articles/${id}`)
}

export function publishArticleApi(id: number) {
  return post(`/articles/${id}/publish`)
}

export function likeArticleApi(id: number) {
  return post<{ liked: boolean }>(`/articles/${id}/like`)
}

export function checkLikedApi(id: number) {
  return get<{ liked: boolean }>(`/articles/${id}/liked`)
}

/**
 * 分类相关 API
 */
export function getCategoriesApi() {
  return get<Category[]>('/categories')
}

export function getCategoryBySlugApi(slug: string) {
  return get<Category>(`/categories/${slug}`)
}

/**
 * 标签相关 API
 */
export function getTagsApi() {
  return get<Tag[]>('/tags')
}

export function getHotTagsApi(limit: number) {
  return get<Tag[]>('/tags/hot', { limit })
}

/**
 * 评论相关 API
 */
export function getCommentsApi(articleId: number) {
  return get<Comment[]>(`/articles/${articleId}/comments`)
}

export function createCommentApi(articleId: number, content: string, parentId?: number) {
  return post<Comment>(`/articles/${articleId}/comments`, { content, parentId })
}

/**
 * 上传相关 API
 */
export function getPresignedUrlApi(fileName: string, fileSize: number, mimeType: string, folder?: string) {
  return post<string>('/upload/presigned-url', { fileName, fileSize, mimeType, folder })
}

export function confirmUploadApi(fileName: string, fileSize: number, mimeType: string) {
  return post('/upload/confirm', { fileName, fileSize, mimeType })
}

export function deleteFileApi(fileId: number) {
  return del(`/upload/${fileId}`)
}

/**
 * 管理后台相关 API
 */
export function getStatsApi() {
  return get<Record<string, unknown>>('/admin/stats')
}

export function getAdminUsersApi(page: number, size: number, keyword?: string) {
  return getPage<User>('/admin/users', { page, size, keyword })
}

export function updateUserStatusApi(userId: number, status: number) {
  return put<void>(`/admin/users/${userId}/status?status=${status}`)
}

export function deleteUserApi(userId: number) {
  return del(`/admin/users/${userId}`)
}

export function getAdminArticlesApi(page: number, size: number, categoryId?: number, keyword?: string, status?: number) {
  return getPage<Article>('/admin/articles', { page, size, categoryId, keyword, status })
}

export function removeArticleApi(id: number) {
  return post(`/admin/articles/${id}/remove`)
}
