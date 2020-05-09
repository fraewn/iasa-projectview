package com.iasa.projectview.security

import com.iasa.projectview.model.entity.User.LoginDto
import com.iasa.projectview.model.entity.User.RegisterDto
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
internal class AuthenticationIntegrationTestConfiguration {
    companion object TestData {
        private const val username = "testUser"
        private const val password = "testPassword"
        val TEST_REGISTER_DTO = RegisterDto(
            username,
            password
        )
        val TEST_LOGIN_DTO = LoginDto(
            username,
            password
        )
    }
}
