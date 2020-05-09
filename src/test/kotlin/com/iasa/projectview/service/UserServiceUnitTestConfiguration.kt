package com.iasa.projectview.service

import com.iasa.projectview.model.entity.User
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class UserServiceUnitTestConfiguration {
    companion object TestData {
        val NEW_USER_DTO: User.RegisterDto = User.RegisterDto("testUser", "testPassword")
    }
}
