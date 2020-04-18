package com.iasa.projectview.security.filter

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.SignatureException
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtVerificationFilter(private val userDetailsService: UserDetailsService) : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        // registering url does not need to check for a valid token
        if (request.requestURI == "/users" && request.method == RequestMethod.POST.name) {
            chain.doFilter(request, response)
            return
        }

        val authorizationHeader = request.getHeader("Authorization")

        if (authorizationHeader.isNullOrEmpty() || !authorizationHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response)
            return
        }

        val token = authorizationHeader.replace("Bearer ", "")

        try {
            val claimsJws: Jws<Claims> = Jwts.parserBuilder()
                .setSigningKey(environment.getRequiredProperty("application.security.jwt.secret").toByteArray())
                .build()
                .parseClaimsJws(token)
            val body = claimsJws.body
            val user = userDetailsService.loadUserByUsername(body["username"] as String)

            val authentication = UsernamePasswordAuthenticationToken(
                user.username,
                user.password,
                user.authorities
            )
            authentication.details = user
            SecurityContextHolder.getContext().authentication = authentication
        } catch (e: SignatureException) {
            throw BadCredentialsException("Token has invalid signature", e)
        } catch (e: ExpiredJwtException) {
            throw CredentialsExpiredException("Token is expired", e)
        } catch (e: UsernameNotFoundException) {
            throw AuthenticationCredentialsNotFoundException(e.message, e)
        }

        chain.doFilter(request, response)
    }
}
