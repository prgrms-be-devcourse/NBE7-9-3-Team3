package org.example.backend.domain.fish.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.example.backend.domain.aquarium.entity.Aquarium;
import org.example.backend.domain.aquarium.repository.AquariumRepository;
import org.example.backend.domain.fish.entity.Fish;
import org.example.backend.domain.fish.repository.FishRepository;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.member.service.AuthTokenService;
import org.example.backend.global.LoginUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FishControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private AquariumRepository aquariumRepository;
    @Autowired
    private FishRepository fishRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthTokenService authTokenService;

    private Aquarium testAquarium;
    private String jwtToken;  // 테스트시 사용할 jwt 토큰

    /*
    FishController를 테스트하기 위해 필수로 진행되어야할
    회원가입, 로그인, 어항 생성
     */
    @BeforeEach
    void setup() {
        LoginUtil loginUtil = new LoginUtil(memberRepository, passwordEncoder, authTokenService);

        // Repository를 직접 사용하여 빠르게 회원 생성 및 토큰 발급
        jwtToken = loginUtil.createMemberAndGetToken("fish@test.com", "test1234", "fish", "");
        Member testMember = loginUtil.getMemberByEmail("fish@test.com");

        // 어항 생성
        testAquarium = new Aquarium(testMember, "test");
        aquariumRepository.save(testAquarium);

        fishRepository.deleteAll();
    }

    @Test
    @DisplayName("t1: 물고기 생성")
    void createFish() throws Exception {
        mvc.perform(post("/api/aquarium/{aquariumId}/fish", testAquarium.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                     {"species": "test", "name": "test"}
                    """)
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.msg").value("물고기가 생성되었습니다."))
            .andExpect(jsonPath("$.data.fishId").isNotEmpty())
            .andExpect(jsonPath("$.data.fishSpecies").value("test"))
            .andExpect(jsonPath("$.data.fishName").value("test"));
    }

    @Test
    @DisplayName("t2: 물고기 생성 실패 - 존재하지 않는 어항")
    void createFishFail() throws Exception {
        mvc.perform(post("/api/aquarium/{aquariumId}/fish", 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                     {"species": "test", "name": "test"}
                    """)
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("t3: 물고기 다건 조회")
    void getFishes() throws Exception {
        Fish fish = new Fish(testAquarium, "test", "test");
        fishRepository.save(fish);

        mvc.perform(get("/api/aquarium/{aquariumId}/fish", testAquarium.getId())
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.msg").value("물고기들이 조회되었습니다."))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].fishId").isNumber())
            .andExpect(jsonPath("$.data[0].fishSpecies").value("test"))
            .andExpect(jsonPath("$.data[0].fishName").value("test"));
    }

    @Test
    @DisplayName("t4: 물고기 종, 이름 수정")
    void updateFish() throws Exception {
        Fish fish = new Fish(testAquarium, "test", "test");
        fishRepository.save(fish);

        mvc.perform(
                put("/api/aquarium/{aquariumId}/fish/{fishId}", testAquarium.getId(), fish.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                         {"species": "newSpecies", "name": "newName"}
                        """)
                    .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.msg").value("물고기 종과 이름이 수정되었습니다."))
            .andExpect(jsonPath("$.data.fishId").isNotEmpty())
            .andExpect(jsonPath("$.data.fishSpecies").value("newSpecies"))
            .andExpect(jsonPath("$.data.fishName").value("newName"));
    }

    @Test
    @DisplayName("t5: 물고기 종, 이름 수정 실패 - 존재하지 않는 어항")
    void updateFishFail1() throws Exception {
        mvc.perform(put("/api/aquarium/{aquariumId}/fish/{fishId}", 0, 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                     {"species": "newSpecies", "name": "newName"}
                    """)
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("t6: 물고기 종, 이름 수정 실패 - 존재하지 않는 물고기")
    void updateFishFail2() throws Exception {
        mvc.perform(put("/api/aquarium/{aquariumId}/fish/{fishId}", testAquarium.getId(), 0)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                     {"species": "newSpecies", "name": "newName"}
                    """)
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("t7: 물고기 삭제")
    void deleteFish() throws Exception {
        Fish fish = new Fish(testAquarium, "test", "test");
        fishRepository.save(fish);

        mvc.perform(
                delete("/api/aquarium/{aquariumId}/fish/{fishId}", testAquarium.getId(), fish.getId())
                    .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.msg").value("물고기가 삭제되었습니다."));
    }

    @Test
    @DisplayName("t8: 물고기 삭제 실패 - 존재하지 않는 물고기 삭제")
    void deleteFishFail() throws Exception {
        mvc.perform(delete("/api/aquarium/{aquariumId}/fish/{fishId}", testAquarium.getId(), 1)
                .header("Authorization", "Bearer " + jwtToken))
            .andExpect(status().is4xxClientError());
    }
}
