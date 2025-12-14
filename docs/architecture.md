
---

# Architecture

## 1. 개요

FoodieBlog는 **맛집 게시글(Post)/댓글(Comment)/카테고리(Category)** 기반의 블로그 API 서버이다.
과제 요구사항에 맞춰 **JWT 기반 인증/인가(RBAC)**, **페이지네이션/검색/정렬**, **일관된 에러 응답 규격**, **Swagger(OpenAPI) 자동 문서화**, **Postman 테스트**, **docker-compose 기반 JCloud 배포**를 포함한다.

* **API Root**: `/api`
* **Swagger UI**: `/swagger-ui/index.html`
* **Health Check**: `GET /health` (인증 없이 200)

---

## 2. 기술 스택

* **Language/Framework**: Java 17, Spring Boot
* **DB**: MySQL 8
* **ORM**: Spring Data JPA (Hibernate)
* **Auth**: JWT (Access/Refresh), Spring Security
* **Docs**: springdoc-openapi (Swagger UI)
* **Deploy**: Docker, docker-compose, JCloud

---

## 3. 전체 구조 (패키지/모듈 & 세부 파일)

프로젝트는 도메인별 패키지 분리를 통해 관심사를 분리하고, 각 도메인에서 **Controller / Service / Repository / Entity / DTO** 역할을 명확히 유지한다.

```text
src/main/java/com/foodieblog
├─ FoodieblogApplication.java
│
├─ auth/                           # 인증 / 인가 (JWT, Security)
│  ├─ dto/
│  ├─ AuthController.java
│  ├─ AuthService.java
│  ├─ JwtAuthFilter.java
│  ├─ JwtProvider.java
│  ├─ RefreshToken.java
│  ├─ RefreshTokenRepository.java
│  ├─ SecurityConfig.java
│  ├─ CorsConfig.java
│  ├─ JacksonConfig.java
│  └─ PasswordDebugConfig.java
│
├─ category/                       # 카테고리 도메인
│  ├─ dto/
│  ├─ Category.java
│  ├─ CategoryController.java
│  ├─ CategoryService.java
│  └─ CategoryRepository.java
│
├─ comment/                        # 댓글 도메인
│  ├─ dto/
│  ├─ Comment.java
│  ├─ CommentController.java
│  ├─ CommentService.java
│  ├─ CommentRepository.java
│  └─ CommentStatus.java
│
├─ common/                         # 공통 모듈
│  ├─ ApiResponse.java
│  ├─ error/
│  │  ├─ BusinessException.java
│  │  ├─ ErrorCode.java
│  │  ├─ ErrorResponse.java
│  │  └─ GlobalExceptionHandler.java
│  └─ ratelimit/
│     └─ RatelimitFilter.java
│
├─ config/                         # 전역 설정
│  └─ SwaggerConfig.java
│
├─ health/                         # 헬스체크
│  ├─ dto/
│  ├─ HealthController.java
│  └─ HealthService.java
│
├─ post/                           # 게시글 도메인
│  ├─ dto/
│  ├─ Post.java
│  ├─ PostController.java
│  ├─ PostService.java
│  ├─ PostRepository.java
│  ├─ PostSpecifications.java
│  └─ PostStatus.java
│
├─ seed/                           # 초기 데이터
│  └─ SeedDataRunner.java
│
├─ stats/                          # 통계/집계
│  ├─ dto/
│  ├─ StatsController.java
│  ├─ StatsService.java
│  └─ StatsRepository.java
│
└─ user/                           # 사용자 도메인
   ├─ dto/
   ├─ User.java
   ├─ UserController.java
   ├─ UserService.java
   └─ UserRepository.java
```

---

## 4. 계층 구조 (Layered Architecture)

프로젝트는 다음과 같은 계층 구조를 따른다.

### 4.1 Controller (Web Layer)

* HTTP 요청을 수신하고 DTO 검증(@Valid), 파라미터 파싱을 수행한다.
* 인증이 필요한 요청은 SecurityContext의 인증 주체를 기반으로 처리한다.
* 비즈니스 로직은 Service로 위임한다.

**예시 패키지/클래스**

* `auth/AuthController`
* `post/PostController`
* `comment/CommentController`
* `category/CategoryController`
* `user/UserController`
* `stats/StatsController`
* `health/HealthController`

### 4.2 Service (Business Layer)

* 트랜잭션 경계 및 비즈니스 규칙을 담당한다.
* 권한 체크(작성자/관리자), 상태 전이(예: 게시글 상태), 도메인 간 조합 로직을 포함한다.
* Repository를 호출하여 데이터를 조회/변경하고, 응답 DTO로 변환한다.

**예시 패키지/클래스**

* `AuthService`, `PostService`, `CommentService`, `CategoryService`, `UserService`, `StatsService`, `HealthService`

### 4.3 Repository (Persistence Layer)

* Spring Data JPA 기반 데이터 접근을 담당한다.
* 페이지네이션/정렬(Pageable/Sort), 검색 조건(Specification 등)을 통해 목록 API 요구사항을 지원한다.

**예시 패키지/클래스**

* `PostRepository`, `CommentRepository`, `CategoryRepository`, `UserRepository`, `RefreshTokenRepository`, `StatsRepository`

