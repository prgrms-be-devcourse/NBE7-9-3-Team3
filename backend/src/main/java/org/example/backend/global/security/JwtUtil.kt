package org.example.backend.global.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.nio.charset.StandardCharsets
import java.security.Key
import java.util.*

object JwtUtil {
    @JvmStatic
    fun toString(
        secret: String,
        expireSeconds: Long,
        body: MutableMap<String, Any>
    ): String {
        val claimsBuilder = Jwts.claims()

        for (entry in body.entries) {
            claimsBuilder.add(entry.key, entry.value)
        }

        val claims = claimsBuilder.build()

        val issuedAt = Date()
        val expiration = Date(issuedAt.time + 1000L * expireSeconds)

        val secretKey: Key = Keys.hmacShaKeyFor(secret.toByteArray())

        return Jwts.builder()
            .claims(claims)
            .issuedAt(issuedAt)
            .expiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    fun isValid(jwt: String, secretPattern: String): Boolean {
        val secretKey = Keys.hmacShaKeyFor(secretPattern.toByteArray(StandardCharsets.UTF_8))

        return try {
            Jwts
                .parser()
                .verifyWith(secretKey)
                .build()
                .parse(jwt)
            true
        } catch (e: Exception) {
            false
        }
    }

    @JvmStatic
    fun payloadOrNull(jwt: String, secretPattern: String): MutableMap<String, Any>? {
        val secretKey = Keys.hmacShaKeyFor(secretPattern.toByteArray(StandardCharsets.UTF_8))

        return if (isValid(jwt, secretPattern)) {
            val claims = Jwts
                .parser()
                .verifyWith(secretKey)
                .build()
                .parse(jwt)
                .payload as Claims
            HashMap(claims)
        } else {
            null
        }
    }
}
