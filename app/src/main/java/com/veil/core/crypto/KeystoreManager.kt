package com.veil.core.crypto

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricPrompt
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages hardware-backed encryption keys via Android Keystore (StrongBox / TEE).
 * Wraps the Vault Master Key so that biometrics can quickly unlock the vault without
 * repeatedly running CPU-heavy KDF derivations.
 */
class KeystoreManager(private val context: Context) {
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS_BIOMETRIC = "veil_keystore_bio_master"
        private const val GCM_IV_BYTES = 12
        private const val GCM_TAG_BITS = 128
    }

    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    fun isBiometricKeyEnrolled(): Boolean {
        return keyStore.containsAlias(KEY_ALIAS_BIOMETRIC)
    }

    fun generateBiometricKey(requireUserAuth: Boolean = true) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val builder = KeyGenParameterSpec.Builder(
            KEY_ALIAS_BIOMETRIC,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)

        if (requireUserAuth) {
            builder.setUserAuthenticationRequired(true)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                builder.setUserAuthenticationParameters(
                    0, // 0 = requires biometric prompt per cryptographic operation
                    KeyProperties.AUTH_BIOMETRIC_STRONG
                )
            }
        }

        keyGenerator.init(builder.build())
        keyGenerator.generateKey()
    }

    fun getEncryptCipher(): Cipher {
        val secretKey = keyStore.getKey(KEY_ALIAS_BIOMETRIC, null) as SecretKey
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher
    }

    fun getDecryptCipher(iv: ByteArray): Cipher {
        val secretKey = keyStore.getKey(KEY_ALIAS_BIOMETRIC, null) as SecretKey
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher
    }

    fun wrapMasterKey(masterKey: ByteArray, cipher: Cipher): Pair<ByteArray, ByteArray> {
        val ciphertext = cipher.doFinal(masterKey)
        return Pair(cipher.iv, ciphertext)
    }

    fun unwrapMasterKey(iv: ByteArray, encryptedMasterKey: ByteArray, cipher: Cipher): SecureBytes {
        val plaintext = cipher.doFinal(encryptedMasterKey)
        return SecureBytes(plaintext)
    }

    fun deleteKeys() {
        if (keyStore.containsAlias(KEY_ALIAS_BIOMETRIC)) {
            keyStore.deleteEntry(KEY_ALIAS_BIOMETRIC)
        }
    }
}
