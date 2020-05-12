package com.iasa.projectview.security

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.iasa.projectview.config.SecurityConfig
import com.iasa.projectview.model.entity.User.LoginDto
import com.iasa.projectview.persistence.repository.UserRepository
import com.iasa.projectview.security.AuthenticationIntegrationTestConfiguration.TestData.TEST_LOGIN_DTO
import com.iasa.projectview.security.AuthenticationIntegrationTestConfiguration.TestData.TEST_REGISTER_DTO
import com.iasa.projectview.service.UserService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AuthenticationIntegrationTest(
    @Autowired private val context: WebApplicationContext,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val env: Environment,
    @Autowired private val userService: UserService
) {
    private lateinit var mvc: MockMvc

    @BeforeAll
    fun setup() {
        userService.registerUser(TEST_REGISTER_DTO)

        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @Test
    fun `post to login route with existing user credentials returns Ok wiht Authorization header containing JWT`() {
        mvc.perform(
            post(SecurityConfig.LOGIN_ROUTE).contentType(MediaType.APPLICATION_JSON)
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
    fun `post to login route with non existing user credentials returns Unauthorized`() {
        mvc.perform(
            post(SecurityConfig.LOGIN_ROUTE).contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(LoginDto("someWrongUsername", "someWrongPassword")))
        ).andExpect { result ->
            assertEquals(HttpStatus.UNAUTHORIZED.value(), result.response.status)
        }
    }

    @AfterAll
    fun teardown() {
        userRepository.deleteAll()
    }
}
