package org.example.backend.domain.aquarium.controller

import org.example.backend.config.TestContainerConfig
import org.example.backend.domain.aquarium.entity.Aquarium
import org.example.backend.domain.aquarium.repository.AquariumRepository
import org.example.backend.domain.fish.entity.Fish
import org.example.backend.domain.fish.repository.FishRepository
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.domain.member.service.AuthTokenService
import org.example.backend.global.LoginUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

@Import(TestContainerConfig::class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AquariumControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var aquariumRepository: AquariumRepository

    @Autowired
    private lateinit var fishRepository: FishRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var authTokenService: AuthTokenService

    private lateinit var testMember: Member // 어항 생성시 사용할 test member
    private lateinit var jwtToken: String // 테스트시 사용할 jwt 토큰

    @BeforeEach
    fun setUp() {
        val loginUtil = LoginUtil(memberRepository, passwordEncoder, authTokenService)

        // Repository를 직접 사용하여 빠르게 회원 생성 및 토큰 발급
        jwtToken = loginUtil.createMemberAndGetToken(
            "aquarium@test.com", "test1234", "aquarium",
            ""
        )
        testMember = loginUtil.getMemberByEmail("aquarium@test.com")
    }

    @Test
    @DisplayName("t1: 어항 생성")
    fun createAquarium() {
        mvc.perform(
            MockMvcRequestBuilders.post("/api/aquarium")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                     {"aquariumName": "test"}
                    
                    """.trimIndent()
                )
                .header("Authorization", "Bearer " + jwtToken)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("어항이 생성되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.aquariumId").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.aquariumName").value("test"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.createDate").exists())
    }

    @Test
    @DisplayName("t2: 어항 다건 조회")
    fun getAquariums() {
        val aquarium = aquariumRepository.save<Aquarium>(Aquarium(testMember, "test"))

        mvc.perform(
            MockMvcRequestBuilders.get("/api/aquarium")
                .header("Authorization", "Bearer " + jwtToken)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("어항 목록이 조회되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].aquariumId").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].aquariumName").value("test"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].createDate").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].notifyCycleDate").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].lastNotifyDate").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].nextNotifyDate").doesNotExist())
    }

    @Test
    @DisplayName("t3: 어항 단건 조회")
    fun getAquarium() {
        val aquarium = aquariumRepository.save<Aquarium>(Aquarium(testMember, "test"))

        mvc.perform(
            MockMvcRequestBuilders.get("/api/aquarium/{id}", aquarium.id)
                .header("Authorization", "Bearer " + jwtToken)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("어항이 조회되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.aquariumId").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.aquariumName").value("test"))
    }

    @Test
    @DisplayName("t4: 어항 단건 조회 실패 - 존재하지 않는 어항 조회")
    fun getAquariumFail() {
        mvc.perform(
            MockMvcRequestBuilders.get("/api/aquarium/{id}", 1)
                .header("Authorization", "Bearer " + jwtToken)
        )
            .andExpect(MockMvcResultMatchers.status().is4xxClientError())
    }

    @Test
    @DisplayName("t5: 어항 이름 수정")
    fun updateAquariumName() {
        val aquarium = aquariumRepository.save<Aquarium>(Aquarium(testMember, "test"))

        mvc.perform(
            MockMvcRequestBuilders.put("/api/aquarium/{id}", aquarium.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                     {"aquariumName": "newName"}
                    
                    """.trimIndent()
                )
                .header("Authorization", "Bearer " + jwtToken)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("어항이 수정되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.aquariumId").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.aquariumName").value("newName"))

        // 어항 이름을 "내가 키운 물고기"로 변경하려할 경우
        mvc.perform(
            MockMvcRequestBuilders.put("/api/aquarium/{id}", aquarium.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                     {"aquariumName": "내가 키운 물고기"}
                    
                    """.trimIndent()
                )
                .header("Authorization", "Bearer " + jwtToken)
        )
            .andExpect(MockMvcResultMatchers.status().is4xxClientError())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("어항 이름으로 '내가 키운 물고기'는 사용할 수 없습니다."))
    }

    @Test
    @DisplayName("t6: 어항 이름 수정 실패 - 존재하지 않는 어항 수정")
    fun updateAquariumNameFail() {
        mvc.perform(
            MockMvcRequestBuilders.put("/api/aquarium/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                     {"aquariumName": "newName"}
                    
                    """.trimIndent()
                )
                .header("Authorization", "Bearer " + jwtToken)
        )
            .andExpect(MockMvcResultMatchers.status().is4xxClientError())
    }

    @Test
    @DisplayName("t7: 삭제 전, 어항 속 물고기 존재 여부 확인")
    fun checkFishInAquarium() {
        val aquarium = aquariumRepository.save<Aquarium>(Aquarium(testMember, "test"))

        // 물고기 없는 경우
        mvc.perform(
            MockMvcRequestBuilders.get("/api/aquarium/{id}/delete", aquarium.id)
                .header("Authorization", "Bearer " + jwtToken)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("어항의 물고기 존재 여부를 확인했습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").value(false))

        // 물고기 있는 경우
        val fish = Fish(aquarium, "test", "test")
        fishRepository.save<Fish>(fish)

        mvc.perform(
            MockMvcRequestBuilders.get("/api/aquarium/{id}/delete", aquarium.id)
                .header("Authorization", "Bearer " + jwtToken)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("어항의 물고기 존재 여부를 확인했습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").value(true))
    }

    @Test
    @DisplayName("t8: 어항 속 물고기들을 '내가 키운 물고기' 어항으로 이동")
    fun moveFishToOwnedAquarium() {
        val aquarium = aquariumRepository.save<Aquarium>(Aquarium(testMember, "test"))
        val fish = Fish(aquarium, "test", "test")
        fishRepository.save<Fish>(fish)

        mvc.perform(
            MockMvcRequestBuilders.put("/api/aquarium/{id}/delete", aquarium.id)
                .header("Authorization", "Bearer " + jwtToken)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("물고기들이 '내가 키운 물고기' 어항으로 이동되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").value("물고기 이동 완료"))
    }

    @Test
    @DisplayName("t9: 빈 어항 삭제")
    fun deleteAquarium() {
        val aquarium = aquariumRepository.save<Aquarium>(Aquarium(testMember, "test"))

        mvc.perform(
            MockMvcRequestBuilders.delete("/api/aquarium/{id}/delete", aquarium.id)
                .header("Authorization", "Bearer " + jwtToken)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("어항이 삭제되었습니다."))
    }

    @Test
    @DisplayName("t10: 어항 관리 알림 주기 설정")
    fun scheduleSetting() {
        val aquarium = aquariumRepository.save<Aquarium>(Aquarium(testMember, "test"))

        mvc.perform(
            MockMvcRequestBuilders.post("/api/aquarium/{id}/schedule", aquarium.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                     {"cycleDate": 21}
                    
                    """.trimIndent()
                )
                .header("Authorization", "Bearer " + jwtToken)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("물갈이&어항세척 스케줄 알림이 설정되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.aquariumId").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.aquariumName").value("test"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.notifyCycleDate").value(21))
    }
}