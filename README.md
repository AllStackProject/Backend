<div align="center">
  <h1>Privideo Backend</h1>
  <h3>조직형 프라이빗 영상 공유 플랫폼 API 서버</h3>

  <img width="70%" alt="banner" src="https://github.com/user-attachments/assets/a93a8d41-de28-4b55-b559-1ecd48f29c9e" />

  <br/><br/>

  <p>
    <img src="https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white"/>
    <img src="https://img.shields.io/badge/Spring_Boot-3.5.6-6DB33F?style=flat-square&logo=springboot&logoColor=white"/>
    <img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white"/>
    <img src="https://img.shields.io/badge/JPA_%2B_QueryDSL-59666C?style=flat-square&logo=hibernate&logoColor=white"/>
  </p>

  <p>
    <img src="https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white"/>
    <img src="https://img.shields.io/badge/MongoDB-47A248?style=flat-square&logo=mongodb&logoColor=white"/>
    <img src="https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white"/>
    <img src="https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white"/>
    <img src="https://img.shields.io/badge/JUnit_5-25A162?style=flat-square&logo=junit5&logoColor=white"/>
    <img src="https://img.shields.io/badge/Spring_AI-6DB33F?style=flat-square&logo=spring&logoColor=white"/>
    <img src="https://img.shields.io/badge/springdoc-85EA2D?style=flat-square&logo=swagger&logoColor=white"/>
  </p>

</div>

<br/>

---

## 주요 기능

- 👥 **조직 / 멤버 관리**  
  - 조직 생성 및 **초대 코드** 기반 가입
  - 역할 기반 권한(관리자 / 일반 멤버)
  - 조직 내 멤버 가입/탈퇴, 상태 관리(활성/비활성)

- 🎬 **영상 업로드 & 스트리밍**  
  - 클라이언트가 S3에 영상을 업로드하기 위한 **Presigned URL 발급**
  - HLS 인코딩 결과에 대한 **HLS Prefix 관리**
  - CloudFront + **Signed Cookie**를 활용한 보안 스트리밍
  - 영상 공개 범위(전체 공개 / 멤버그룹 제한) 및 만료일 설정

- 📈 **시청 이력 & 구간별 분석**  
  - Redis 기반 시청 세션 관리 (watchSegments, recentPosition 등)
  - 세션 종료 시 종료 시점 및 이탈 관련 정보에 대해 요약 저장
  - MongoDB를 활용한 구간별(세그먼트) 시청 분석 데이터 집계 : 구간별 조회수 및 이탈수

- 🤖 **AI 분석 제공 (Spring AI / Vertex AI Gemini)**
  - S3의 원본 영상으로부터 RTZR API를 통해 STT 처리
  - 추출한 텍스트에 대해 요약, 퀴즈, 피드백 결과 생성
  - 생성 결과를 저장해 각 영상의 시청자에게 제공

- 🧩 **관리자 대시보드**  
  - 조직별 영상별 시청 지표(완료율, 이탈 구간, 연령/성별 분포 등)
  - 필터(기간, 그룹, 카테고리) 기반 조회

---

## 🏗 아키텍처

<div align="center">
  <img width="100%" alt="privideo-아키텍처" src="https://github.com/user-attachments/assets/41c73cac-c886-4456-84f4-c5df022dfe2e" />
</div>

- Route53 / Ingress를 통해 `privideo-backend`로 요청 유입
- Spring Security + JWT 기반 인증/인가
- JPA + QueryDSL로 RDB(PostgreSQL) 도메인 모델 관리
- MongoDB / Redis를 통해 대용량 시청 로그, 세션 정보 관리
- S3 Presigned URL & CloudFront Signed Cookie를 이용한 안전하고 효율적인 HLS 스트리밍 연동

---

## 🔖 ERD

<img width="100%" alt="FISA" src="https://github.com/user-attachments/assets/f12ae76d-2c93-4d14-8ced-8c6e5e66b185" />

<!--
> RDB에는 사용자/조직/영상/권한/시청 이력 등 **정합성이 중요한 데이터**,  
> MongoDB에는 **세그먼트 단위의 분석용 로그**,  
> Redis에는 **실시간 세션 상태**를 분리해서 저장합니다.
-->

---

## 🗂 Repository 구조

> 도메인 단위로 나누고, 각 도메인 하위에 `controller / service / repository / dto / entity`를 두는 형태

```
privideo-backend
├── src
│   ├── main
│   │   ├── java
│   │   │   └── app.allstackproject.privideo
│   │   │       ├── PrivideoApplication.java
│   │   │       ├── domain
│   │   │       │   ├── admin
│   │   │       │   ├── comment
│   │   │       │   ├── history
│   │   │       │   ├── home
│   │   │       │   ├── member
│   │   │       │   ├── notice
│   │   │       │   ├── organization
│   │   │       │   ├── quiz
│   │   │       │   ├── scrap
│   │   │       │   ├── user
│   │   │       │   └── video
│   │   │       ├── global
│   │   │       │   ├── config        
│   │   │       │   ├── exception
│   │   │       │   ├── response
│   │   │       │   ├── security
│   │   │       │   ├── util
│   │   │       │   └── validator
│   │   │       └── shared
│   │   │           ├── entity       
│   │   │           └── enums        
│   │   └── resources
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       └── lua/
│   └── test
│       ├── java
│       │   └── app.allstackproject.privideo
│       │       ├── PrivideoApplicationTests.java
│       │       └── domain
│       │           ├── admin
│       │           ├── comment
│       │           ├── history
│       │           ├── home
│       │           ├── organization
│       │           ├── scrap
│       │           ├── user
│       │           └── video
│       └── resources
│           ├── summarySample.txt
│           ├── quizSample.txt
└──         └── feedbackSample.txt

```

