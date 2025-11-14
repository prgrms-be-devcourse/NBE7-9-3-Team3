package org.example.backend.global.security

import org.example.backend.domain.member.entity.Member
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(val member: Member) : UserDetails {
    val id: Long?
        get() = member.memberId

    val email: String
        get() = member.email

    val nickname: String
        get() = member.nickname

    override fun getAuthorities(): MutableCollection<out GrantedAuthority?> {
        return mutableListOf<GrantedAuthority?>()
    }

    override fun getPassword(): String {
        return member.password
    }

    override fun getUsername(): String {
        return member.email
    }
}
