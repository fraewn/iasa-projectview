package com.iasa.projectview.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.DelegatingPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class PasswordConfig(private val env: Environment) {

    /**
     * Using DelegatingPasswordEncoder to be able to upgrade in the future
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder {

        // BCrypt
        val bcryptStrength: Int? = env.getProperty("application.security.bcrypt.strength")?.toInt()
        val encoders = mapOf(
            "bcrypt" to BCryptPasswordEncoder(bcryptStrength ?: 12)
        )

        return DelegatingPasswordEncoder("bcrypt", encoders)
    }
}
