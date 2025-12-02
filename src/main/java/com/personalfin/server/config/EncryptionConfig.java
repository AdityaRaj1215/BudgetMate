package com.personalfin.server.config;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Configuration for encryption utilities.
 * Note: This is a basic implementation. For production, consider using
 * more robust encryption libraries and key management systems.
 */
public class EncryptionConfig {

    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;

    /**
     * Generates a new AES secret key.
     * This should be done once and stored securely.
     *
     * @return Base64 encoded secret key
     * @throws NoSuchAlgorithmException if AES algorithm is not available
     */
    public static String generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(KEY_SIZE);
        SecretKey secretKey = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    /**
     * Encrypts a string value using AES encryption.
     *
     * @param plainText The text to encrypt
     * @param secretKey Base64 encoded secret key
     * @return Base64 encoded encrypted text
     * @throws Exception if encryption fails
     */
    public static String encrypt(String plainText, String secretKey) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(
                Base64.getDecoder().decode(secretKey), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Decrypts an encrypted string value using AES decryption.
     *
     * @param encryptedText Base64 encoded encrypted text
     * @param secretKey Base64 encoded secret key
     * @return Decrypted plain text
     * @throws Exception if decryption fails
     */
    public static String decrypt(String encryptedText, String secretKey) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(
                Base64.getDecoder().decode(secretKey), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}






