package com.iasa.projectview.controller

import com.iasa.projectview.controller.UsersApi.Companion.USERS_API_ROUTE
import com.iasa.projectview.model.entity.User
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping(USERS_API_ROUTE)
interface UsersApi {
    companion object {
        const val USERS_API_ROUTE = "/api/users"
    }

    @PostMapping
    fun registerUser(@RequestBody dto: User.RegisterDto): ResponseEntity<Unit>
}
