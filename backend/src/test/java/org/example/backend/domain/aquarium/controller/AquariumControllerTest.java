package org.example.backend.domain.aquarium.controller;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)  // @BeforeAll을 non-static 설정
public class AquariumControllerTest {

  @Autowired
  private MockMvc mvc;
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private AquariumRepository aquariumRepository;
  @Autowired
  private FishRepository fishRepository;

  private ObjectMapper objectMapper = new ObjectMapper();
  private Member testMember;  // 어항 생성시 사용할 test member
  private String jwtToken;  // 테스트시 사용할 jwt 토큰

  @BeforeAll
  void initMemberAndLogin() throws Exception {
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
  }

  @Test
  @DisplayName("t1: 어항 생성")
  void createAquarium() throws Exception {
    mvc.perform(post("/api/aquarium")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
             {"aquariumName": "test"}
            """)
            .header("Authorization", "Bearer " + jwtToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.msg").value("어항이 생성되었습니다."));
  }

  @Test
  @DisplayName("t2: 어항 다건 조회")
  void getAquariums() throws Exception {
    Aquarium aquarium = aquariumRepository.save(new Aquarium(testMember, "test"));

    mvc.perform(get("/api/aquarium")
            .header("Authorization", "Bearer " + jwtToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.msg").value("어항 목록이 조회되었습니다."));
  }

  @Test
  @DisplayName("t3: 어항 단건 조회")
  void getAquarium() throws Exception {
    Aquarium aquarium = aquariumRepository.save(new Aquarium(testMember, "test"));

    mvc.perform(get("/api/aquarium/{id}", aquarium.getId())
            .header("Authorization", "Bearer " + jwtToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.msg").value("어항이 조회되었습니다."));
  }

  @Test
  @DisplayName("t4: 어항 이름 수정")
  void updateAquariumName() throws Exception {
    Aquarium aquarium = aquariumRepository.save(new Aquarium(testMember, "test"));

    mvc.perform(put("/api/aquarium/{id}", aquarium.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
             {"aquariumName": "newName"}
            """)
            .header("Authorization", "Bearer " + jwtToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.msg").value("어항이 수정되었습니다."));

    // 어항 이름을 "내가 키운 물고기"로 변경하려할 경우
    mvc.perform(put("/api/aquarium/{id}", aquarium.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
             {"aquariumName": "내가 키운 물고기"}
            """)
            .header("Authorization", "Bearer " + jwtToken))
        .andExpect(status().is4xxClientError())
        .andExpect(jsonPath("$.msg").value("어항 이름으로 '내가 키운 물고기'는 사용할 수 없습니다."));
  }

  @Test
  @DisplayName("t5: 삭제 전, 어항 속 물고기 존재 여부 확인")
  void checkFishInAquarium() throws Exception {
    Aquarium aquarium = aquariumRepository.save(new Aquarium(testMember, "test"));

    // 물고기 없는 경우
    mvc.perform(get("/api/aquarium/{id}/delete", aquarium.getId())
            .header("Authorization", "Bearer " + jwtToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.msg").value("어항의 물고기 존재 여부를 확인했습니다."))
        .andExpect(jsonPath("$.data").value(false));


    // 물고기 있는 경우
    Fish fish = new Fish(aquarium, "test", "test");
    fishRepository.save(fish);

    mvc.perform(get("/api/aquarium/{id}/delete", aquarium.getId())
            .header("Authorization", "Bearer " + jwtToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.msg").value("어항의 물고기 존재 여부를 확인했습니다."))
        .andExpect(jsonPath("$.data").value(true));
  }

  @Test
  @DisplayName("t6: 어항 속 물고기들을 '내가 키운 물고기' 어항으로 이동")
  void moveFishToOwnedAquarium() throws Exception {
    Aquarium aquarium = aquariumRepository.save(new Aquarium(testMember, "test"));
    Fish fish = new Fish(aquarium, "test", "test");
    fishRepository.save(fish);

    mvc.perform(put("/api/aquarium/{id}/delete", aquarium.getId())
            .header("Authorization", "Bearer " + jwtToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.msg").value("물고기들이 '내가 키운 물고기' 어항으로 이동되었습니다."))
        .andExpect(jsonPath("$.data").value("물고기 이동 완료"));
  }

  @Test
  @DisplayName("t7: 빈 어항 삭제")
  void deleteAquarium() throws Exception {
    Aquarium aquarium = aquariumRepository.save(new Aquarium(testMember, "test"));

    mvc.perform(delete("/api/aquarium/{id}/delete", aquarium.getId())
            .header("Authorization", "Bearer " + jwtToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.msg").value("어항이 삭제되었습니다."));
  }

  @Test
  @DisplayName("t8: 어항 관리 알림 주기 설정")
  void scheduleSetting() throws Exception {
    Aquarium aquarium = aquariumRepository.save(new Aquarium(testMember, "test"));

    mvc.perform(post("/api/aquarium/{id}/schedule", aquarium.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
             {"cycleDate": 21}
            """)
            .header("Authorization", "Bearer " + jwtToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.msg").value("물갈이&어항세척 스케줄 알림이 설정되었습니다."));
  }
}