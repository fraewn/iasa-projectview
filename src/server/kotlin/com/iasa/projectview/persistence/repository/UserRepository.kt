package com.iasa.projectview.persistence.repository

import com.iasa.projectview.model.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Int> {
    fun findOneByUsername(username: String): User?
}
