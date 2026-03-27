import JSEncrypt from 'jsencrypt'
import { get } from '@/api/request'

let cachedPublicKey: string | null = null

/**
 * 获取 RSA 公钥
 */
export async function getPublicKey(): Promise<string> {
  if (cachedPublicKey) {
    return cachedPublicKey
  }

  const res = await get<{ publicKey: string }>('/auth/public-key')
  cachedPublicKey = res.data.publicKey
  return cachedPublicKey!
}

/**
 * 使用公钥加密数据
 */
export function encryptWithPublicKey(data: string, publicKey: string): string {
  const encrypt = new JSEncrypt()
  encrypt.setPublicKey(publicKey)
  const encrypted = encrypt.encrypt(data)
  if (!encrypted) {
    throw new Error('RSA 加密失败')
  }
  return encrypted
}

/**
 * 加密密码（自动获取公钥）
 */
export async function encryptPassword(password: string): Promise<string> {
  const publicKey = await getPublicKey()
  return encryptWithPublicKey(password, publicKey)
}