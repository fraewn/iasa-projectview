package com.iasa.projectview.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.iasa.projectview.model.dto.IASAErrorResponse
import com.iasa.projectview.model.entity.SystemRole
import com.iasa.projectview.model.entity.User
import com.iasa.projectview.persistence.repository.UserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.util.*

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UsersApiControllerIntegrationTest(
    @Autowired private val userRepository: UserRepository
) {

    @Autowired
    private lateinit var mvc: MockMvc

    private val username = "testUser"
    private val password = "testPassword"
    val testRegisterDto = User.RegisterDto(
        username,
        password
    )

    val testGuestUser = User(
        "guest",
        "password",
        HashSet(listOf(SystemRole(SystemRole.GUEST, 2))),
        2,
        isActive = true,
        isLocked = true,
        isExpired = true
    )

    @Test
    fun `post to UsersApi with registration credentials returns Created with Location header`() {
        mvc.perform(
            MockMvcRequestBuilders.post(UsersApi.ROUTE).contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsBytes(testRegisterDto))
        ).andExpect { result ->
            assertEquals(HttpStatus.CREATED.value(), result.response.status)
            val locationHeader: String? = result.response.getHeader("Location")
            assertTrue(locationHeader?.contains(UsersApi.ROUTE) ?: fail("Location header was not present"))
        }
    }

    @Test
    fun `post to UsersApi with credentials from existing user returns Conflict`() {
        userRepository.save(testGuestUser)

        mvc.perform(
            MockMvcRequestBuilders.post(UsersApi.ROUTE).contentType(MediaType.APPLICATION_JSON).content(
                jacksonObjectMapper().writeValueAsBytes(User.RegisterDto("guest", "password"))
            )
        ).andExpect { result ->
            val responseBody = jacksonObjectMapper().readValue<IASAErrorResponse>(result.response.contentAsString)
            assertEquals(HttpStatus.CONFLICT.value(), result.response.status)
            assertEquals(HttpStatus.CONFLICT.value(), responseBody.code)
            assertEquals(HttpStatus.CONFLICT.reasonPhrase, responseBody.status)
            assertEquals(UsersApi.ROUTE, responseBody.path)
        }
    }

    @AfterEach
    fun tearDown() {
        userRepository.deleteAll()
    }
}
