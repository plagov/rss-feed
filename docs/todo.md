# AI Mentor Prompt

Use this prompt when starting a new session with an AI agent (Junie, Codex, Copilot, etc.) to ensure it behaves as a mentor rather than a coder.

```text
I am working on a Spring Boot RSS Feed project. You are my Senior Backend Mentor. 
I am the lead developer and I will do all the coding. Your role is to:
1. READ the file 'docs/todo.md' to understand the full project roadmap, architecture, and phases of this overhaul.
2. Guide me through the implementation of the tasks listed in the todo.md.
3. Provide conceptual explanations, architectural advice, and small cues.
4. DO NOT provide the full source code for any task unless I explicitly ask for a snippet of a specific complex logic.
5. Help me debug by asking leading questions that lead me to the solution.
6. Ensure I follow best practices for Spring Boot, REST API design, and Security.

We are working on the task: [INSERT TASK NAME HERE]
```

---

# Project Overhaul Roadmap: RSS Feed Backend

## Phase 1: Transition to REST API
Focus on removing the UI layer and standardizing the API.

1. **Task: Identify and Remove Thymeleaf Dependencies** (45 min)
    - Remove `spring-boot-starter-thymeleaf` and any related UI dependencies from `build.gradle.kts`.
    - Cleanup `src/main/resources/static` and `src/main/resources/templates`.
2. **Task: Convert View Controllers to REST Controllers** (60 min)
    - Identify logic in `PostsView`, `BlogsView`, etc., and move it to existing or new `@RestController` classes.
    - Ensure all methods return `ResponseEntity<T>` or DTOs instead of String view names.
3. **Task: Standardize API Response Models** (45 min)
    - Create a consistent `ApiResponse` wrapper if needed or refine DTOs for all endpoints.
    - Ensure `PostResponse` and `BlogResponse` are properly structured for JSON.
    - **Update existing `PostTest.java`** to reflect changes in API response structure.
4. **Task: Configure Jackson and JSON Error Handling** (60 min)
    - Set up a `@ControllerAdvice` to handle exceptions and return meaningful JSON error messages instead of the default Spring error page.
5. **Task: Update Security Configuration for API only** (60 min)
    - Update `SecurityConfig` to remove `.formLogin()` and `.oauth2Login()` (we will replace them in Phase 2).
    - Set up a basic "Permit All" or basic auth temporarily to keep the API functional while testing.
6. **Task: Clean up and Migration of Tests** (60 min)
    - **Delete View-based tests**: Remove `PostViewTest.java` and `BlogsViewTest.java` as they rely on Thymeleaf/Views.
    - **Create new Integration Tests**: Add new REST-based integration tests for the converted endpoints (e.g., `getUnreadPosts`, `subscribeToBlog`).

## Phase 2: User Authentication & Registration
Moving from a single-user GitHub hack to a full multi-user system.

1. **Task: Design User Entity and Repository** (45 min)
    - Create `User` entity (id, username, password, email, role).
    - Create `UserRepository` (JDBCTemplate or JPA based on project style).
2. **Task: Implement User Registration Logic** (60 min)
    - Create `RegistrationController` and `UserService`.
    - Implement password encoding using `BCryptPasswordEncoder`.
3. **Task: Implement JWT or Session-based Authentication** (60 min)
    - Set up authentication filters to issue and verify tokens (or standard sessions adapted for REST).
    - Create a `/login` endpoint that returns a token or sets a session cookie.
4. **Task: Secure All API Endpoints** (60 min)
    - Update `SecurityConfig` to require authentication for all `/api/**` paths.
    - Implement Method Security (e.g., `@PreAuthorize`).
5. **Task: Replace GitHub Hack with User-specific Context** (45 min)
    - Remove `ALLOWED_USER_EMAIL` logic.
    - Update controllers to get the current authenticated user's ID.
6. **Task: Update Security and Auth Tests** (60 min)
    - Update all integration tests to use the new authentication mechanism (e.g., provide JWT or valid session).
    - Add specific tests for the Registration and Login endpoints.

## Phase 3: Data Schema & Multi-tenancy
Ensuring each user has their own state and data.

1. **Task: Database Migration for Users Table** (45 min)
    - Create a Flyway migration to add the `users` table.
2. **Task: Migration for Multi-tenancy (Blogs)** (60 min)
    - Add `user_id` column to the `blogs` table.
    - Create a Flyway migration to update existing records if necessary.
3. **Task: Update DAO Layer for Multi-tenancy** (60 min)
    - Update `BlogDao` and `PostDao` to include `userId` in all queries (SELECT, INSERT, DELETE).
    - Ensure a user can only see their own blogs and posts.
4. **Task: Update Service Layer for User Context** (60 min)
    - Pass the authenticated user's ID from the Controller to the Service and then to the DAO.
5. **Task: Validation and Edge Case Handling** (45 min)
    - Ensure users cannot fetch or modify blogs/posts belonging to other users via ID manipulation.
6. **Task: Verify Multi-tenancy Isolation in Tests** (60 min)
    - Add integration tests that verify User A cannot access or delete User B's data.

## Phase 4: Finalization & Polish
1. **Task: API Documentation with Swagger/OpenAPI** (60 min)
    - Add SpringDoc OpenAPI dependency.
    - Document all REST endpoints.
2. **Task: Refactor PostService for Performance** (60 min)
    - Review `recordLatestBlogPosts` and ensure it's efficient for multiple users.
    - Consider how background tasks will handle user-specific contexts.
3. **Task: Final Project Cleanup** (45 min)
    - Remove any remaining unused files, configs, or comments related to the old UI.
    - Verify all tests pass (and update them for REST/Auth).
