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
        val encoders = HashMap<String, PasswordEncoder>()

        // BCrypt
        val bcryptStrength: Int? = env.getRequiredProperty("application.security.bcrypt.strength").toInt()
        val bcryptPasswordEncoder = BCryptPasswordEncoder(bcryptStrength ?: 12)
        encoders["bcrypt"] = bcryptPasswordEncoder

        val encoder = DelegatingPasswordEncoder("bcrypt", encoders)
        encoder.setDefaultPasswordEncoderForMatches(bcryptPasswordEncoder)
        return encoder
    }
}
