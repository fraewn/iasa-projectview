package com.iasa.projectview.util.handler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.iasa.projectview.model.dto.IASAErrorResponse
import com.iasa.projectview.model.exception.IASAException
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
        response?.contentType = MediaType.APPLICATION_JSON_VALUE
        response?.status = evaluateStatusCode(exception).value()
        jacksonObjectMapper().writeValue(
            response?.outputStream, IASAErrorResponse("Invalid credentials")
        )
    }

    override fun handle(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        exception: AccessDeniedException?
    ) {
        response?.contentType = MediaType.APPLICATION_JSON_VALUE
        response?.status = evaluateStatusCode(exception).value()
        jacksonObjectMapper().writeValue(
            response?.outputStream, IASAErrorResponse("You don't have access to this resource")
        )
    }

    @ExceptionHandler(IASAException::class)
    fun handleAppException(
        e: IASAException,
        req: HttpServletRequest
    ): ResponseEntity<IASAErrorResponse> =
        ResponseEntity(IASAErrorResponse(e.message ?: "Error"), evaluateStatusCode(e))

    private fun evaluateStatusCode(e: Exception?): HttpStatus {
        return when {
            e is AuthenticationException -> HttpStatus.UNAUTHORIZED
            e is AccessDeniedException -> HttpStatus.FORBIDDEN
            e!!::class.isSubclassOf(IASAException::class) -> HttpStatus.INTERNAL_SERVER_ERROR
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }
    }
}
