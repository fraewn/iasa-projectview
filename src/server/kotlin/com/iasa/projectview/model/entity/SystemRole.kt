package com.iasa.projectview.model.entity

import javax.persistence.*

@Entity
@Table(name = "systemrole")
data class SystemRole(
    @Column(name = "systemrole_name")
    var name: String,

    @Id
    @Column(name = "systemrole_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0
) {
    override fun toString(): String {
        return "ROLE_${name.toUpperCase()}"
    }
}
