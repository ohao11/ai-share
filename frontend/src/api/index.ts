import { get, post, put, del, getPage, upload } from './request'
import type { LoginResponse, User, Article, Category, Tag, Comment, PresignedUrlResponse, UploadResponse } from '@/types'
import { encryptPassword } from '@/utils/rsa'

/**
 * 认证相关 API
 */
export function getPublicKeyApi() {
  return get<{ publicKey: string }>('/auth/public-key')
}

export async function loginApi(email: string, password: string) {
  const encryptedPassword = await encryptPassword(password)
  return post<LoginResponse>('/auth/login', { email, password: encryptedPassword })
}

export async function registerApi(username: string, email: string, password: string) {
  const encryptedPassword = await encryptPassword(password)
  return post<LoginResponse>('/auth/register', { username, email, password: encryptedPassword })
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

export function getArticlesByTagApi(tagId: number, page: number, size: number) {
  return getPage<Article>(`/articles/tag/${tagId}`, { page, size })
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

export function unlikeArticleApi(id: number) {
  return del(`/articles/${id}/like`)
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

export function createCategoryApi(data: Partial<Category>) {
  return post<Category>('/categories', data)
}

export function updateCategoryApi(id: number, data: Partial<Category>) {
  return put<Category>(`/categories/${id}`, data)
}

export function deleteCategoryApi(id: number) {
  return del(`/categories/${id}`)
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

export function createTagApi(data: Partial<Tag>) {
  return post<Tag>('/tags', data)
}

export function updateTagApi(id: number, data: Partial<Tag>) {
  return put<Tag>(`/tags/${id}`, data)
}

export function deleteTagApi(id: number) {
  return del(`/tags/${id}`)
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

export function deleteCommentApi(articleId: number, commentId: number) {
  return del(`/articles/${articleId}/comments/${commentId}`)
}

/**
 * 上传相关 API
 */
export function uploadFileApi(file: File, folder: string = 'uploads') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('folder', folder)
  return upload<UploadResponse>('/upload', formData)
}

export function getPresignedUrlApi(fileName: string, fileSize: number, mimeType: string, folder?: string) {
  return post<PresignedUrlResponse>('/upload/presigned-url', { fileName, fileSize, mimeType, folder })
}

export function confirmUploadApi(fileName: string, fileSize: number, mimeType: string, objectName: string) {
  return post('/upload/confirm', { fileName, fileSize, mimeType, objectName })
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
