# Repository Guidelines

## Project Structure & Module Organization

This repository contains the YatIdle Spring Boot backend. Application code lives under `src/main/java/com/yatidle/backend`, with `BackendApplication.java` as the entry point. Packages follow the current layered layout: `controller` for REST endpoints, `service` for business logic, `mapper` for MyBatis-Plus database access, `entity` for persisted models, `dto` for request payloads, `vo` for response objects, and `common`, `config`, `handler`, and `enums` for shared infrastructure. Runtime configuration is in `src/main/resources/application.properties`. Database schema setup is kept in `db/schema.sql`. Build output under `target/` is generated and should not be edited.

## Build, Test, and Development Commands

Use the Maven wrapper when possible so contributors use the project Maven version:

- `.\mvnw.cmd spring-boot:run` starts the API locally on the configured `server.port` (`8080` by default).
- `.\mvnw.cmd test` runs the test suite.
- `.\mvnw.cmd package` compiles, tests, and creates the packaged artifact under `target/`.
- `.\mvnw.cmd clean` removes generated build output.

The app expects a MySQL database matching `spring.datasource.*` in `application.properties`; initialize local tables with `db/schema.sql`.

## Coding Style & Naming Conventions

Use Java 17 and the existing Spring Boot/MyBatis-Plus style. Keep indentation at 4 spaces. Name classes by role, for example `UserController`, `TradeOrderService`, `ItemMapper`, `LoginDTO`, and `UserVO`. Keep REST controllers thin; place validation, state transitions, and database coordination in services. Prefer constructor injection for Spring beans. Use Lombok only where it already fits the local model style.

## Testing Guidelines

Spring Boot test support is declared in `pom.xml`, but no test tree is currently present. Add tests under `src/test/java/com/yatidle/backend`, mirroring the package under test. Name unit tests `*Test` and broader Spring integration tests `*IntegrationTest`. Run `.\mvnw.cmd test` before submitting changes. For service or controller changes, cover success paths and important failure cases such as missing users, unauthorized access, and invalid order state transitions.

## Commit & Pull Request Guidelines

Recent history uses short feature summaries, sometimes with conventional prefixes such as `feat(chat): 实现聊天功能模块`, plus merge commits from feature branches. Prefer concise messages in the form `feat(scope): summary`, `fix(scope): summary`, or a clear Chinese summary when matching team practice.

Pull requests should describe the behavior changed, list test results, mention database/schema changes, and link related issues. Include request/response examples or screenshots when API behavior affects frontend integration.

## Security & Configuration Tips

Do not commit real secrets. Move local database credentials and JWT secrets out of committed config before production use, and document required environment-specific values in the PR.
