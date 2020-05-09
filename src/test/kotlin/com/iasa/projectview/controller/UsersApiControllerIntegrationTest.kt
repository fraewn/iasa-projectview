package com.iasa.projectview.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.iasa.projectview.controller.UsersApiControllerIntegrationTestConfiguration.TestData.TEST_REGISTER_DTO
import com.iasa.projectview.model.entity.User
import com.iasa.projectview.persistence.repository.UserRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UsersApiControllerIntegrationTest(
    @Autowired private val context: WebApplicationContext,
    @Autowired private val userRepository: UserRepository
) {

    private lateinit var mvc: MockMvc

    @BeforeAll
    fun setup() {
        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .build()
    }

    @Test
    fun `post to UsersApi with registration credentials returns Created with Location header`() {
        mvc.perform(
            MockMvcRequestBuilders.post(UsersApi.USERS_API_ROUTE).contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(TEST_REGISTER_DTO))
        ).andExpect { result ->
            assertEquals(HttpStatus.CREATED.value(), result.response.status)
            assertNotNull(result.response.headerNames.contains("Location"))
        }
    }

    @AfterAll
    fun tearDown() {
        userRepository.delete(userRepository.findOneByUsername(TEST_REGISTER_DTO.username) as User)
    }
}
