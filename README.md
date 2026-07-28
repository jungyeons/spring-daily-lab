# Spring Daily Lab

매일의 학습·개발 작업을 기록하고 상태와 마감일을 관리하는 Spring Boot REST API입니다. 작은 CRUD 예제에서 시작하지만, 운영 환경에서 필요한 보안·관측성·데이터 품질 기능을 점진적으로 확장할 수 있도록 구성했습니다.

## 기술 구성

- Java 21
- Spring Boot 4.1.0
- Gradle 9.5.1 Wrapper
- Spring Web MVC, Validation, Data JPA, Actuator
- Flyway
- H2(기본 로컬 실행), PostgreSQL(Docker Compose)
- JUnit 5, AssertJ, Spring Boot Test

## 실행

의존 프로그램은 Java 21뿐입니다. 기본 설정은 메모리 H2 데이터베이스를 사용합니다.

```bash
./gradlew bootRun
```

애플리케이션이 실행되면 다음 주소를 사용할 수 있습니다.

- API: `http://localhost:8080/api/v1/tasks`
- 상태 확인: `http://localhost:8080/actuator/health`
- H2 콘솔: `http://localhost:8080/h2-console`

PostgreSQL과 애플리케이션을 함께 실행하려면:

```bash
docker compose up --build
```

Compose는 애플리케이션을 중지할 때 Spring의 20초 graceful shutdown이 끝날 수 있도록 최대 25초를 기다린 뒤 강제 종료합니다.

## API 예시

작업 생성:

```bash
curl -i http://localhost:8080/api/v1/tasks \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "서비스 테스트 추가",
    "description": "상태 전이와 통계를 검증한다",
    "category": "TEST",
    "priority": 4,
    "dueDate": "2030-12-31"
  }'
```

목록과 필터:

```bash
curl 'http://localhost:8080/api/v1/tasks?status=TODO&category=TEST&page=0&size=20'
```

상태 변경:

```bash
curl -X PATCH http://localhost:8080/api/v1/tasks/1/status \
  -H 'Content-Type: application/json' \
  -d '{"status":"DONE"}'
```

요약 통계:

```bash
curl http://localhost:8080/api/v1/tasks/summary
```

## 엔드포인트

| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/api/v1/tasks` | 작업 생성 |
| `GET` | `/api/v1/tasks` | 페이지 목록 및 상태·분류 필터 |
| `GET` | `/api/v1/tasks/{id}` | 단일 작업 조회 |
| `PUT` | `/api/v1/tasks/{id}` | 작업 전체 수정 |
| `PATCH` | `/api/v1/tasks/{id}/status` | 상태 변경 |
| `DELETE` | `/api/v1/tasks/{id}` | 작업 삭제 |
| `GET` | `/api/v1/tasks/summary` | 상태 및 기한 초과 통계 |

잘못된 요청은 RFC 9457 Problem Details 형식으로 응답합니다.

## 검증

```bash
./gradlew check
```

CI도 pull request와 `main` push에서 같은 검사를 실행합니다.

## 데이터베이스 설정

환경 변수로 PostgreSQL 같은 외부 데이터베이스를 연결할 수 있습니다.

| 변수 | 기본값 |
| --- | --- |
| `DB_URL` | `jdbc:h2:mem:dailylab;...` |
| `DB_USERNAME` | `sa` |
| `DB_PASSWORD` | 빈 값 |
| `H2_CONSOLE_ENABLED` | `true` |

스키마는 [Flyway 마이그레이션](src/main/resources/db/migration/V1__create_growth_tasks.sql)으로만 변경합니다.

## 기여 자동화 원칙

매일 자동 작업은 [ROADMAP.md](ROADMAP.md)의 미완료 항목과 현재 이슈·PR을 검토하고, 서로 독립적인 실제 개선을 최대 10개까지 초안 PR로 제안합니다.

- 빈 커밋이나 날짜 변경만 있는 커밋을 만들지 않습니다.
- 한 PR에는 한 가지 설명 가능한 개선만 담습니다.
- 이미 열린 PR과 중복되는 작업을 만들지 않습니다.
- 관련 테스트와 문서를 함께 수정하고 `./gradlew check`를 통과해야 합니다.
- 유효한 개선이 없으면 기여 수를 채우지 않고 종료합니다.

구체적인 작업 규칙은 [AGENTS.md](AGENTS.md)에 있습니다.

## 라이선스

[MIT License](LICENSE)
