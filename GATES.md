# Gates: Backend and UI integration

OWNS: backend/**, src/**, vite.config.ts, package.json, .gitignore, README.md

Scope: Secure the Java backend, make its checks reproducible, and connect the existing React UI to the real API without discarding the current visual work.

- [x] G1: Backend main sources compile against the checked-in runtime libraries.
  CHECK: $sources = @(Get-ChildItem -LiteralPath "src/main/java" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }); javac -cp "lib/*" -d "target\gate-classes" $sources; Write-Output "BACKEND_COMPILE_PASSED"
  EXPECT: BACKEND_COMPILE_PASSED
  CWD: backend
  EVIDENCE: javac --release 21 compile passed; BACKEND_COMPILE_PASSED.

- [x] G2: Frontend typecheck and production build pass.
  CHECK: npm run build; Write-Output "FRONTEND_BUILD_PASSED"
  EXPECT: FRONTEND_BUILD_PASSED
  EVIDENCE: npm run build passed.

- [x] G3: Frontend lint passes without new errors.
  CHECK: npm run lint; Write-Output "FRONTEND_LINT_PASSED"
  EXPECT: FRONTEND_LINT_PASSED
  EVIDENCE: npm run lint exited 0; existing React warnings remain, with no lint errors.

- [x] G4: Backend test command is runnable and all tests pass.
  CHECK: npm run backend:test; Write-Output "BACKEND_TESTS_PASSED"
  EXPECT: BACKEND_TESTS_PASSED
  EVIDENCE: npm run backend:test passed: 42 tests, 0 failures, 0 errors.

- [x] G5: The browser-facing UI uses the Java API for login, alerts, logs, integrity, patients, simulator actions, and reviewer actions; local simulation remains opt-in only.
  EVIDENCE: src/lib/medsentinelApi.ts is the single API adapter; dashboard/clinical/login calls no longer reference medEngine; frontend build passed; backend authenticated smoke path returned login/me/stats successfully.

- [x] G6: Protected backend routes reject unauthenticated requests, enforce role permissions, and never return password hashes.
  EVIDENCE: backend smoke returned 401 for unauthenticated /api/auth/me; ApiSecurity and PublicUserDto tests passed; admin responses use PublicUserDto.
