package org.example.backend.domain.aquarium.util

import org.example.backend.domain.aquarium.entity.Aquarium

/**
 * Java 클래스 Aquarium의 확장 함수
 * Aquarium이 Kotlin으로 변경되면 제거 예정
 */
fun Aquarium.getIdSafely(): Long? {
    return try {
        val getIdMethod = Aquarium::class.java.getMethod("getId")
        getIdMethod.invoke(this) as? Long
    } catch (e: Exception) {
        null
    }
}

