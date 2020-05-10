package com.iasa.projectview.controller

import com.iasa.projectview.model.dto.IASAApiResponse
import com.iasa.projectview.model.entity.User
import com.iasa.projectview.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController("UserController")
class UsersApiController(private val userService: UserService) : UsersApi {

    override fun getAll(): ResponseEntity<IASAApiResponse<List<User>>> {
        return ResponseEntity.ok(IASAApiResponse(userService.getAll()))
    }

    override fun registerUser(@RequestBody dto: User.RegisterDto): ResponseEntity<Unit> {
        val newUser = userService.registerUser(dto)
        return ResponseEntity.created(URI("${UsersApi.ROUTE}/${newUser.id}")).build()
    }
}
