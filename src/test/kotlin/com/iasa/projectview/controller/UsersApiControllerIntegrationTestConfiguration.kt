package com.iasa.projectview.controller

import com.iasa.projectview.model.entity.User
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
internal class UsersApiControllerIntegrationTestConfiguration {
    companion object TestData {
        private const val username = "testUser"
        private const val password = "testPassword"
        val TEST_REGISTER_DTO = User.RegisterDto(
            username,
            password
        )
    }
}
