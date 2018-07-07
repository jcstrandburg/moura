package unittests.services

import junit.framework.TestCase
import services.AuthenticationService

class AuthenticationServiceFixture : TestCase() {

    val token = "78eacc80-8184-11e8-b567-0800200c9a66"

    fun testExtractTokenFromHeader() {
        assertEquals(null, AuthenticationService.extractTokenFromHeader("TOOKEN $token"))
        assertEquals(null, AuthenticationService.extractTokenFromHeader("TOKEN"))
        assertEquals(null, AuthenticationService.extractTokenFromHeader("TOKEN "))
        assertEquals(token, AuthenticationService.extractTokenFromHeader("TOKEN $token"))
    }
}