### 4.4 Entity / DTO

* Entity는 DB 테이블과 매핑되는 도메인 모델이다.
* DTO는 외부 요청/응답 스키마를 정의하며, Validation 규칙을 포함할 수 있다.

**예시**

* Entity: `Post`, `Comment`, `Category`, `User`, `RefreshToken`
* Enum: `PostStatus`, `CommentStatus`
* DTO 패키지: `*/dto/`

---

## 5. 의존성 규칙 (Dependency Rule)

의존성은 **상위 계층 → 하위 계층** 방향으로만 흐른다.

* Controller → Service → Repository → Entity
* DTO는 Controller/Service에서 사용 가능
* `common`은 전 계층에서 참조 가능하나, `common`이 도메인 패키지에 의존하지 않도록 유지한다.

**금지 예시**

* Repository → Service 참조 ❌
* Entity → DTO/Controller 참조 ❌

---

## 6. 인증/인가 아키텍처 (JWT + Spring Security)

### 6.1 구성 요소

* `JwtAuthFilter`: 요청의 `Authorization: Bearer <token>` 추출 및 검증
* `JwtProvider`: 토큰 생성/검증/클레임 파싱
* `SecurityConfig`: 보안 필터 체인/인가 규칙 설정
* `RefreshToken`, `RefreshTokenRepository`: Refresh Token 저장 및 재발급 근거 관리

### 6.2 처리 흐름

1. 클라이언트가 Access Token을 포함하여 API 요청
2. `JwtAuthFilter`가 토큰을 검증
3. 유효하면 인증 정보를 SecurityContext에 저장
4. Controller/Service에서 인증 주체 기반으로 권한을 확인하고 로직 수행
5. 만료/위조 토큰은 표준 에러 응답으로 처리

---

## 7. 목록 조회(페이지네이션/검색/정렬) 설계

### 7.1 Pagination / Sorting

* 목록 API는 `page`, `size`, `sort=field,DESC|ASC`를 지원한다.
* Spring Data `Pageable` 기반 구현을 전제로 한다.

### 7.2 Search / Filter

* `post/PostSpecifications`를 통해 검색/필터 조건을 조합 가능하도록 설계한다.
* 예시 조건: keyword, categoryId, status, 날짜 범위 등(구현에 맞게 문서화)

---

## 8. 공통 응답/에러 처리

### 8.1 성공 응답

* `common/ApiResponse`로 성공 응답을 통일한다.

```json
{
  "success": true,
  "data": { }
}
```

### 8.2 에러 응답

* `common/error/GlobalExceptionHandler`에서 예외를 표준 포맷으로 매핑한다.
* `BusinessException` + `ErrorCode` 기반으로 프로젝트 내 에러를 일관되게 관리한다.

```json
{
  "timestamp": "2025-12-14T12:34:56Z",
  "path": "/api/posts/1",
  "status": 400,
  "code": "VALIDATION_FAILED",
  "message": "요청 필드 검증에 실패했습니다.",
  "details": { }
}
```

---

## 9. Rate Limit / 보안 기본 설정

* `common/ratelimit/RatelimitFilter`를 통해 기본 요청 제한(또는 인증 없는 엔드포인트 제한)을 적용한다.
* CORS는 `auth/CorsConfig`에서 테스트/배포 환경 요구에 맞춰 설정한다.
* JSON 직렬화 관련 설정은 `auth/JacksonConfig`에서 관리한다.

---

## 10. Health Check

* `health/HealthController`는 인증 없이 200을 반환한다.
* `HealthService`는 버전/빌드시간 등 서버 상태 정보를 제공한다(구현에 맞게 확장 가능).

---

## 11. Seed / Stats 설계

### 11.1 Seed

* `seed/SeedDataRunner`는 초기 데이터 적재를 담당한다.
* 통계/검색/페이지네이션 검증을 위한 충분한 데이터(과제 요구사항)를 제공하는 목적이다.

### 11.2 Stats

* `stats/*`는 집계/통계 API를 제공한다.
* `StatsRepository`는 통계성 쿼리(집계/그룹핑 등)를 위한 전용 접근 계층으로 둔다.
* 관리자 전용 API로 설계할 수 있으며(RBAC), Swagger 및 Postman에서 권한 케이스를 문서화한다.

---

## 12. 배포 아키텍처 (docker-compose + JCloud)

* 본 프로젝트는 `docker-compose`로 **애플리케이션 컨테이너 + MySQL 컨테이너**를 함께 구동한다.
* JCloud 환경의 포트 리다이렉션 규칙에 따라 외부 접근 Base URL을 제공한다.
* 서버 재시작 후에도 `docker compose up -d`로 지속 구동되도록 운영한다.

---

## 13. 문서화 / 테스트

* Swagger(OpenAPI): 요청/응답 스키마 및 대표 에러 응답(401/403/404/422/500 포함) 문서화
* Postman: 환경변수 기반 컬렉션 + 토큰 저장/주입 및 실패 케이스 검증 스크립트 포함
* Automated Tests: 인증/권한/실패 케이스를 포함하여 20개 이상 구성(과제 요구사항)

---
