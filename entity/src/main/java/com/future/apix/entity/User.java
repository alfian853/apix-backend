package com.future.apix.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Document("Users")
public class User implements UserDetails {
    @Id
    String id;

    @NotEmpty
    String username, password;

    private List<String> roles = new ArrayList<>();
    private List<String> teams = new ArrayList<>();

    public enum Role {
        ROLE_ADMIN, ROLE_USER;
    }

    private boolean enabled = true;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        /* // dipakai jika String role
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        grantedAuthorities.add( new SimpleGrantedAuthority(getRole()) );
        return grantedAuthorities;
        */

        // dipakai jika List<String> roles
        return this.roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
}
