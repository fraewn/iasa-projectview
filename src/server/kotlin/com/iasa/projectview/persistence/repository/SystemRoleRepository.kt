package com.iasa.projectview.persistence.repository

import com.iasa.projectview.model.entity.SystemRole
import org.springframework.data.jpa.repository.JpaRepository

interface SystemRoleRepository : JpaRepository<SystemRole, Int> {
    fun findByName(name: String): SystemRole?
}
