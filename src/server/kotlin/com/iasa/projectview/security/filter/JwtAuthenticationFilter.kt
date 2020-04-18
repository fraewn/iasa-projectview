package com.iasa.projectview.security.filter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.iasa.projectview.model.entity.User
import com.iasa.projectview.model.entity.User.LoginDto
import com.iasa.projectview.util.addSeconds
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtAuthenticationFilter : UsernamePasswordAuthenticationFilter() {

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        val loginDto: LoginDto = jacksonObjectMapper().readValue(request.inputStream, LoginDto::class.java)
        val authenticationToken = UsernamePasswordAuthenticationToken(
            loginDto.username,
            loginDto.password
        )
        return authenticationManager.authenticate(authenticationToken)
    }

    override fun successfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
        authResult: Authentication
    ) {
        val user: User = authResult.principal as User
        val token = Jwts.builder()
            .setClaims(user.jwtPayload)
            .setIssuedAt(Date())
            .setExpiration(
                Date().addSeconds(
                    environment.getRequiredProperty("application.security.jwt.expiration.seconds").toInt()
                )
            )
            .signWith(
                Keys.hmacShaKeyFor(
                    environment.getRequiredProperty("application.security.jwt.secret").toByteArray()
                )
            )
            .compact()
        response.addHeader("Authorization", "Bearer $token")
    }
}
