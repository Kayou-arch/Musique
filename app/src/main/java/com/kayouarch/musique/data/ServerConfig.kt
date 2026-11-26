package com.kayouarch.musique.data

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * On ne stocke jamais le mot de passe : seulement le sel et le token
 * (md5(motDePasse + sel)), comme le permet l'API Subsonic.
 */
data class ServerConfig(
    val url: String,
    val user: String,
    val salt: String,
    val token: String,
) {
    companion object {
        fun fromPassword(url: String, user: String, password: String): ServerConfig {
            val salt = ByteArray(8).also { SecureRandom().nextBytes(it) }.toHex()
            return ServerConfig(url, user, salt, md5(password + salt))
        }

        private fun md5(s: String): String =
            MessageDigest.getInstance("MD5").digest(s.toByteArray()).toHex()

        private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
    }
}
