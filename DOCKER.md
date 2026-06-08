# Docker 실행 가이드

## 사전 준비

Docker Desktop을 실행한 뒤, 백엔드 레포 루트에서 명령어를 실행합니다.

```bash
cd ssiren-backend
```

처음 실행할 때는 예시 환경변수 파일을 복사합니다.

```bash
cp .env.example .env
```

`.env.example`은 공유용 템플릿이므로 실제 값은 `.env`에 채워 넣습니다.

## 실행

```bash
docker compose up --build
```

백그라운드로 실행하려면:

```bash
docker compose up --build -d
```

PostgreSQL은 로컬 개발 환경의 5432 포트 충돌을 피하기 위해 호스트 `15432` 포트로 노출됩니다.
IntelliJ에서 Spring Boot를 로컬로 실행할 때는 백엔드 루트의 `.env`를 자동 로드하며, 기본 접속값은 다음과 같습니다.

## 종료

```bash
docker compose down
```

DB 볼륨까지 삭제해서 완전히 초기화하려면:

```bash
docker compose down -v
```
