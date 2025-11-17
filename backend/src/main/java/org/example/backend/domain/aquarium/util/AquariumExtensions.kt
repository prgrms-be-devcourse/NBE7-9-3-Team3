package org.example.backend.domain.aquarium.util

import org.example.backend.domain.aquarium.entity.Aquarium

/**
 * Java 클래스 Aquarium의 임시 확장 함수
 * Aquarium이 코틀린으로 변경되면 제거 예정
 * 
 * 임시 코드: Java 클래스와 Kotlin 코드 간의 호환성을 위해 작성됨
 * Aquarium 엔티티가 Kotlin으로 전환되면 이 파일은 제거될 예정입니다.
 */
fun Aquarium.getIdSafely(): Long? {
    return try {
        val getIdMethod = Aquarium::class.java.getMethod("getId")
        getIdMethod.invoke(this) as? Long
    } catch (e: Exception) {
        // 메서드를 찾을 수 없거나 호출에 실패한 경우 (예: Aquarium이 Kotlin으로 전환되어 .id로 직접 접근 가능한 경우)
        // 또는 다른 예외 발생 시 null 반환
        null
    }
}

