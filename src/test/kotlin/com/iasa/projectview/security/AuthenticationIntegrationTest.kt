package com.iasa.projectview.security

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.iasa.projectview.config.AuthTestConfiguration.TestData.TEST_LOGIN_DTO
import com.iasa.projectview.config.AuthTestConfiguration.TestData.TEST_REGISTER_DTO
import com.iasa.projectview.model.entity.User.LoginDto
import com.iasa.projectview.persistence.repository.UserRepository
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation::class)
internal class AuthenticationIntegrationTest(
    @Autowired private val context: WebApplicationContext,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val env: Environment
) {
    private lateinit var mvc: MockMvc

    @BeforeAll
    fun setup() {
        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @Test
    @Order(1)
    fun `api users with registration credentials returns Created with Location header`() {
        mvc.perform(
            post("/api/users").contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(TEST_REGISTER_DTO))
        ).andExpect { result ->
            assertEquals(HttpStatus.CREATED.value(), result.response.status)
            assertNotNull(result.response.headerNames.contains("Location"))
        }
    }

    @Test
    @Order(2)
    fun `api login with existing user credentials returns Authorization header with JWT`() {
        mvc.perform(
            post("/api/login").contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(TEST_LOGIN_DTO))
        ).andExpect { result ->
            assertEquals(HttpStatus.OK.value(), result.response.status)
            assertNotNull(result.response.getHeader(HttpHeaders.AUTHORIZATION))
            val token = result.response.getHeader(HttpHeaders.AUTHORIZATION)?.replace("Bearer ", "")
            val claims: Claims = Jwts.parserBuilder()
                .setSigningKey(env.getRequiredProperty("application.security.jwt.secret").toByteArray())
                .build()
                .parseClaimsJws(token).body
            val user = userRepository.findOneByUsername(TEST_REGISTER_DTO.username)
                ?: fail("User not found! Previous test case must have failed.")
            for ((key, value) in user.jwtPayload) {
                assertTrue(claims.containsKey(key))
                assertEquals(value, claims[key])
            }
        }
    }

    @Test
    @Order(3)
    fun `assert that login fails with wrong credentials`() {
        mvc.perform(
            post("/api/login").contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(LoginDto("someWrongUsername", "someWrongPassword")))
        ).andExpect { result ->
            assertEquals(HttpStatus.UNAUTHORIZED.value(), result.response.status)
        }
    }

    @AfterAll
    fun teardown() {
        userRepository.delete(userRepository.findOneByUsername(TEST_REGISTER_DTO.username)!!)
    }
}
