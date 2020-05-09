package com.iasa.projectview.config

import com.iasa.projectview.controller.UsersApi
import com.iasa.projectview.security.filter.JwtAuthenticationFilter
import com.iasa.projectview.security.filter.JwtVerificationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val encoder: PasswordEncoder,
    private val userDetailsService: UserDetailsService,
    private val env: Environment
) : WebSecurityConfigurerAdapter() {
    companion object {
        const val LOGIN_ROUTE = "/api/login"
    }

    override fun configure(http: HttpSecurity) {
        http
            .csrf().disable()
            .cors().configurationSource(corsConfig())
            .and()
            .authorizeRequests()
            // register route
            .antMatchers(HttpMethod.POST, UsersApi.USERS_API_ROUTE).permitAll()
            // static files (matches anything that does not start with /api)
            // language=RegExp
            .regexMatchers(HttpMethod.GET, "/((?!api).*)").permitAll()
            .and()
            .addFilter(jwtAuthenticationFilter())
            .addFilterAfter(jwtVerificationFilter(), JwtAuthenticationFilter::class.java)
            .authorizeRequests()
            .anyRequest()
            .authenticated()
    }


    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(daoAuthenticationProvider())
    }

    @Bean
    fun daoAuthenticationProvider(): DaoAuthenticationProvider {
        val provider = DaoAuthenticationProvider()
        provider.setPasswordEncoder(encoder)
        provider.setUserDetailsService(userDetailsService)
        return provider
    }

    private fun corsConfig(): CorsConfigurationSource {
        val config = CorsConfiguration().applyPermitDefaultValues()
        config.allowedOrigins = listOf("http://localhost:4200", "http://localhost:8090")
        config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        config.exposedHeaders = listOf(
            "Cache-Control",
            "Content-Language",
            "Content-Length",
            "Content-Type",
            "Expires",
            "Last-Modified",
            "Pragma",
            "Authorization"
        )
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }

    private fun jwtAuthenticationFilter(): JwtAuthenticationFilter {
        val filter = JwtAuthenticationFilter()
        filter.setFilterProcessesUrl(LOGIN_ROUTE)
        filter.setAuthenticationManager(authenticationManager())
        filter.environment = env
        return filter
    }

    private fun jwtVerificationFilter(): JwtVerificationFilter {
        val filter = JwtVerificationFilter(userDetailsService)
        filter.environment = env
        return filter
    }
}
