package com.iasa.projectview.security.filter

import com.iasa.projectview.controller.IASAExceptionHandler
import com.iasa.projectview.controller.UsersApi
import io.jsonwebtoken.*
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

    private val handler = IASAExceptionHandler()

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        // registering url does not need to check for a valid token
        if (request.requestURI == UsersApi.ROUTE && request.method == RequestMethod.POST.name) {
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
            handler.commence(request, response, BadCredentialsException("Token has invalid signature", e))
        } catch (e: ExpiredJwtException) {
            handler.commence(request, response, CredentialsExpiredException("Token is expired", e))
        } catch (e: UsernameNotFoundException) {
            handler.commence(request, response, AuthenticationCredentialsNotFoundException(e.message, e))
        } catch (e: MalformedJwtException) {
            handler.commence(request, response, BadCredentialsException("Malformed token", e))
        }

        chain.doFilter(request, response)
    }
}
