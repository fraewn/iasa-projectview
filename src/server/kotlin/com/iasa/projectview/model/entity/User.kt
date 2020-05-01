package com.iasa.projectview.model.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*
import javax.persistence.*
import kotlin.collections.HashSet
import kotlin.collections.set

@Entity
@Table(name = "iasauser")
data class User(
    @get:JsonIgnore
    @Column(name = "iasauser_name", nullable = false, unique = true)
    @get:JvmName("getUsername_")
    var username: String,

    @get:JsonIgnore
    @Column(name = "iasauser_password", nullable = false)
    @get:JvmName("getPassword_")
    var password: String,

    @JsonManagedReference
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "iasauser_systemrole",
        joinColumns = [JoinColumn(name = "iasauser_id")],
        inverseJoinColumns = [JoinColumn(name = "systemrole_id")]
    )
    var roles: Set<SystemRole>,

    @Id
    @Column(name = "iasauser_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,

    @Column(name = "iasauser_active")
    var isActive: Boolean = true,

    @Column(name = "iasauser_locked")
    var isLocked: Boolean = false,

    @Column(name = "iasauser_expired")
    var isExpired: Boolean = false
) : UserDetails {
    @get:JsonIgnore
    val jwtPayload: Map<String, Any>
        get() {
            val payload = HashMap<String, Any>()
            payload["id"] = id
            payload["username"] = username
            payload["authorities"] = authorities.map { grantedAuthority -> grantedAuthority.authority }
            return payload
        }

    @JsonIgnore
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return HashSet(roles.map { systemRole -> SimpleGrantedAuthority(systemRole.toString()) })
    }

    @JsonIgnore
    override fun isEnabled(): Boolean {
        return isActive
    }

    override fun getUsername(): String {
        return username
    }

    @JsonIgnore
    override fun isCredentialsNonExpired(): Boolean {
        return true // credentials do not expire in this application
    }

    @JsonIgnore
    override fun getPassword(): String {
        return password
    }

    @JsonIgnore
    override fun isAccountNonExpired(): Boolean {
        return !isExpired
    }

    @JsonIgnore
    override fun isAccountNonLocked(): Boolean {
        return !isLocked
    }

    data class LoginDto(val username: String, val password: String)

    // currently the same as LoginDto but can be extended in the future
    data class RegisterDto(val username: String, val password: String)
}
