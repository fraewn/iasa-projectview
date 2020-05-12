package com.iasa.projectview.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.iasa.projectview.model.dto.IASAErrorResponse
import com.iasa.projectview.model.exception.IASAException
import com.iasa.projectview.model.exception.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.reflect.full.isSubclassOf

@RestControllerAdvice
class IASAExceptionHandler : AuthenticationEntryPoint, AccessDeniedHandler {

    override fun commence(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        exception: AuthenticationException?
    ) {
        handle(request, response, exception, "Invalid credentials")
    }

    override fun handle(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        exception: AccessDeniedException?
    ) {
        handle(request, response, exception, "You don't have access to this resource")
    }

    @ExceptionHandler(IASAException::class)
    fun handleAppException(
        e: IASAException?,
        req: HttpServletRequest?
    ): ResponseEntity<IASAErrorResponse> {
        val code = evaluateStatusCode(e)
        return ResponseEntity(
            IASAErrorResponse(
                e?.message ?: "Error",
                code.value(),
                code.reasonPhrase,
                req?.requestURI ?: "unknown"
            ), evaluateStatusCode(e)
        )
    }

    private fun evaluateStatusCode(e: Exception?): HttpStatus {
        return when {
            e is AuthenticationException -> HttpStatus.UNAUTHORIZED
            e is AccessDeniedException -> HttpStatus.FORBIDDEN
            e is NotFoundException -> HttpStatus.NOT_FOUND
            e!!::class.isSubclassOf(IASAException::class) -> HttpStatus.INTERNAL_SERVER_ERROR
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }
    }

    private fun handle(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        exception: Exception?,
        text: String? = null
    ) {
        response?.contentType = MediaType.APPLICATION_JSON_VALUE
        val code = evaluateStatusCode(exception)
        response?.status = code.value()
        jacksonObjectMapper().writeValue(
            response?.outputStream,
            IASAErrorResponse(
                text ?: exception?.message ?: "An error occurred",
                code.value(),
                code.reasonPhrase,
                request?.requestURI ?: "unknown"
            )
        )
    }
}
