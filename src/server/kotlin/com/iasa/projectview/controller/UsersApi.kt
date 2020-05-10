package com.iasa.projectview.controller

import com.iasa.projectview.controller.UsersApi.Companion.ROUTE
import com.iasa.projectview.model.dto.IASAApiResponse
import com.iasa.projectview.model.entity.User
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping(ROUTE)
interface UsersApi {
    companion object {
        const val ROUTE = "/api/users"
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    fun getAll(): ResponseEntity<IASAApiResponse<List<User>>>

    @PostMapping
    fun registerUser(@RequestBody dto: User.RegisterDto): ResponseEntity<Unit>
}
