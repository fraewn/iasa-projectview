package com.iasa.projectview.service

import com.iasa.projectview.model.entity.User
import com.iasa.projectview.persistence.repository.UserRepository
import com.iasa.projectview.service.UserServiceUnitTestConfiguration.TestData.NEW_USER_DTO
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
internal class UserServiceUnitTest(
    @Autowired private val userService: UserService,
    @Autowired private val userRepository: UserRepository
) {
    @Test
    @Order(1)
    fun `registerUser with register credentials returns newly added user`() {
        val newUser = (userService.registerUser(NEW_USER_DTO))
        assertEquals(NEW_USER_DTO.username, newUser.username)
        assertNotEquals(NEW_USER_DTO.password, newUser.password)
        assertNotEquals(newUser.id, 0)
    }

    @Test
    @Order(2)
    fun `loadUserByUsername with username from existing user returns user`() {
        val loadedUser = userService.loadUserByUsername(NEW_USER_DTO.username) as User
        assertEquals(NEW_USER_DTO.username, loadedUser.username)
        assertNotEquals(NEW_USER_DTO.password, loadedUser.password)
        assertNotEquals(loadedUser.id, 0)
    }

    @Test
    @Order(3)
    fun `loadUserByUsername with username from not existing user throws UsernameNotFoundException`() {
        assertThrows(UsernameNotFoundException::class.java) {
            userService.loadUserByUsername("someRandomUsername")
        }
    }

    @AfterAll
    fun teardown() {
        userRepository.deleteAll()
    }
}
