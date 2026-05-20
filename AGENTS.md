# Repository Guidelines

## Project Structure & Module Organization

YatIdle is a campus second-hand trading app split into a Spring Boot API and a uni-app/Vue frontend. Backend code lives in `backend/src/main/java/com/yatidle/backend`, organized by role: `controller`, `service`, `mapper`, `entity`, `dto`, `vo`, `common`, `config`, `handler`, and `enums`. Backend configuration is in `backend/src/main/resources/application.properties`, and database setup is in `backend/db/schema.sql`. Frontend code lives in `frontend/`: pages are under `frontend/pages`, reusable UI in `frontend/components`, API wrappers in `frontend/api`, and images/fonts in `frontend/static`. Manual test plans and cases are stored in `test/test-plan` and `test/testcases`.

## Build, Test, and Development Commands

- `cd backend; .\mvnw.cmd spring-boot:run` starts the local API, usually on port `8080`.
- `cd backend; .\mvnw.cmd test` runs the backend test suite.
- `cd backend; .\mvnw.cmd package` compiles, tests, and writes the packaged artifact to `backend/target`.
- Open `frontend/` in HBuilderX or the uni-app CLI workflow to run the mini-program frontend; keep generated `frontend/unpackage` output out of reviews.

Before running backend features locally, create the MySQL schema from `backend/db/schema.sql` and align credentials with `application.properties`.

## Coding Style & Naming Conventions

Use Java 17, Spring Boot 3, MyBatis-Plus, and 4-space indentation in backend code. Name Java classes by role, such as `UserController`, `TradeOrderService`, `ItemMapper`, `LoginDTO`, and `UserVO`. Keep controllers thin and place business rules in services. For frontend files, follow existing Vue single-file component style; page folders use kebab-case names such as `goods-detail` and `my-orders`.

## Testing Guidelines

Backend test dependencies are available through Spring Boot, but the test tree is currently minimal. Add Java tests under `backend/src/test/java/com/yatidle/backend`, mirroring the production package. Name focused tests `*Test` and integration tests `*IntegrationTest`. Update Markdown test cases in `test/testcases` when user flows, request fields, or order states change.

## Commit & Pull Request Guidelines

Recent commits use short Chinese summaries and occasional Conventional Commit prefixes, for example `feat(order): ...` or `feat(order/favorite/chat): ...`. Prefer concise, scoped messages that describe the behavior changed.

Pull requests should include a short summary, test results, database/schema notes, and affected frontend/backend areas. Add screenshots for visible UI changes and request/response examples for API contract changes.

## Security & Configuration Tips

Do not commit real credentials, production JWT secrets, or local database dumps. Keep environment-specific values documented in PR notes or local setup docs, not hard-coded into shared configuration.
