package com.example.aishare.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA 加密解密工具类
 * 用于前后端密码传输加密
 */
@Slf4j
@Component
public class RsaUtil {

    private static final String RSA_ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;

    // 密钥对（启动时生成）
    private KeyPair keyPair;

    public RsaUtil() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            keyPairGenerator.initialize(KEY_SIZE);
            this.keyPair = keyPairGenerator.generateKeyPair();
            log.info("RSA 密钥对生成成功");
        } catch (NoSuchAlgorithmException e) {
            log.error("RSA 密钥对生成失败", e);
            throw new RuntimeException("RSA 密钥对生成失败", e);
        }
    }

    /**
     * 获取公钥（Base64 编码）
     */
    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }

    /**
     * 获取私钥（Base64 编码）
     */
    public String getPrivateKeyBase64() {
        return Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
    }

    /**
     * 使用公钥加密
     */
    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("RSA 加密失败", e);
            throw new RuntimeException("RSA 加密失败", e);
        }
    }

    /**
     * 使用私钥解密
     */
    public String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("RSA 解密失败", e);
            throw new RuntimeException("RSA 解密失败", e);
        }
    }

    /**
     * 使用指定公钥加密（用于测试）
     */
    public static String encryptWithPublicKey(String data, String publicKeyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("RSA 加密失败", e);
            throw new RuntimeException("RSA 加密失败", e);
        }
    }
}