package com.iasa.projectview.security

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.iasa.projectview.config.SecurityConfig
import com.iasa.projectview.controller.UsersApi
import com.iasa.projectview.model.entity.SystemRole
import com.iasa.projectview.model.entity.User
import com.iasa.projectview.model.entity.User.LoginDto
import com.iasa.projectview.persistence.repository.SystemRoleRepository
import com.iasa.projectview.persistence.repository.UserRepository
import com.iasa.projectview.service.UserService
import com.iasa.projectview.util.addSeconds
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.util.*

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AuthenticationIntegrationTest(
    @Autowired private val context: WebApplicationContext,
    @Autowired private val userService: UserService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val systemRoleRepository: SystemRoleRepository,
    @Autowired private val env: Environment
) {
    private val mvc: MockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply<DefaultMockMvcBuilder>(springSecurity())
        .build()

    val testAdminUser = User(
        "admin",
        "password",
        HashSet(listOf(systemRoleRepository.findByName(SystemRole.ADMIN)!!)),
        1,
        isActive = true,
        isLocked = true,
        isExpired = true
    )

    val testGuestUser = User(
        "guest",
        "password",
        HashSet(listOf(systemRoleRepository.findByName(SystemRole.GUEST)!!)),
        2,
        isActive = true,
        isLocked = true,
        isExpired = true
    )

    val testNonExistingUser = User(
        "none",
        "password",
        HashSet(listOf(systemRoleRepository.findByName(SystemRole.GUEST)!!)),
        3,
        isActive = true,
        isLocked = true,
        isExpired = true
    )

    @Test
    fun `post to login route with existing user credentials returns Ok with Authorization header containing Jwt`() {
        val username = "admin"
        val password = "password"
        userService.registerUser(User.RegisterDto(username, password))

        mvc.perform(
            post(SecurityConfig.LOGIN_ROUTE).contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsBytes(LoginDto(username, password)))
        ).andExpect { result ->
            assertEquals(HttpStatus.OK.value(), result.response.status)
            assertNotNull(result.response.getHeader(HttpHeaders.AUTHORIZATION))
            val token = result.response.getHeader(HttpHeaders.AUTHORIZATION)?.replace("Bearer ", "")
            val claims: Claims = Jwts.parserBuilder()
                .setSigningKey(env.getRequiredProperty("application.security.jwt.secret").toByteArray())
                .build()
                .parseClaimsJws(token).body
            val user = userRepository.findOneByUsername(username)!!
            for ((key, value) in user.jwtPayload) {
                assertTrue(claims.containsKey(key))
                assertEquals(value, claims[key])
            }
        }
    }

    @Test
    fun `post to login route with non existing user credentials returns Unauthorized`() {
        mvc.perform(
            post(SecurityConfig.LOGIN_ROUTE).contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(LoginDto("someWrongUsername", "someWrongPassword")))
        ).andExpect { result ->
            assertEquals(HttpStatus.UNAUTHORIZED.value(), result.response.status)
        }
    }

    @Test
    fun `get to protected route without Authorization header returns Unauthorized`() {
        mvc.perform(
            MockMvcRequestBuilders.get(UsersApi.ROUTE).accept(MediaType.APPLICATION_JSON)
        ).andExpect { result ->
            assertEquals(HttpStatus.UNAUTHORIZED.value(), result.response.status)
        }
    }

    @Test
    fun `get to protected route with expired Jwt returns Unauthorized`() {
        val expiredJwt = Jwts.builder()
            .setClaims(testAdminUser.jwtPayload)
            .setIssuedAt(Date())
            .setExpiration(Date().addSeconds(-1))
            .signWith(
                Keys.hmacShaKeyFor(
                    env.getRequiredProperty("application.security.jwt.secret").toByteArray()
                )
            )
            .compact()

        mvc.perform(
            MockMvcRequestBuilders
                .get(UsersApi.ROUTE)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $expiredJwt")
        ).andExpect { result ->
            assertEquals(HttpStatus.UNAUTHORIZED.value(), result.response.status)
        }
    }

    @Test
    fun `get to protected route with invalid Jwt signature returns Unauthorized`() {
        val invalidSignatureJwt = Jwts.builder()
            .setClaims(testAdminUser.jwtPayload)
            .setIssuedAt(Date())
            .setExpiration(Date().addSeconds(-1))
            .signWith(
                Keys.hmacShaKeyFor("invalidSecretThatIsDefinitelyLongEnough".toByteArray())
            )
            .compact()

        mvc.perform(
            MockMvcRequestBuilders
                .get(UsersApi.ROUTE)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $invalidSignatureJwt")
        ).andExpect { result ->
            assertEquals(HttpStatus.UNAUTHORIZED.value(), result.response.status)
        }
    }

    @Test
    fun `get to protected route with valid Jwt for non existing user returns Unauthorized`() {
        val validJwtForNonExistingUser = Jwts.builder()
            .setClaims(testNonExistingUser.jwtPayload)
            .setIssuedAt(Date())
            .setExpiration(
                Date().addSeconds(
                    env.getRequiredProperty("application.security.jwt.expiration.seconds").toInt()
                )
            )
            .signWith(
                Keys.hmacShaKeyFor(
                    env.getRequiredProperty("application.security.jwt.secret").toByteArray()
                )
            )
            .compact()

        mvc.perform(
            MockMvcRequestBuilders
                .get(UsersApi.ROUTE)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $validJwtForNonExistingUser")
        ).andExpect { result ->
            assertEquals(HttpStatus.UNAUTHORIZED.value(), result.response.status)
        }
    }

    @Test
    fun `get to protected route with valid Jwt for guest user returns Forbidden`() {
        userRepository.save(testGuestUser)

        val validJwtForGuestUser = Jwts.builder()
            .setClaims(testGuestUser.jwtPayload)
            .setIssuedAt(Date())
            .setExpiration(
                Date().addSeconds(
                    env.getRequiredProperty("application.security.jwt.expiration.seconds").toInt()
                )
            )
            .signWith(
                Keys.hmacShaKeyFor(
                    env.getRequiredProperty("application.security.jwt.secret").toByteArray()
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
    fun `get to protected route with valid Jwt for admin user returns Ok`() {
        userRepository.save(testAdminUser)

        val validJwtForAdminUser = Jwts.builder()
            .setClaims(testAdminUser.jwtPayload)
            .setIssuedAt(Date())
            .setExpiration(
                Date().addSeconds(
                    env.getRequiredProperty("application.security.jwt.expiration.seconds").toInt()
                )
            )
            .signWith(
                Keys.hmacShaKeyFor(
                    env.getRequiredProperty("application.security.jwt.secret").toByteArray()
                )
            )
            .compact()

        mvc.perform(
            MockMvcRequestBuilders
                .get(UsersApi.ROUTE)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $validJwtForAdminUser")
        ).andExpect { result ->
            assertEquals(HttpStatus.OK.value(), result.response.status)
        }
    }

    @Test
    fun `get to protected route with valid Jwt but without Bearer prefix returns Unauthorized`() {
        val validJwtForAdminUser = Jwts.builder()
            .setClaims(testAdminUser.jwtPayload)
            .setIssuedAt(Date())
            .setExpiration(
                Date().addSeconds(
                    env.getRequiredProperty("application.security.jwt.expiration.seconds").toInt()
                )
            )
            .signWith(
                Keys.hmacShaKeyFor(
                    env.getRequiredProperty("application.security.jwt.secret").toByteArray()
                )
            )
            .compact()

        mvc.perform(
            MockMvcRequestBuilders
                .get(UsersApi.ROUTE)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", validJwtForAdminUser)
        ).andExpect { result ->
            assertEquals(HttpStatus.UNAUTHORIZED.value(), result.response.status)
        }
    }

    @AfterEach
    fun teardown() {
        userRepository.deleteAll()
    }
}
