package com.iasa.projectview.controller

import com.iasa.projectview.model.entity.User
import com.iasa.projectview.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @PostMapping
    fun registerUser(@RequestBody dto: User.RegisterDto): ResponseEntity<Unit> {
        val newUser = userService.registerUser(dto)
        return ResponseEntity.created(URI("/api/users/${newUser.id}")).build()
    }
}
