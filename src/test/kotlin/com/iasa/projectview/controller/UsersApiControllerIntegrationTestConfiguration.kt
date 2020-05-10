package com.iasa.projectview.controller

import com.iasa.projectview.model.entity.SystemRole
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
        val TEST_ADMIN_USER = User(
            "admin",
            "password",
            HashSet(listOf(SystemRole("admin", 1))),
            1,
            isActive = true,
            isLocked = true,
            isExpired = true
        )

        val TEST_GUEST_USER = User(
            "guest",
            "password",
            HashSet(listOf(SystemRole("guest", 2))),
            2,
            isActive = true,
            isLocked = true,
            isExpired = true
        )

        val TEST_NON_EXISTING_USER = User(
            "none",
            "password",
            HashSet(listOf(SystemRole("guest", 1))),
            3,
            isActive = true,
            isLocked = true,
            isExpired = true
        )
    }
}
