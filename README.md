# FoodieBlog API Server

## 1. 프로젝트 개요

FoodieBlog는 전북대학교 웹서비스설계 과목 과제2를 수행한 프로젝트입니다.
FoodieBlog는 맛집블로그를 주제로 백엔드를 구현하여 **맛집 게시글을 작성·조회·관리할 수 있는 블로그 API 서버**이다.
JWT 기반 인증/인가(RBAC)를 적용하여 **블로그 방문자(USER)**와 **블로그 관리자(ADMIN)** 권한을 구분하고,
게시글·댓글·카테고리·통계 기능을 REST API 형태로 제공합니다.

본 프로젝트는 **DB·API 설계를 실제 코드로 구현하고, docker-compose 기반으로 JCloud에 배포**하는 것을 목표로 합니다.

### 주요 기능

* 회원 인증/인가 (JWT Access/Refresh Token)
* 게시글(Post) CRUD + 검색/정렬/페이지네이션
* 댓글(Comment) CRUD
* 카테고리(Category) 관리
* 관리자 전용 통계(Stats) API
* 표준 에러 응답 규격
* Swagger(OpenAPI) 자동 문서화
* Postman 컬렉션 기반 API 테스트
* Health Check API 제공

---

## 2. 실행 방법

### 2-1. 로컬 실행 (Docker Compose)

#### ① 필수 파일 준비

```bash
cp .env.example .env
```

`.env` 파일에 DB 정보 및 JWT Secret을 설정한다.

#### ② Docker 컨테이너 실행

```bash
docker-compose up -d --build
```

#### ③ 서버 확인

* API Base URL: `http://localhost:8080`
* Swagger: `http://localhost:8080/swagger-ui/index.html`
* Health: `http://localhost:8080/health`

---

### 2-2. (참고) 로컬 직접 실행

```bash
./gradlew clean build
gradlew bootRun
```

---

## 3. 환경변수 설명 (`.env.example`)

```env
# Server
SERVER_PORT=8080

# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=foodieblog
DB_USERNAME=foodie
DB_PASSWORD=생략

# JWT
JWT_SECRET=생략
JWT_ACCESS_TOKEN_EXPIRE=3600
JWT_REFRESH_TOKEN_EXPIRE=1209600
```

> ⚠️ `.env` 파일은 **GitHub public repo에 포함되지 않으며**, Classroom으로만 제출한다.

---

## 4. 배포 주소 (JCloud)

* **Base URL**: `http://113.198.66.75:10236/api`
* **Swagger URL**: `http://113.198.66.75:10236/swagger-ui/index.html`
* **Health URL**: `http://113.198.66.75:10236/health`
* **Postman URL**: `https://www.postman.com/esther10777-8409077/foodie-blog-api/collection/vl4wmni/foodieblog-api?action=share&creator=50427518&active-environment=50427518-fabeb94d-a571-4ac9-b7b9-55362474e0e2`

---

## 5. 인증 플로우 설명

1. `POST /auth/login`
   → Access Token + Refresh Token 발급
2. Access Token을 `Authorization: Bearer <token>` 헤더에 포함하여 API 호출
3. Access Token 만료 시
   → `POST /auth/refresh`로 재발급
4. 토큰 위조/만료/권한 부족 시
   → 표준 에러 코드(`TOKEN_EXPIRED`, `FORBIDDEN` 등)로 응답

---

## 6. 역할 / 권한 표 (RBAC)

| API 유형       | ROLE_USER(방문자) | ROLE_ADMIN(블로그 주인) |
| ------------ |----------------|--------------------|
| 게시글 조회       | ✅              | ✅                  |
| 게시글 작성/수정/삭제 | ✅ (본인)         | ✅                  |
| 댓글 CRUD      | ✅              | ✅                  |
| 카테고리 관리      | ❌              | ✅                  |
| 통계 API       | ❌              | ✅                  |
| 사용자 목록 조회    | ❌              | ✅                  |

---

## 7. 예제 계정

| 구분    | 이메일                                         | 비밀번호      | 비고     |
| ----- | ------------------------------------------- |-----------| ------ |
| USER  | [user@example.com](mailto:user@example.com) | user1234  | 일반 사용자 |
| ADMIN | [admin@example.com](mailto:admin@example.com) | admin1234 | 관리자 권한 |

---

## 8. DB 연결 정보 (테스트용)

* **DBMS**: MySQL 8
* **Host**: `<JCloud-IP>` 또는 `localhost`
* **Port**: `3306`
* **Database**: `foodieblog`
* **User**: `foodie`
* **권한**: 테스트/과제용 (DDL/DML 가능)

```bash
mydocker exec -it foodieblog-mysql mysql -ufoodie -pfoodpass foodieblog
```

---

## 9. 엔드포인트 요약표

| Method | URL                  | 설명                   |
| ------ | -------------------- | -------------------- |
| POST   | /auth/login          | 로그인                  |
| POST   | /auth/refresh        | 토큰 재발급               |
| GET    | /users/me            | 내 정보 조회              |
| GET    | /posts               | 게시글 목록(검색/정렬/페이지네이션) |
| POST   | /posts               | 게시글 작성               |
| GET    | /posts/{id}          | 게시글 상세               |
| PATCH  | /posts/{id}          | 게시글 수정               |
| DELETE | /posts/{id}          | 게시글 삭제               |
| GET    | /posts/{id}/comments | 댓글 목록                |
| POST   | /posts/{id}/comments | 댓글 작성                |
| GET    | /categories          | 카테고리 조회              |
| POST   | /categories          | 카테고리 생성 (ADMIN)      |
| GET    | /stats/daily         | 일별 통계 (ADMIN)        |
| GET    | /health              | 헬스체크                 |

> 전체 엔드포인트는 Swagger 문서 참고  
> 
>**Swagger URL**: `http://113.198.66.75:10236/swagger-ui/index.html`

---

## 10. 성능 / 보안 고려사항

* JWT 기반 인증 및 Role 기반 인가(RBAC)
* 비밀번호 해시 처리
* 요청 Rate Limit 필터 적용
* 페이지네이션 기반 목록 조회
* 검색/정렬 대상 컬럼 인덱스 적용
* 전역 예외 처리 및 표준 에러 응답

---

## 11. 한계 및 개선 계획

* Redis 캐시 도입(인기 게시글, 카테고리)
* QueryDSL 적용으로 검색 조건 확장
* 파일 업로드(이미지) 기능 추가
* 모니터링 지표(Metrics) 및 로그 수집 고도화
* CI(GitHub Actions) 자동 테스트/빌드 구성

---
   
