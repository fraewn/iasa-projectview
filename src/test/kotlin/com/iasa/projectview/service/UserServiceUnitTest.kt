package com.iasa.projectview.service

import com.iasa.projectview.model.entity.SystemRole
import com.iasa.projectview.model.entity.User
import com.iasa.projectview.model.exception.ExistsException
import com.iasa.projectview.persistence.repository.SystemRoleRepository
import com.iasa.projectview.persistence.repository.UserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UserServiceUnitTest(
    @Autowired private val userService: UserService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val systemRoleRepository: SystemRoleRepository
) {
    val newUserRegisterDto: User.RegisterDto = User.RegisterDto("testUser", "testPassword")

    @Test
    fun `registerUser with register credentials returns newly added user`() {
        val newUser = (userService.registerUser(newUserRegisterDto))
        assertEquals(newUserRegisterDto.username, newUser.username)
        assertNotEquals(newUserRegisterDto.password, newUser.password)
        assertNotEquals(newUser.id, 0)
    }

    @Test
    fun `registerUser with register credentials for existing user throws ExistsException`() {
        userRepository.save(
            User(
                newUserRegisterDto.username,
                newUserRegisterDto.password,
                HashSet(listOf(systemRoleRepository.findByName(SystemRole.ADMIN)!!))
            )
        )
        assertThrows(ExistsException::class.java) {
            userService.registerUser(newUserRegisterDto)
        }
    }

    @Test
    fun `loadUserByUsername with username from existing user returns user`() {
        val expectedUser = userRepository.save(
            User(
                newUserRegisterDto.username,
                newUserRegisterDto.password,
                HashSet(listOf(systemRoleRepository.findByName(SystemRole.GUEST)!!))
            )
        )
        val loadedUser = userService.loadUserByUsername(newUserRegisterDto.username) as User
        assertEquals(expectedUser.username, loadedUser.username)
        assertEquals(expectedUser.password, loadedUser.password)
        assertNotEquals(loadedUser.id, 0)
    }

    @Test
    fun `loadUserByUsername with username from not existing user throws UsernameNotFoundException`() {
        assertThrows(UsernameNotFoundException::class.java) {
            userService.loadUserByUsername("someRandomUsername")
        }
    }

    @AfterEach
    fun teardown() {
        userRepository.deleteAll()
    }
}
