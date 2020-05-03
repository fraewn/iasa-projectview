package com.iasa.projectview.service

import com.iasa.projectview.config.UserServiceTestConfiguration.TestData.NEW_USER_DTO
import com.iasa.projectview.model.entity.User
import com.iasa.projectview.persistence.repository.UserRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation::class)
internal class UserServiceTest(
    @Autowired private val userService: UserService,
    @Autowired private val userRepository: UserRepository
) {
    @Test
    @Order(1)
    fun `assert that a user is added correctly to the database`() {
        val newUser = (userService.registerUser(NEW_USER_DTO))
        assertEquals(NEW_USER_DTO.username, newUser.username)
        assertNotEquals(NEW_USER_DTO.password, newUser.password)
        assertNotEquals(newUser.id, 0)
    }

    @Test
    @Order(2)
    fun `assert that user is loaded correctly from the database`() {
        val loadedUser = userService.loadUserByUsername(NEW_USER_DTO.username) as User
        assertEquals(NEW_USER_DTO.username, loadedUser.username)
        assertNotEquals(NEW_USER_DTO.password, loadedUser.password)
        assertNotEquals(loadedUser.id, 0)
    }

    @Test
    @Order(3)
    fun `assert that the appropriate exception is thrown for the wrong username`() {
        assertThrows(UsernameNotFoundException::class.java) {
            userService.loadUserByUsername("someRandomUsername")
        }
    }

    @AfterAll
    fun teardown() {
        userRepository.delete(userRepository.findOneByUsername(NEW_USER_DTO.username)!!)
    }
}
