# 🐠 우리집 물꼬기 (Our Aquarium)

> **프로그래머스 데브코스 7기 9회차 3팀 3다수 2차 프로젝트**

물고기 키우기와 커뮤니티를 결합한 종합 플랫폼으로, 개인 어항 관리부터 거래, 소통까지 모든 것을 한 곳에서!

---

## 🤝 팀원
| <img src="https://github.com/do04080.png" width="120px;" alt=""/> | <img src="https://github.com/BE9koo.png" width="120px;" alt=""/> | <img src="https://github.com/kimwonmin.png" width="120px;" alt=""/> | <img src="https://github.com/premierbell.png" width="120px;" alt=""/> | <img src="https://github.com/qivvoon.png" width="120px;" alt=""/> | <img src="https://github.com/xoxoisme.png" width="120px;" alt=""/> |
| :----------------------------------------------------: | :----------------------------------------------------: | :----------------------------------------------------: | :----------------------------------------------------: | :----------------------------------------------------: | :----------------------------------------------------: |
|   [도석환](https://github.com/do04080)   |   [구본황](https://github.com/BE9koo)   |   [김원민](https://github.com/kimwonmin)   |   [박종원](https://github.com/premierbell)   |   [강지원](https://github.com/qivvoon)   |   [권태현](https://github.com/xoxoisme)   |
|   팀장, 회원관리   |   커뮤니티 게시판   |   물고기 및 어항 기록, 알림   |   거래 게시판, 이미지 업로드(S3)   |   물고기 및 어항 관리   |   채팅 및 결제   |

---

## 🌟 주요 기능

### 🏠 **어항 관리**
- **개인 어항 생성 및 관리**: 나만의 어항을 만들고 관리
- **물고기 등록**: 어항에 물고기 종류와 이름 등록
- **어항 로그**: 어항 관리 기록 및 알림 기능
- **물고기 이동**: 어항 간 물고기 이동 기능
- **어항 관리 알림**: 어항 관리 일정 알림

### 🛒 **거래 마켓플레이스**
- **물고기 거래**: 물고기 판매 및 구매
- **중고물품 거래**: 어항 관련 중고물품 거래
- **실시간 채팅**: 거래자 간 실시간 소통
- **거래 상태 관리**: 판매중/거래완료 등 상태 관리

### 👥 **커뮤니티**
- **자랑 게시판**: 내 어항과 물고기 자랑
- **질문 게시판**: 어항 관리 관련 질문과 답변
- **댓글 및 좋아요**: 게시글에 댓글 작성 및 좋아요 기능
- **팔로우 시스템**: 다른 사용자 팔로우 및 팔로워 관리

### 💰 **포인트 시스템**
- **포인트 충전**: 현금으로 포인트 충전
- **포인트 거래**: 포인트를 이용한 거래
- **포인트 내역**: 포인트 사용 내역 조회

---

## 🛠️ 기술 스택

<img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"/> <img src="https://img.shields.io/badge/Spring Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white"/>
<img src="https://img.shields.io/badge/Spring Security-6DB33F?style=for-the-badge&logo=spring&logoColor=white"/> <img src="https://img.shields.io/badge/Spring Data JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white"/>
<br/>
<img src="https://img.shields.io/badge/Next.js-000000?style=for-the-badge&logo=nextdotjs&logoColor=white"/> <img src="https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white"/> <img src="https://img.shields.io/badge/Tailwind CSS-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white"/>
<br/>
<img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white"/> <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white"/> <img src="https://img.shields.io/badge/AWS S3-569A31?style=for-the-badge&logo=amazon-aws&logoColor=white"/>
<br/>
<img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white"/> <img src="https://img.shields.io/badge/WebSocket-010101?style=for-the-badge&logo=socketdotio&logoColor=white"/>

---

## 📁 프로젝트 구조

```
NBE7-9-2-Team3/
├── backend/                 # Spring Boot 백엔드
│   ├── src/main/java/org/example/backend/
│   │   ├── domain/         # 도메인별 패키지
│   │   │   ├── aquarium/   # 어항 관리
│   │   │   ├── fish/       # 물고기 관리
│   │   │   ├── member/     # 회원 관리
│   │   │   ├── trade/      # 거래 관리
│   │   │   ├── post/       # 게시글 관리
│   │   │   ├── point/      # 포인트 관리
│   │   │   └── ...
│   │   └── global/         # 공통 설정
│   └── src/main/resources/
│       └── application.yml # 설정 파일
└── frontend/               # Next.js 프론트엔드
    ├── src/
    │   ├── app/           # 페이지 라우팅
    │   ├── components/    # 재사용 컴포넌트
    │   ├── lib/          # 유틸리티 함수
    │   └── type/         # TypeScript 타입 정의
    └── package.json
```
---

## 🗄️ ERD

<img width="2070" height="922" alt="image" src="https://github.com/user-attachments/assets/e90cd0e6-df19-45fb-a99c-c78bc914f720" />

---

## 🚀 시작하기

### Prerequisites
- Java 21+
- Node.js 18+
- MySQL 8.0+
- Gradle 7.0+

### Frontend 실행
```bash
cd frontend
npm install
npm run dev
```

### API 문서
- Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 🎯 핵심 특징

### 🔐 **보안**
- JWT 토큰 기반 인증
- Spring Security를 통한 권한 관리
- 비밀번호 암호화

### 📊 **성능**
- JPA를 활용한 효율적인 데이터 접근
- 인덱스를 통한 쿼리 최적화
- 페이징을 통한 대용량 데이터 처리

### 🔄 **실시간**
- WebSocket을 활용한 실시간 채팅
- 거래자 간 즉시 소통 가능
