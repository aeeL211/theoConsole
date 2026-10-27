package com.aeeL.theokntl.core

import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class CryptoEngine @Inject constructor() {
    private val random = SecureRandom()

    fun utf8(text: String): ByteArray = text.toByteArray(Charsets.UTF_8)

    fun md5(bytes: ByteArray): ByteArray = MessageDigest.getInstance("MD5").digest(bytes)

    fun md5Hex(text: String): String = toHex(md5(utf8(text)))

    fun keyFingerprint(encKey: String): String = md5Hex(encKey).take(8)

    fun fromHex(hex: String): ByteArray {
        val clean = hex.replace(Regex("\\s+"), "")
        require(clean.length % 2 == 0 &&
            clean.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) {
            "Invalid hex string"
        }
        return ByteArray(clean.length / 2) { index ->
            clean.substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }
    }

    fun toHex(bytes: ByteArray): String {
        val builder = StringBuilder(bytes.size * 2)
        for (byte in bytes) {
            val value = byte.toInt() and 0xff
            builder.append(HEX[value ushr 4])
            builder.append(HEX[value and 0x0f])
        }
        return builder.toString()
    }

    fun base64(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)

    fun decodeBase64(value: String): ByteArray =
        Base64.getDecoder().decode(value.replace(Regex("\\s+"), ""))

    fun aesCbcEncrypt(key: ByteArray, iv: ByteArray, plaintext: ByteArray): ByteArray {
        validateKey(key)
        require(iv.size == 16) { "AES IV must be 16 bytes" }
        return try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
            cipher.doFinal(plaintext)
        } catch (error: Exception) {
            throw ConsoleException(ErrorKind.PROTOCOL, "AES encryption failed: ${error.message}")
        }
    }

    fun aesCbcDecrypt(key: ByteArray, iv: ByteArray, ciphertext: ByteArray): ByteArray {
        validateKey(key)
        require(iv.size == 16) { "AES IV must be 16 bytes" }
        require(ciphertext.isNotEmpty() && ciphertext.size % 16 == 0) {
            "Ciphertext must be a non-empty multiple of 16 bytes"
        }
        return try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
            cipher.doFinal(ciphertext)
        } catch (error: Exception) {
            throw ConsoleException(ErrorKind.PROTOCOL, "AES decryption failed: ${error.message}")
        }
    }

    fun timingSafeEqual(left: ByteArray, right: ByteArray): Boolean =
        MessageDigest.isEqual(left, right)

    /**
     * Mirrors the forum login seal: MD5(username) masks the password bytes, then
     * RSA PKCS#1 v1.5 encryption with the client's public key.
     */
    fun sealForumPassword(username: String, password: String): String {
        val mask = md5(utf8(username))
        val raw = utf8(password)
        val mixed = ByteArray(raw.size) { index ->
            (raw[index].toInt() xor mask[index % mask.size].toInt()).toByte()
        }
        return base64(rsaPkcs1Encrypt(mixed))
    }

    private fun rsaPkcs1Encrypt(message: ByteArray): ByteArray {
        val modulus = BigInteger(1, fromHex(ConsoleConstants.FORUM_RSA_N_HEX))
        val keySize = (modulus.bitLength() + 7) / 8
        if (message.size > keySize - 11) {
            throw ConsoleException(ErrorKind.INPUT, "Forum password is too long for the RSA key.")
        }
        val paddingLength = keySize - message.size - 3
        val padding = ByteArray(paddingLength)
        val buffer = ByteArray(maxOf(32, paddingLength))
        var filled = 0
        while (filled < paddingLength) {
            random.nextBytes(buffer)
            for (byte in buffer) {
                if (byte != 0.toByte() && filled < paddingLength) {
                    padding[filled] = byte
                    filled += 1
                }
            }
        }
        val encoded = ByteArray(keySize)
        encoded[0] = 0.toByte()
        encoded[1] = 2.toByte()
        System.arraycopy(padding, 0, encoded, 2, paddingLength)
        encoded[2 + paddingLength] = 0.toByte()
        System.arraycopy(message, 0, encoded, 3 + paddingLength, message.size)

        val encrypted = BigInteger(1, encoded)
            .modPow(BigInteger.valueOf(ConsoleConstants.FORUM_RSA_E), modulus)
            .toByteArray()

        val out = ByteArray(keySize)
        if (encrypted.size > keySize) {
            System.arraycopy(encrypted, encrypted.size - keySize, out, 0, keySize)
        } else {
            System.arraycopy(encrypted, 0, out, keySize - encrypted.size, encrypted.size)
        }
        return out
    }

    private fun validateKey(key: ByteArray) {
        require(key.size == 16 || key.size == 24 || key.size == 32) {
            "AES key must be 16, 24, or 32 bytes"
        }
    }

    private companion object {
        const val HEX = "0123456789abcdef"
    }
}
