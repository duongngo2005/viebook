package com.ndd.viebook.user;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    MEMBER,
    LIBRARIAN,
    ADMIN
    ;

    @Override
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
