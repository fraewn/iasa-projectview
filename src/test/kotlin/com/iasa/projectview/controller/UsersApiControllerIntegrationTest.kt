package com.iasa.projectview.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.iasa.projectview.controller.UsersApiControllerIntegrationTestConfiguration.TestData.TEST_ADMIN_USER
import com.iasa.projectview.controller.UsersApiControllerIntegrationTestConfiguration.TestData.TEST_GUEST_USER
import com.iasa.projectview.controller.UsersApiControllerIntegrationTestConfiguration.TestData.TEST_NON_EXISTING_USER
import com.iasa.projectview.controller.UsersApiControllerIntegrationTestConfiguration.TestData.TEST_REGISTER_DTO
import com.iasa.projectview.persistence.repository.UserRepository
import com.iasa.projectview.util.addSeconds
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.util.*

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UsersApiControllerIntegrationTest(
    @Autowired private val userRepository: UserRepository,
    @Autowired private val environment: Environment
) {

    @Autowired
    private lateinit var mvc: MockMvc

    @BeforeAll
    fun setUp() {
        userRepository.save(TEST_ADMIN_USER)
        userRepository.save(TEST_GUEST_USER)
    }

    @Test
    fun `post to UsersApi with registration credentials returns Created with Location header`() {
        mvc.perform(
            MockMvcRequestBuilders.post(UsersApi.ROUTE).contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(TEST_REGISTER_DTO))
        ).andExpect { result ->
            assertEquals(HttpStatus.CREATED.value(), result.response.status)
            val locationHeader: String? = result.response.getHeader("Location")
            assertTrue(locationHeader?.contains(UsersApi.ROUTE) ?: fail("Location header was not present"))
        }
    }

    @Test
    fun `get to UsersApi without Authorization header returns Unauthorized`() {
        mvc.perform(
            MockMvcRequestBuilders.get(UsersApi.ROUTE).accept(MediaType.APPLICATION_JSON)
        ).andExpect { result ->
            assertEquals(HttpStatus.UNAUTHORIZED.value(), result.response.status)
        }
    }

    @Test
    fun `get to UsersApi with expired Jwt returns Unauthorized`() {
        val expiredJwt = Jwts.builder()
            .setClaims(TEST_ADMIN_USER.jwtPayload)
            .setIssuedAt(Date())
            .setExpiration(Date().addSeconds(-1))
            .signWith(
                Keys.hmacShaKeyFor(
                    environment.getRequiredProperty("application.security.jwt.secret").toByteArray()
                )
            )
            .compact()

        assertThrows(CredentialsExpiredException::class.java) {
            mvc.perform(
                MockMvcRequestBuilders
                    .get(UsersApi.ROUTE)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer $expiredJwt")
            )
        }
    }

    @Test
    fun `get to UsersApi with invalid Jwt signature returns Unauthorized`() {
        val invalidSignatureJwt = Jwts.builder()
            .setClaims(TEST_ADMIN_USER.jwtPayload)
            .setIssuedAt(Date())
            .setExpiration(Date().addSeconds(-1))
            .signWith(
                Keys.hmacShaKeyFor("invalidSecretThatIsDefinitelyLongEnough".toByteArray())
            )
            .compact()

        assertThrows(BadCredentialsException::class.java) {
            mvc.perform(
                MockMvcRequestBuilders
                    .get(UsersApi.ROUTE)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer $invalidSignatureJwt")
            )
        }
    }

    @Test
    fun `get to UsersApi with valid Jwt for non existing user returns Unauthorized`() {
        val validJwtForNonExistingUser = Jwts.builder()
            .setClaims(TEST_NON_EXISTING_USER.jwtPayload)
            .setIssuedAt(Date())
            .setExpiration(
                Date().addSeconds(
                    environment.getRequiredProperty("application.security.jwt.expiration.seconds").toInt()
                )
            )
            .signWith(
                Keys.hmacShaKeyFor(
                    environment.getRequiredProperty("application.security.jwt.secret").toByteArray()
                )
            )
            .compact()

        assertThrows(AuthenticationCredentialsNotFoundException::class.java) {
            mvc.perform(
                MockMvcRequestBuilders
                    .get(UsersApi.ROUTE)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer $validJwtForNonExistingUser")
            )
        }
    }

    @Test
    fun `get to UsersApi with valid Jwt for guest user returns Forbidden`() {
        val validJwtForGuestUser = Jwts.builder()
            .setClaims(TEST_GUEST_USER.jwtPayload)
            .setIssuedAt(Date())
            .setExpiration(
                Date().addSeconds(
                    environment.getRequiredProperty("application.security.jwt.expiration.seconds").toInt()
                )
            )
            .signWith(
                Keys.hmacShaKeyFor(
                    environment.getRequiredProperty("application.security.jwt.secret").toByteArray()
                )
            )
            .compact()

        mvc.perform(
            MockMvcRequestBuilders
                .get(UsersApi.ROUTE)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $validJwtForGuestUser")
        ).andExpect { result ->
            assertEquals(HttpStatus.FORBIDDEN.value(), result.response.status)
        }
    }

    @Test
    fun `get to UsersApi with valid Jwt for admin user returns Ok`() {
        val validJwtForGuestUser = Jwts.builder()
            .setClaims(TEST_ADMIN_USER.jwtPayload)
            .setIssuedAt(Date())
            .setExpiration(
                Date().addSeconds(
                    environment.getRequiredProperty("application.security.jwt.expiration.seconds").toInt()
                )
            )
            .signWith(
                Keys.hmacShaKeyFor(
                    environment.getRequiredProperty("application.security.jwt.secret").toByteArray()
                )
            )
            .compact()

        mvc.perform(
            MockMvcRequestBuilders
                .get(UsersApi.ROUTE)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $validJwtForGuestUser")
        ).andExpect { result ->
            assertEquals(HttpStatus.OK.value(), result.response.status)
        }
    }

    @AfterAll
    fun tearDown() {
        userRepository.deleteAll()
    }
}
