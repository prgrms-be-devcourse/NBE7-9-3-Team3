package org.example.backend.domain.fish.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.domain.aquarium.entity.Aquarium;
import org.example.backend.domain.aquarium.repository.AquariumRepository;
import org.example.backend.domain.fish.entity.Fish;
import org.example.backend.domain.fish.repository.FishRepository;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private ObjectMapper objectMapper = new ObjectMapper();
    private Member testMember;
    private Aquarium testAquarium;
    private String jwtToken;  // 테스트시 사용할 jwt 토큰

    /*
    FishController를 테스트하기 위해 필수로 진행되어야할
    회원가입, 로그인, 어항 생성
     */
    @BeforeAll
    void initRequiredProcess() throws Exception {
        // 회원가입
        testMember = memberRepository.findByEmail("test1@test.com")
                .orElseGet(() -> {
                    try {
                        MvcResult result = mvc.perform(post("/api/members/join")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("""
                                                {
                                                  "email": "test1@test.com",
                                                  "password": "test1234",
                                                  "nickname": "test",
                                                  "profileImage": null
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andReturn();

                        // mvcResult -> Json -> Member 변환
                        String responseBody = result.getResponse().getContentAsString();
                        JsonNode dataNode = objectMapper.readTree(responseBody).get("data");
                        return objectMapper.treeToValue(dataNode, Member.class);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        // 로그인 → JWT 토큰 발급
        MvcResult loginResult = mvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "email": "test1@test.com",
                                   "password": "test1234"
                                 }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        jwtToken = loginResult.getResponse().getCookie("accessToken").getValue();

        // 어항 생성
        testAquarium = new Aquarium(testMember, "test");
        aquariumRepository.save(testAquarium);
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

        mvc.perform(put("/api/aquarium/{aquariumId}/fish/{fishId}", testAquarium.getId(), fish.getId())
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

        mvc.perform(delete("/api/aquarium/{aquariumId}/fish/{fishId}", testAquarium.getId(), fish.getId())
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
