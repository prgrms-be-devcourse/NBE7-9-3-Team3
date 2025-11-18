package org.example.backend.domain.fish.controller

import org.example.backend.config.TestContainerConfig
import org.example.backend.domain.aquarium.entity.Aquarium
import org.example.backend.domain.aquarium.repository.AquariumRepository
import org.example.backend.domain.fish.entity.Fish
import org.example.backend.domain.fish.repository.FishRepository
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
class FishControllerTest {
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

    private lateinit var testAquarium: Aquarium
    private lateinit var jwtToken: String // 테스트시 사용할 jwt 토큰

    /*
    FishController를 테스트하기 위해 필수로 진행되어야할
    회원가입, 로그인, 어항 생성
     */
    @BeforeEach
    fun setUp() {
        val loginUtil = LoginUtil(memberRepository, passwordEncoder, authTokenService)

        // Repository를 직접 사용하여 빠르게 회원 생성 및 토큰 발급
        jwtToken = loginUtil.createMemberAndGetToken("fish@test.com", "test1234", "fish", "")
        val testMember = loginUtil.getMemberByEmail("fish@test.com")

        // 어항 생성
        testAquarium = Aquarium(testMember, "test")
        aquariumRepository.save<Aquarium>(testAquarium)
    }

    @Test
    @DisplayName("t1: 물고기 생성")
    fun createFish() {
        mvc.perform(
            MockMvcRequestBuilders.post("/api/aquarium/{aquariumId}/fish", testAquarium.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                     {"species": "test", "name": "test"}
                    
                    """.trimIndent()
                )
                .header("Authorization", "Bearer " + jwtToken)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("물고기가 생성되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.fishId").isNotEmpty())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.fishSpecies").value("test"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.fishName").value("test"))
    }

    @Test
    @DisplayName("t2: 물고기 생성 실패 - 존재하지 않는 어항")
    fun createFishFail() {
        mvc.perform(
            MockMvcRequestBuilders.post("/api/aquarium/{aquariumId}/fish", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                     {"species": "test", "name": "test"}
                    
                    """.trimIndent()
                )
                .header("Authorization", "Bearer " + jwtToken)
        )
            .andExpect(MockMvcResultMatchers.status().is4xxClientError())
    }

    @Test
    @DisplayName("t3: 물고기 다건 조회")
    fun getFishes() {
        val fish = Fish(testAquarium, "test", "test")
        fishRepository.save<Fish>(fish)

        mvc.perform(
            MockMvcRequestBuilders.get("/api/aquarium/{aquariumId}/fish", testAquarium.id)
                .header("Authorization", "Bearer " + jwtToken)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("물고기들이 조회되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fishId").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fishSpecies").value("test"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fishName").value("test"))
    }

    @Test
    @DisplayName("t4: 물고기 종, 이름 수정")
    fun updateFish() {
        val fish = Fish(testAquarium, "test", "test")
        fishRepository.save<Fish>(fish)

        mvc.perform(
            MockMvcRequestBuilders.put("/api/aquarium/{aquariumId}/fish/{fishId}", testAquarium.id, fish.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                         {"species": "newSpecies", "name": "newName"}
                        
                        """.trimIndent()
                )
                .header("Authorization", "Bearer " + jwtToken)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("물고기 종과 이름이 수정되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.fishId").isNotEmpty())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.fishSpecies").value("newSpecies"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.fishName").value("newName"))
    }

    @Test
    @DisplayName("t5: 물고기 종, 이름 수정 실패 - 존재하지 않는 어항")
    fun updateFishFail1() {
        mvc.perform(
            MockMvcRequestBuilders.put("/api/aquarium/{aquariumId}/fish/{fishId}", 0, 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                     {"species": "newSpecies", "name": "newName"}
                    
                    """.trimIndent()
                )
                .header("Authorization", "Bearer " + jwtToken)
        )
            .andExpect(MockMvcResultMatchers.status().is4xxClientError())
    }

    @Test
    @DisplayName("t6: 물고기 종, 이름 수정 실패 - 존재하지 않는 물고기")
    fun updateFishFail2() {
        mvc.perform(
            MockMvcRequestBuilders.put("/api/aquarium/{aquariumId}/fish/{fishId}", testAquarium.id, 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                     {"species": "newSpecies", "name": "newName"}
                    
                    """.trimIndent()
                )
                .header("Authorization", "Bearer " + jwtToken)
        )
            .andExpect(MockMvcResultMatchers.status().is4xxClientError())
    }

    @Test
    @DisplayName("t7: 물고기 삭제")
    fun deleteFish() {
        val fish = Fish(testAquarium, "test", "test")
        fishRepository.save<Fish>(fish)

        mvc.perform(
            MockMvcRequestBuilders.delete("/api/aquarium/{aquariumId}/fish/{fishId}", testAquarium.id, fish.id)
                .header("Authorization", "Bearer " + jwtToken)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("물고기가 삭제되었습니다."))
    }

    @Test
    @DisplayName("t8: 물고기 삭제 실패 - 존재하지 않는 물고기 삭제")
    fun deleteFishFail() {
        mvc.perform(
            MockMvcRequestBuilders.delete("/api/aquarium/{aquariumId}/fish/{fishId}", testAquarium.id, 1)
                .header("Authorization", "Bearer " + jwtToken)
        )
            .andExpect(MockMvcResultMatchers.status().is4xxClientError())
    }
}
