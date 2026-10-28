package aeeL.theoSol.core

import java.util.Base64
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CryptoEngineTest {
    private val engine = CryptoEngine()

    @Test
    fun md5MatchesKnownVector() {
        assertEquals("900150983cd24fb0d6963f7d28e17f72", engine.md5Hex("abc"))
    }

    @Test
    fun aesCbcMatchesNistVector() {
        val key = engine.fromHex("2b7e151628aed2a6abf7158809cf4f3c")
        val iv = engine.fromHex("000102030405060708090a0b0c0d0e0f")
        val plain = engine.fromHex(
            "6bc1bee22e409f96e93d7e117393172a" +
                "ae2d8a571e03ac9c9eb76fac45af8e51",
        )
        val encrypted = engine.aesCbcEncrypt(key, iv, plain)
        assertEquals(
            "7649abac8119b246cee98e9b12e9197d" +
                "5086cb9b507219ee95db113a917678b2",
            engine.toHex(encrypted).take(64),
        )
        assertArrayEquals(plain, engine.aesCbcDecrypt(key, iv, encrypted))
    }

    @Test
    fun sealedPasswordUsesTheRsaBlockSize() {
        val first = engine.sealForumPassword("aeeL", "correct horse battery staple")
        val second = engine.sealForumPassword("aeeL", "correct horse battery staple")
        val block = Base64.getDecoder().decode(first)
        assertEquals(256, block.size)
        assertTrue(first != second)
    }

    @Test
    fun timingSafeEqualComparesContent() {
        assertTrue(engine.timingSafeEqual(byteArrayOf(1, 2, 3), byteArrayOf(1, 2, 3)))
        assertFalse(engine.timingSafeEqual(byteArrayOf(1, 2, 3), byteArrayOf(1, 2, 4)))
    }

    @Test
    fun fingerprintIsEightHexCharacters() {
        val fingerprint = engine.keyFingerprint("0123456789abcdef")
        assertEquals(8, fingerprint.length)
        assertEquals(engine.md5Hex("0123456789abcdef").take(8), fingerprint)
    }
}
