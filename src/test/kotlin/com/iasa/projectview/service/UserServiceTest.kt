package com.iasa.projectview.service

import com.iasa.projectview.config.UserServiceTestConfiguration
import com.iasa.projectview.model.entity.SystemRole
import com.iasa.projectview.model.entity.User
import com.iasa.projectview.persistence.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UserServiceTest(
    @Autowired private val userService: UserService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val encoder: PasswordEncoder
) {
    private lateinit var testDto: User.RegisterDto

    @BeforeAll
    fun setup() {
        testDto = UserServiceTestConfiguration.NEW_USER_DTO

        `when`(userRepository.save(any(User::class.java)))
            .thenReturn(
                User(
                    testDto.username,
                    encoder.encode(testDto.password),
                    listOf(
                        SystemRole("admin", 1),
                        SystemRole("guest", 2)
                    ).toHashSet(),
                    id = 1
                )
            )

        `when`(userRepository.findOneByUsername(testDto.username))
            .thenReturn(
                User(
                    testDto.username,
                    encoder.encode(testDto.password),
                    listOf(
                        SystemRole("admin", 1),
                        SystemRole("guest", 2)
                    ).toHashSet(),
                    id = 1
                )
            )
    }

    @Test
    @Order(1)
    fun `assert that a user is added correctly to the database`() {
        val newUser = userService.registerUser(testDto)
        assertEquals(testDto.username, newUser.username)
        assertNotEquals(testDto.password, newUser.password)
        assertNotEquals(newUser.id, 0)
    }

    @Test
    @Order(2)
    fun `assert that user is loaded correctly from the database`() {
        val loadedUser = userService.loadUserByUsername(testDto.username) as User
        assertEquals(testDto.username, loadedUser.username)
        assertNotEquals(testDto.password, loadedUser.password)
        assertNotEquals(loadedUser.id, 0)
    }

    @Test
    @Order(3)
    fun `assert that the appropriate exception is thrown for the wrong username`() {
        assertThrows(UsernameNotFoundException::class.java) {
            userService.loadUserByUsername("someRandomUsername")
        }
    }
}
