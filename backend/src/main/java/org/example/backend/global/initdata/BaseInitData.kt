package org.example.backend.global.initdata

import org.example.backend.domain.aquarium.entity.Aquarium
import org.example.backend.domain.aquarium.repository.AquariumRepository
import org.example.backend.domain.fish.entity.Fish
import org.example.backend.domain.fish.repository.FishRepository
import org.example.backend.domain.follow.repository.FollowRepository
import org.example.backend.domain.follow.service.FollowService
import org.example.backend.domain.member.entity.Member
import org.example.backend.domain.member.repository.MemberRepository
import org.example.backend.domain.post.dto.PostWriteRequestDto
import org.example.backend.domain.post.entity.Post
import org.example.backend.domain.post.entity.PostImage
import org.example.backend.domain.post.repository.PostRepository
import org.example.backend.domain.trade.entity.Trade
import org.example.backend.domain.trade.enums.BoardType
import org.example.backend.domain.trade.enums.TradeStatus
import org.example.backend.domain.trade.repository.TradeRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Configuration
@Profile("init-data") // init-data 프로파일이 활성화될 때만 실행
class BaseInitData(
    private val memberRepository: MemberRepository,
    private val followRepository: FollowRepository,
    private val followService: FollowService,
    private val aquariumRepository: AquariumRepository,
    private val fishRepository: FishRepository,
    private val postRepository: PostRepository,
    private val tradeRepository: TradeRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Bean
    @Transactional
    fun initBaseData(): CommandLineRunner {
        return CommandLineRunner { args: Array<String?>? ->
            // 테스트 유저 데이터 생성
            createTestUsers()

            // 팔로우 관계 생성
            createFollowRelationships()

            // 어항 및 물고기 데이터 생성
            createAquariumsAndFish()

            // 게시글 데이터 생성
            createShowoffPosts()
            createQuestionPosts()

            // 거래 게시글 데이터 생성
            createFishTrades()
            createSecondhandTrades()
        }
    }

    // 글 생성 순서 섞기
    private fun createShuffledItems(itemsPerUser: Int, creator: ItemCreator) {
        val allItems: MutableList<Int?> = ArrayList<Int?>()
        for (i in 1..10) {
            for (itemNum in 1..itemsPerUser) {
                allItems.add(i * 10 + itemNum) // 유저ID * 10 + 아이템번호로 고유 식별
            }
        }
        Collections.shuffle(allItems) // 모든 아이템 순서를 완전히 섞기

        for (itemIndex in allItems.indices) {
            val itemId: Int = allItems.get(itemIndex)!!
            val i = itemId / 10 // 유저 ID
            val itemNum = itemId % 10 // 아이템 번호

            val member = memberRepository.findByMemberId(i.toLong())
            if (member == null) {
                continue
            }

            creator.create(i, itemNum, member)
        }
    }

    private fun interface ItemCreator {
        fun create(userId: Int, itemNum: Int, member: Member?)
    }

    private fun createTestUsers() {
        // 이미 데이터가 있는지 확인
        if (memberRepository.count() > 0) {
            return
        }

        for (i in 1..10) {
            val email = "test" + i + "@test.com"
            val nickname = "test" + i
            val password = "test1234"

            // 이미 존재하는지 확인
            if (memberRepository.findByEmail(email) != null) {
                continue
            }

            val member = Member(
                email = email,
                password = passwordEncoder.encode(password),
                nickname = nickname,
                profileImage = "https://upload.wikimedia.org/wikipedia/commons/7/75/%EC%82%AC%EB%9E%8C.png"
            )

            memberRepository.save(member)
        }
    }

    private fun createFollowRelationships() {
        // 이미 팔로우 관계가 있는지 확인
        if (followRepository.count() > 0) {
            return
        }

        // test1부터 test10까지의 유저들을 조회
        for (i in 1..10) {
            val follower = memberRepository.findByMemberId(i.toLong())

            if (follower == null) {
                continue
            }

            // 각 유저가 다음 번호 유저 2명을 팔로우
            for (j in 1..2) {
                val followeeNumber = (i + j - 1) % 10 + 1 // 순환 로직
                val followee =
                    memberRepository.findByMemberId(followeeNumber.toLong())

                if (followee != null && follower.memberId != followee.memberId) {
                    try {
                        followService.follow(follower.memberId!!, followee.memberId!!)
                    } catch (e: Exception) {
                        // 이미 팔로우 관계가 있거나 다른 예외가 발생한 경우 무시하고 계속 진행
                        // (초기 데이터 생성이므로 예외 발생 시 로그만 남기고 계속 진행)
                    }
                }
            }
        }
    }

    private fun createAquariumsAndFish() {
        // 이미 어항 데이터가 있는지 확인
        if (aquariumRepository.count() > 0) {
            return
        }

        val fishSpecies = arrayOf<String>(
            "금붕어",
            "구피",
            "네온테트라",
            "베타",
            "앵거피시",
            "플래티",
            "몰리",
            "다니오",
            "라스보라",
            "카디널테트라"
        )
        val fishNames =
            arrayOf<String>("아름이", "예쁜이", "귀여니", "멋쟁이", "똑똑이", "영리이", "발랄이", "활발이", "사랑이", "행복이")

        // test1부터 test10까지의 유저들에 대해 어항과 물고기 생성
        for (i in 1..10) {
            val member = memberRepository.findByMemberId(i.toLong())

            if (member == null) {
                continue
            }

            // 각 유저당 2개의 어항 생성
            for (aquariumNum in 1..2) {
                val aquariumName = "test" + i + "의 어항 " + aquariumNum
                val aquarium = Aquarium(member, aquariumName)
                val savedAquarium = aquariumRepository.save(aquarium)

                // 각 어항에 3마리의 물고기 생성
                for (fishNum in 1..3) {
                    val species = fishSpecies[(i + fishNum - 1) % fishSpecies.size]
                    val name = fishNames[(i + fishNum - 1) % fishNames.size] + fishNum

                    val fish = Fish(savedAquarium, species, name)
                    fishRepository.save(fish)
                }
            }
        }
    }

    private fun createShowoffPosts() {
        // 이미 자랑게시판 데이터가 있는지 확인 (SHOWOFF 타입만 체크)
        if (postRepository.findByBoardType(Post.BoardType.SHOWOFF).isNotEmpty()) {
            return
        }

        val showoffTitles = arrayOf<String?>(
            "우리 집 금붕어 자랑해요!",
            "새로 만든 어항 세팅",
            "물고기들이 너무 귀여워요",
            "어항 장식 완성!",
            "건강한 물고기들"
        )

        val showoffContents = arrayOf<String?>(
            "우리 집 금붕어가 정말 건강하게 잘 자라고 있어요! 물을 깨끗하게 관리하고 있어서 색깔도 예쁘고 활발해요.",
            "새로운 어항을 세팅했는데 물고기들이 정말 좋아하네요. 필터와 히터도 새로 설치해서 최적의 환경을 만들어줬어요.",
            "우리 물고기들이 너무 귀여워서 매일 보는 재미가 있어요. 먹이를 줄 때마다 달려와서 정말 사랑스러워요.",
            "어항 장식을 새로 해봤는데 물고기들이 더 예뻐 보여요. 산호와 돌로 자연스러운 환경을 만들어줬어요.",
            "우리 물고기들이 정말 건강해요. 수질 관리도 잘하고 있고, 먹이도 적당히 주고 있어서 활발하게 헤엄치고 있어요."
        )

        // 모든 게시글을 개별적으로 생성하여 완전히 섞기
        createShuffledItems(5, ItemCreator { i: Int, postNum: Int, member: Member? ->
            val title = showoffTitles[(i + postNum - 1) % showoffTitles.size] + " " + postNum
            val content = showoffContents[(i + postNum - 1) % showoffContents.size] +
                    " (작성자: test" + i + ")"

            // 자랑게시판용 이미지들 (물고기/어항 관련)
            val showoffImages = arrayOf<String>(
                "https://images.unsplash.com/photo-1535591273668-578e31182c4f?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NHx8JUVCJUFDJUJDJUVBJUIzJUEwJUVBJUI4JUIwfGVufDB8fDB8fHww&fm=jpg&q=60&w=3000",
                "https://t1.daumcdn.net/news/202211/30/nongmin/20221130163646676iqlt.png",
                "https://marketplace.canva.com/XdJTM/MAGyo8XdJTM/1/tl/canva-adorable-cartoon-blue-fish-illustration-MAGyo8XdJTM.png",
                "https://thumbnail.coupangcdn.com/thumbnails/remote/492x492ex/image/vendor_inventory/5eed/1ba36cb8138318d15d5db5fccc1bb7693b7de596e033de724cb805e7ba4b.jpg",
                "https://raraaqua.com/web/product/medium/202508/6101dc2e316a22869d4a905629f97970.jpg"
            )

            val selectedImage = showoffImages[(i + postNum - 1) % showoffImages.size]

            // PostWriteRequestDto를 사용하여 Post 생성
            val requestDto = PostWriteRequestDto(
                title,
                content,
                Post.BoardType.SHOWOFF,
                listOf(selectedImage),  // 자랑게시판은 이미지 필수
                null
            )

            val post = Post(requestDto, member)

            // 게시글에 이미지 추가
            requestDto.imageUrls?.forEach { url ->
                post.addImage(
                    PostImage(
                        url,
                        post
                    )
                )
            }
            postRepository.save(post)
        })
    }

    private fun createQuestionPosts() {
        // 이미 질문게시판 데이터가 있는지 확인 (QUESTION 타입만 체크)
        if (!postRepository.findByBoardType(Post.BoardType.QUESTION).isEmpty()) {
            return
        }

        val questionTitles = arrayOf<String?>(
            "물고기가 잘 안 먹어요",
            "어항 수질 관리 질문",
            "물고기 질병 증상이 뭔가요?",
            "어항 크기 추천해주세요",
            "물고기 키우기 초보 질문"
        )

        val questionContents = arrayOf<String?>(
            "물고기가 최근에 먹이를 잘 안 먹는데 왜 그런 건가요? 수질은 깨끗한 것 같은데...",
            "어항 수질 관리를 어떻게 해야 할지 모르겠어요. 필터 교체 주기나 물갈이 주기를 알려주세요.",
            "우리 물고기 몸에 하얀 점들이 생겼는데 이게 질병인가요? 어떻게 치료해야 할까요?",
            "물고기 3마리 키우려는데 어항 크기는 얼마나 큰 게 좋을까요? 추천해주세요.",
            "물고기 키우기를 처음 시작하는데 어떤 장비들이 필요한지, 초보자 팁을 알려주세요."
        )

        // 모든 게시글을 개별적으로 생성하여 완전히 섞기
        createShuffledItems(5, ItemCreator { i: Int, postNum: Int, member: Member? ->
            val title = questionTitles[(i + postNum - 1) % questionTitles.size] + " " + postNum
            val content = questionContents[(i + postNum - 1) % questionContents.size] +
                    " (작성자: test" + i + ")"

            // PostWriteRequestDto를 사용하여 Post 생성 (질문게시판은 이미지 없음)
            val requestDto = PostWriteRequestDto(
                title,
                content,
                Post.BoardType.QUESTION,
                null,  // 질문게시판은 이미지 없음
                null
            )

            val post = Post(requestDto, member)
            postRepository.save(post)
        })
    }

    private fun createFishTrades() {
        // 이미 물고기 거래 데이터가 있는지 확인 (FISH 타입만 체크)
        if (tradeRepository.findByBoardType(
                BoardType.FISH,
                PageRequest.of(0, 1)
            ).getTotalElements() > 0
        ) {
            return
        }

        val fishTitles = arrayOf<String?>(
            "건강한 금붕어 판매",
            "구피 새끼 판매합니다",
            "베타 물고기 판매",
            "네온테트라 판매",
            "앵거피시 판매"
        )

        val fishContents = arrayOf<String?>(
            "건강한 금붕어를 판매합니다. 초보자도 키우기 쉽고 색깔도 예뻐요.",
            "구피 새끼들을 판매합니다. 부모 물고기도 건강하고 새끼들도 활발해요.",
            "아름다운 베타 물고기를 판매합니다. 색깔이 정말 예쁘고 건강해요.",
            "네온테트라를 판매합니다. 작고 귀여운 물고기로 어항에 잘 어울려요.",
            "앵거피시를 판매합니다. 독특한 모양의 물고기로 관상용으로 좋아요."
        )

        // 모든 거래글을 개별적으로 생성하여 완전히 섞기
        createShuffledItems(2, ItemCreator { i: Int, tradeNum: Int, member: Member? ->
            val title = fishTitles[(i + tradeNum - 1) % fishTitles.size] + " " + tradeNum
            val content = fishContents[(i + tradeNum - 1) % fishContents.size] +
                    " (판매자: test" + i + ")"

            // 물고기 거래는 물고기 이미지만 사용
            val fishImages = arrayOf<String>(
                "https://images.unsplash.com/photo-1535591273668-578e31182c4f?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NHx8JUVCJUFDJUJDJUVBJUIzJUEwJUVBJUI4JUIwfGVufDB8fDB8fHww&fm=jpg&q=60&w=3000",
                "https://marketplace.canva.com/XdJTM/MAGyo8XdJTM/1/tl/canva-adorable-cartoon-blue-fish-illustration-MAGyo8XdJTM.png"
            )

            val selectedImage = fishImages[(i + tradeNum - 1) % fishImages.size]

            val trade = Trade(
                member!!,
                BoardType.FISH,
                title,
                content,
                5000L + (i * 1000L) + (tradeNum * 500L),  // 물고기 가격 (5000원부터 시작)
                TradeStatus.SELLING,
                "물고기",
                LocalDateTime.now()
            )

            // 거래글에 이미지 추가
            trade.addImage(selectedImage)
            tradeRepository.save(trade)
        })
    }

    private fun createSecondhandTrades() {
        // 이미 중고물품 거래 데이터가 있는지 확인
        if (tradeRepository.findByBoardType(
                BoardType.SECONDHAND,
                PageRequest.of(0, 1)
            ).getTotalElements() > 0
        ) {
            return
        }

        val secondhandTitles = arrayOf<String?>(
            "어항 세트 급처",
            "물고기 사료 판매",
            "어항 장식품 판매",
            "수질 테스트 키트 판매"
        )

        val secondhandContents = arrayOf<String?>(
            "사용하던 어항 세트를 급하게 판매합니다. 상태 양호하고 깨끗해요.",
            "고품질 물고기 사료를 저렴하게 판매합니다. 유통기한도 충분해요.",
            "어항을 예쁘게 꾸밀 수 있는 장식품들을 판매합니다. 다양한 종류 있어요.",
            "수질을 정확히 측정할 수 있는 테스트 키트를 판매합니다. 정확도 높아요."
        )

        // 모든 거래글을 개별적으로 생성하여 완전히 섞기
        createShuffledItems(4, ItemCreator { i: Int, tradeNum: Int, member: Member? ->
            val title =
                secondhandTitles[(i + tradeNum - 1) % secondhandTitles.size] + " " + tradeNum
            val content = secondhandContents[(i + tradeNum - 1) % secondhandContents.size] +
                    " (판매자: test" + i + ")"

            // 중고물품 거래는 중고물품 이미지만 사용 (제목 순서에 맞춰서)
            val secondhandImages = arrayOf<String>(
                "https://t1.daumcdn.net/news/202211/30/nongmin/20221130163646676iqlt.png",  // 어항 세트
                "https://sitem.ssgcdn.com/88/52/99/item/1000525995288_i1_750.jpg",  // 물고기 사료
                "https://thumbnail.coupangcdn.com/thumbnails/remote/492x492ex/image/vendor_inventory/5eed/1ba36cb8138318d15d5db5fccc1bb7693b7de596e033de724cb805e7ba4b.jpg",  // 어항 장식품
                "https://asset.m-gs.kr/prod/1050974578/1/550" // 수질 테스트 키트
            )

            val selectedImage = secondhandImages[(i + tradeNum - 1) % secondhandImages.size]

            val trade = Trade(
                member!!,
                BoardType.SECONDHAND,
                title,
                content,
                10000L + (i * 2000L) + (tradeNum * 1000L),  // 중고물품 가격 (10000원부터 시작)
                TradeStatus.SELLING,
                "중고물품",
                LocalDateTime.now()
            )

            // 거래글에 이미지 추가
            trade.addImage(selectedImage)
            tradeRepository.save(trade)
        })
    }
}
