# API 오류 코드 카탈로그

Spring Daily Lab API는 실패 응답에 RFC 9457 Problem Details(`application/problem+json`) 형식을 사용합니다. 모든 오류에는 HTTP 상태 코드, 사람이 읽을 수 있는 `title`과 `detail`이 포함됩니다. `instance`는 요청 경로입니다.

```json
{
  "type": "about:blank",
  "title": "Invalid request",
  "status": 400,
  "detail": "Request validation failed",
  "instance": "/api/v1/tasks",
  "errors": {
    "title": "must not be blank"
  }
}
```

| HTTP 상태 | 제목 | 발생 조건 | 클라이언트 처리 |
| --- | --- | --- | --- |
| `400 Bad Request` | `Invalid request` | 요청 본문이 Bean Validation 규칙을 위반함 | `errors` 객체의 필드별 메시지를 사용자에게 보여 주고 입력을 수정한다. |
| `400 Bad Request` | `Unreadable request` | JSON 형식이 잘못되었거나 enum 값이 지원되지 않음 | JSON 구문과 enum 문자열을 확인한 뒤 다시 요청한다. |
| `404 Not Found` | `Task not found` | 경로의 작업 ID가 존재하지 않음 | 목록을 새로고침하거나 사용자가 접근한 링크가 오래되었음을 알린다. |
| `409 Conflict` | `Concurrent update conflict` | 동시에 수정된 작업을 이전 상태로 저장하려 함 | 최신 작업을 다시 조회하고 사용자의 변경을 다시 적용하도록 안내한다. |

## 필드 검증 오류

`Invalid request` 응답의 `errors` 객체는 필드 이름과 첫 번째 검증 메시지를 매핑합니다. 현재 작업 생성·수정 요청에서 다음 조건을 확인합니다.

| 필드 | 조건 |
| --- | --- |
| `title` | 비어 있지 않고 최대 120자 |
| `description` | 지정하면 최대 2,000자 |
| `category` | 지원하는 작업 분류 enum 값 |
| `priority` | 1 이상 5 이하 |
| `dueDate` | ISO-8601 날짜(`YYYY-MM-DD`) 형식이며 오늘 또는 미래 날짜 |

클라이언트는 `detail` 문자열로 분기하지 말고 HTTP 상태 코드와 `title`, 그리고 `errors`의 필드 키를 사용해야 합니다. 문구는 이후에도 사용자 경험을 위해 개선될 수 있습니다.
