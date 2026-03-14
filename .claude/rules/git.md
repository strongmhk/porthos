# Git 규칙

## 브랜치 전략

- `develop` — 메인 개발 브랜치 (PR 대상)
- `feat-{기능명}` — 새 기능 개발
- `fix-{버그명}` — 버그 수정
- `refactor-{대상}` — 리팩토링
- `chore-{작업명}` — 빌드/설정 변경

## 커밋 메시지 형식

```
#{이슈번호} {이모지} {타입}: {내용}
```

### 이모지 및 타입

| 이모지 | 타입 | 용도 |
|--------|------|------|
| ✨ `:sparkles:` | feat | 새로운 기능 추가 |
| 🐛 `:bug:` | fix | 버그 수정 |
| ♻️ `:recycle:` | refactor | 코드 리팩토링 |
| 🔥 `:fire:` | remove | 코드/파일 삭제 |
| 📝 `:memo:` | docs | 문서 작성/수정 |
| 🎨 `:art:` | style | 코드 포맷팅 |
| ✅ `:white_check_mark:` | test | 테스트 추가/수정 |
| 🔧 `:wrench:` | chore | 빌드/설정 변경 |
| 🚀 `:rocket:` | deploy | 배포 관련 |

### 예시

```
#74 :sparkles: feat: 장애 관리 댓글 기능 구현
#74 :recycle: refactor: 불필요한 readComment API 삭제
#75 :bug: fix: JWT 토큰 만료 처리 오류 수정
```

## PR 규칙

- PR 대상 브랜치: `develop`
- PR 템플릿 사용 (`.github/PULL_REQUEST_TEMPLATE.md`)
- PR 유형 체크박스 선택 필수:
  - 새로운 기능 추가
  - 버그 수정
  - 코드 리팩토링
  - 기타

## 이슈 연동

- 브랜치명과 커밋에 이슈 번호(`#{번호}`) 포함
- 이슈 템플릿: `.github/ISSUE_TEMPLATE/`
  - `BUG_REPORT.md`
  - `FEATURE_REQUEST.md`
