package com.iasa.projectview.service

import com.iasa.projectview.model.entity.User
import com.iasa.projectview.model.entity.User.RegisterDto
import com.iasa.projectview.model.exception.ExistsException
import com.iasa.projectview.persistence.repository.SystemRoleRepository
import com.iasa.projectview.persistence.repository.UserRepository
import org.springframework.context.annotation.Primary
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
@Primary
class UserService(
    private val userRepository: UserRepository,
    private val systemRoleRepository: SystemRoleRepository,
    private val encoder: PasswordEncoder
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails = userRepository.findOneByUsername(username)
        ?: throw UsernameNotFoundException("User $username does not exist")

    fun registerUser(dto: RegisterDto): User {
        if (userRepository.findOneByUsername(dto.username) != null) {
            throw ExistsException("User already exists")
        }
        return userRepository.save(
            User(
                dto.username,
                encoder.encode(dto.password),
                HashSet(systemRoleRepository.findAll())
            )
        )
    }

    fun getAll(): List<User> = userRepository.findAll()
}
