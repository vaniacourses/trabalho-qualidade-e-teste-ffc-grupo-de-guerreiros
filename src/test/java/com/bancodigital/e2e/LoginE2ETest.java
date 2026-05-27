package com.bancodigital.e2e;

// Placeholder for login flow E2E coverage — to be implemented by whoever owns the login tests.
//
// Suggested scenarios:
//
//   loginWithValidCredentialsSucceeds:
//     Call seedDefaultUser(), use loginAs(), verify getCurrentUrl() contains "/dashboard".
//
//   loginWithInvalidCredentialsFails:
//     Call tryLogin() with wrong password. Expect error message "E-mail ou senha inválidos."
//     and URL still containing "/login". Use tryLogin() — not loginAs() — since loginAs()
//     is a setup helper that assumes success and waits for /dashboard.
//
//   loginWithUnregisteredEmailFails:
//     Call tryLogin() with a non-existent email. Spring Security does not distinguish
//     "email not found" from "wrong password" — expect the same error message.
//
//   unauthenticatedAccessToProtectedRouteRedirects:
//     Navigate directly to "/dashboard" without authenticating and verify redirect to "/login".
