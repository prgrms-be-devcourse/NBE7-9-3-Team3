package org.example.backend.global.initdata;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.aquarium.entity.Aquarium;
import org.example.backend.domain.aquarium.repository.AquariumRepository;
import org.example.backend.domain.fish.entity.Fish;
import org.example.backend.domain.fish.repository.FishRepository;
import org.example.backend.domain.follow.entity.Follow;
import org.example.backend.domain.follow.repository.FollowRepository;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.member.repository.MemberRepository;
import org.example.backend.domain.post.dto.PostWriteRequestDto;
import org.example.backend.domain.post.entity.Post;
import org.example.backend.domain.post.entity.Post.BoardType;
import org.example.backend.domain.post.entity.PostImage;
import org.example.backend.domain.post.repository.PostRepository;
import org.example.backend.domain.trade.entity.Trade;
import org.example.backend.domain.trade.repository.TradeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

@Configuration
@RequiredArgsConstructor
@Profile("init-data") // init-data 프로파일이 활성화될 때만 실행
public class BaseInitData {

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final AquariumRepository aquariumRepository;
    private final FishRepository fishRepository;
    private final PostRepository postRepository;
    private final TradeRepository tradeRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Transactional
    CommandLineRunner initBaseData() {
        return args -> {
            // 테스트 유저 데이터 생성
            createTestUsers();

            // 팔로우 관계 생성
            createFollowRelationships();

            // 어항 및 물고기 데이터 생성
            createAquariumsAndFish();

            // 게시글 데이터 생성
            createShowoffPosts();
            createQuestionPosts();

            // 거래 게시글 데이터 생성
            createFishTrades();
            createSecondhandTrades();
        };
    }

    // 글 생성 순서 섞기
    private void createShuffledItems(int itemsPerUser, ItemCreator creator) {
        List<Integer> allItems = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            for (int itemNum = 1; itemNum <= itemsPerUser; itemNum++) {
                allItems.add(i * 10 + itemNum); // 유저ID * 10 + 아이템번호로 고유 식별
            }
        }
        Collections.shuffle(allItems); // 모든 아이템 순서를 완전히 섞기
        
