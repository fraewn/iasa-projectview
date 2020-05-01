package com.iasa.projectview.config

import com.iasa.projectview.model.entity.User
import com.iasa.projectview.persistence.repository.UserRepository
import org.mockito.Mockito
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class UserServiceTestConfiguration {

    companion object {
        val NEW_USER_DTO: User.RegisterDto = User.RegisterDto("testUser", "testPassword")
    }

    @Bean
    @Primary
    fun userRepositoryMock(): UserRepository {
        return Mockito.mock(UserRepository::class.java)
    }
}