        for (int itemIndex = 0; itemIndex < allItems.size(); itemIndex++) {
            int itemId = allItems.get(itemIndex);
            int i = itemId / 10; // 유저 ID
            int itemNum = itemId % 10; // 아이템 번호
            
            Member member = memberRepository.findByMemberId((long) i).orElse(null);
            if (member == null) {
                continue;
            }
            
            creator.create(i, itemNum, member);
        }
    }

    @FunctionalInterface
    private interface ItemCreator {
        void create(int userId, int itemNum, Member member);
    }

    private void createTestUsers() {
        // 이미 데이터가 있는지 확인
        if (memberRepository.count() > 0) {
            return;
        }

        for (int i = 1; i <= 10; i++) {
            String email = "test" + i + "@test.com";
            String nickname = "test" + i;
            String password = "test1234";

            // 이미 존재하는지 확인
            if (memberRepository.findByEmail(email).isPresent()) {
                continue;
            }

            Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .profileImage("https://upload.wikimedia.org/wikipedia/commons/7/75/%EC%82%AC%EB%9E%8C.png")
                .build();

            memberRepository.save(member);
        }
    }

    private void createFollowRelationships() {
        // 이미 팔로우 관계가 있는지 확인
        if (followRepository.count() > 0) {
            return;
        }

        // test1부터 test10까지의 유저들을 조회
        for (int i = 1; i <= 10; i++) {
            Member follower = memberRepository.findByMemberId((long) i).orElse(null);

            if (follower == null) {
                continue;
            }

            // 각 유저가 다음 번호 유저 2명을 팔로우
            for (int j = 1; j <= 2; j++) {
                int followeeNumber = (i + j - 1) % 10 + 1; // 순환 로직
                Member followee = memberRepository.findByMemberId((long) followeeNumber).orElse(null);

                if (followee != null && !follower.getMemberId().equals(followee.getMemberId())) {
                    // 이미 팔로우 관계가 있는지 확인
                    if (!followRepository.existsByFollowerMemberIdAndFolloweeMemberId(
                        follower.getMemberId(), followee.getMemberId())) {

                        Follow follow = Follow.builder()
                            .follower(follower)
                            .followee(followee)
                            .build();

                        followRepository.save(follow);
                    }
                }
            }
        }
    }

    private void createAquariumsAndFish() {
        // 이미 어항 데이터가 있는지 확인
        if (aquariumRepository.count() > 0) {
            return;
        }

        String[] fishSpecies = {"금붕어", "구피", "네온테트라", "베타", "앵거피시", "플래티", "몰리", "다니오", "라스보라", "카디널테트라"};
        String[] fishNames = {"아름이", "예쁜이", "귀여니", "멋쟁이", "똑똑이", "영리이", "발랄이", "활발이", "사랑이", "행복이"};

        // test1부터 test10까지의 유저들에 대해 어항과 물고기 생성
        for (int i = 1; i <= 10; i++) {
            Member member = memberRepository.findByMemberId((long) i).orElse(null);

            if (member == null) {
                continue;
            }

            // 각 유저당 2개의 어항 생성
            for (int aquariumNum = 1; aquariumNum <= 2; aquariumNum++) {
                String aquariumName = "test" + i + "의 어항 " + aquariumNum;
                Aquarium aquarium = new Aquarium(member, aquariumName);
                Aquarium savedAquarium = aquariumRepository.save(aquarium);

                // 각 어항에 3마리의 물고기 생성
                for (int fishNum = 1; fishNum <= 3; fishNum++) {
                    String species = fishSpecies[(i + fishNum - 1) % fishSpecies.length];
                    String name = fishNames[(i + fishNum - 1) % fishNames.length] + fishNum;

                    Fish fish = new Fish(savedAquarium, species, name);
                    fishRepository.save(fish);
                }
            }
        }
    }

    private void createShowoffPosts() {
        // 이미 자랑게시판 데이터가 있는지 확인 (SHOWOFF 타입만 체크)
        if (!postRepository.findByBoardType(org.example.backend.domain.post.entity.Post.BoardType.SHOWOFF).isEmpty()) {
            return;
        }

        String[] showoffTitles = {
            "우리 집 금붕어 자랑해요!",
            "새로 만든 어항 세팅",
            "물고기들이 너무 귀여워요",
            "어항 장식 완성!",
            "건강한 물고기들"
        };

        String[] showoffContents = {
            "우리 집 금붕어가 정말 건강하게 잘 자라고 있어요! 물을 깨끗하게 관리하고 있어서 색깔도 예쁘고 활발해요.",
            "새로운 어항을 세팅했는데 물고기들이 정말 좋아하네요. 필터와 히터도 새로 설치해서 최적의 환경을 만들어줬어요.",
            "우리 물고기들이 너무 귀여워서 매일 보는 재미가 있어요. 먹이를 줄 때마다 달려와서 정말 사랑스러워요.",
            "어항 장식을 새로 해봤는데 물고기들이 더 예뻐 보여요. 산호와 돌로 자연스러운 환경을 만들어줬어요.",
            "우리 물고기들이 정말 건강해요. 수질 관리도 잘하고 있고, 먹이도 적당히 주고 있어서 활발하게 헤엄치고 있어요."
        };

        // 모든 게시글을 개별적으로 생성하여 완전히 섞기
        createShuffledItems(5, (i, postNum, member) -> {
            String title = showoffTitles[(i + postNum - 1) % showoffTitles.length] + " " + postNum;
            String content = showoffContents[(i + postNum - 1) % showoffContents.length] +
                " (작성자: test" + i + ")";

            // 자랑게시판용 이미지들 (물고기/어항 관련)
            String[] showoffImages = {
                "https://images.unsplash.com/photo-1535591273668-578e31182c4f?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NHx8JUVCJUFDJUJDJUVBJUIzJUEwJUVBJUI4JUIwfGVufDB8fDB8fHww&fm=jpg&q=60&w=3000",
                "https://t1.daumcdn.net/news/202211/30/nongmin/20221130163646676iqlt.png",
                "https://marketplace.canva.com/XdJTM/MAGyo8XdJTM/1/tl/canva-adorable-cartoon-blue-fish-illustration-MAGyo8XdJTM.png",
                "https://thumbnail.coupangcdn.com/thumbnails/remote/492x492ex/image/vendor_inventory/5eed/1ba36cb8138318d15d5db5fccc1bb7693b7de596e033de724cb805e7ba4b.jpg",
                "https://raraaqua.com/web/product/medium/202508/6101dc2e316a22869d4a905629f97970.jpg"
            };
            
            String selectedImage = showoffImages[(i + postNum - 1) % showoffImages.length];

            // PostWriteRequestDto를 사용하여 Post 생성
            PostWriteRequestDto requestDto = new PostWriteRequestDto(
                title,
                content,
                BoardType.SHOWOFF,
                List.of(selectedImage), // 자랑게시판은 이미지 필수
                null
            );

            Post post = new Post(requestDto, member);

            // 게시글에 이미지 추가
            if (requestDto.imageUrls() != null && !requestDto.imageUrls().isEmpty()) {
                requestDto.imageUrls().forEach(url -> 
                    post.addImage(new PostImage(url, post)));
            }

            postRepository.save(post);
        });
    }

    private void createQuestionPosts() {
        // 이미 질문게시판 데이터가 있는지 확인 (QUESTION 타입만 체크)
        if (!postRepository.findByBoardType(org.example.backend.domain.post.entity.Post.BoardType.QUESTION).isEmpty()) {
            return;
        }

        String[] questionTitles = {
            "물고기가 잘 안 먹어요",
            "어항 수질 관리 질문",
            "물고기 질병 증상이 뭔가요?",
            "어항 크기 추천해주세요",
            "물고기 키우기 초보 질문"
        };

        String[] questionContents = {
            "물고기가 최근에 먹이를 잘 안 먹는데 왜 그런 건가요? 수질은 깨끗한 것 같은데...",
            "어항 수질 관리를 어떻게 해야 할지 모르겠어요. 필터 교체 주기나 물갈이 주기를 알려주세요.",
            "우리 물고기 몸에 하얀 점들이 생겼는데 이게 질병인가요? 어떻게 치료해야 할까요?",
            "물고기 3마리 키우려는데 어항 크기는 얼마나 큰 게 좋을까요? 추천해주세요.",
            "물고기 키우기를 처음 시작하는데 어떤 장비들이 필요한지, 초보자 팁을 알려주세요."
        };

        // 모든 게시글을 개별적으로 생성하여 완전히 섞기
        createShuffledItems(5, (i, postNum, member) -> {
            String title = questionTitles[(i + postNum - 1) % questionTitles.length] + " " + postNum;
            String content = questionContents[(i + postNum - 1) % questionContents.length] +
                " (작성자: test" + i + ")";

            // PostWriteRequestDto를 사용하여 Post 생성 (질문게시판은 이미지 없음)
            PostWriteRequestDto requestDto = new PostWriteRequestDto(
                title,
                content,
                BoardType.QUESTION,
                null, // 질문게시판은 이미지 없음
                null
            );

            Post post = new Post(requestDto, member);
            postRepository.save(post);
        });
    }

    private void createFishTrades() {
        // 이미 물고기 거래 데이터가 있는지 확인 (FISH 타입만 체크)
        if (tradeRepository.findByBoardType(org.example.backend.domain.trade.enums.BoardType.FISH, 
                PageRequest.of(0, 1)).getTotalElements() > 0) {
            return;
        }

        String[] fishTitles = {
            "건강한 금붕어 판매",
            "구피 새끼 판매합니다",
            "베타 물고기 판매",
            "네온테트라 판매",
            "앵거피시 판매"
        };

        String[] fishContents = {
            "건강한 금붕어를 판매합니다. 초보자도 키우기 쉽고 색깔도 예뻐요.",
            "구피 새끼들을 판매합니다. 부모 물고기도 건강하고 새끼들도 활발해요.",
            "아름다운 베타 물고기를 판매합니다. 색깔이 정말 예쁘고 건강해요.",
            "네온테트라를 판매합니다. 작고 귀여운 물고기로 어항에 잘 어울려요.",
            "앵거피시를 판매합니다. 독특한 모양의 물고기로 관상용으로 좋아요."
        };

        // 모든 거래글을 개별적으로 생성하여 완전히 섞기
        createShuffledItems(2, (i, tradeNum, member) -> {
            String title = fishTitles[(i + tradeNum - 1) % fishTitles.length] + " " + tradeNum;
            String content = fishContents[(i + tradeNum - 1) % fishContents.length] +
                " (판매자: test" + i + ")";

            // 물고기 거래는 물고기 이미지만 사용
            String[] fishImages = {
                "https://images.unsplash.com/photo-1535591273668-578e31182c4f?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NHx8JUVCJUFDJUJDJUVBJUIzJUEwJUVBJUI4JUIwfGVufDB8fDB8fHww&fm=jpg&q=60&w=3000",
                "https://marketplace.canva.com/XdJTM/MAGyo8XdJTM/1/tl/canva-adorable-cartoon-blue-fish-illustration-MAGyo8XdJTM.png"
            };
            
            String selectedImage = fishImages[(i + tradeNum - 1) % fishImages.length];

            Trade trade = new Trade(
                member,
                org.example.backend.domain.trade.enums.BoardType.FISH,
                title,
                content,
                5000L + (i * 1000L) + (tradeNum * 500L), // 물고기 가격 (5000원부터 시작)
                org.example.backend.domain.trade.enums.TradeStatus.SELLING,
                "물고기",
                java.time.LocalDateTime.now()
            );

            // 거래글에 이미지 추가
            trade.addImage(selectedImage);

            tradeRepository.save(trade);
        });
    }

    private void createSecondhandTrades() {
        // 이미 중고물품 거래 데이터가 있는지 확인
        if (tradeRepository.findByBoardType(org.example.backend.domain.trade.enums.BoardType.SECONDHAND, 
                PageRequest.of(0, 1)).getTotalElements() > 0) {
            return;
        }

        String[] secondhandTitles = {
            "어항 세트 급처",
            "물고기 사료 판매",
            "어항 장식품 판매",
            "수질 테스트 키트 판매"
        };

        String[] secondhandContents = {
            "사용하던 어항 세트를 급하게 판매합니다. 상태 양호하고 깨끗해요.",
            "고품질 물고기 사료를 저렴하게 판매합니다. 유통기한도 충분해요.",
            "어항을 예쁘게 꾸밀 수 있는 장식품들을 판매합니다. 다양한 종류 있어요.",
            "수질을 정확히 측정할 수 있는 테스트 키트를 판매합니다. 정확도 높아요."
        };

        // 모든 거래글을 개별적으로 생성하여 완전히 섞기
        createShuffledItems(4, (i, tradeNum, member) -> {
            String title = secondhandTitles[(i + tradeNum - 1) % secondhandTitles.length] + " " + tradeNum;
            String content = secondhandContents[(i + tradeNum - 1) % secondhandContents.length] +
                " (판매자: test" + i + ")";

            // 중고물품 거래는 중고물품 이미지만 사용 (제목 순서에 맞춰서)
            String[] secondhandImages = {
                "https://t1.daumcdn.net/news/202211/30/nongmin/20221130163646676iqlt.png", // 어항 세트
                "https://sitem.ssgcdn.com/88/52/99/item/1000525995288_i1_750.jpg", // 물고기 사료
                "https://thumbnail.coupangcdn.com/thumbnails/remote/492x492ex/image/vendor_inventory/5eed/1ba36cb8138318d15d5db5fccc1bb7693b7de596e033de724cb805e7ba4b.jpg", // 어항 장식품
                "https://asset.m-gs.kr/prod/1050974578/1/550" // 수질 테스트 키트
            };
            
            String selectedImage = secondhandImages[(i + tradeNum - 1) % secondhandImages.length];

            Trade trade = new Trade(
                member,
                org.example.backend.domain.trade.enums.BoardType.SECONDHAND,
                title,
                content,
                10000L + (i * 2000L) + (tradeNum * 1000L), // 중고물품 가격 (10000원부터 시작)
                org.example.backend.domain.trade.enums.TradeStatus.SELLING,
                "중고물품",
                java.time.LocalDateTime.now()
            );

            // 거래글에 이미지 추가
            trade.addImage(selectedImage);

            tradeRepository.save(trade);
        });
    }
}
