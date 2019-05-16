package com.future.apix.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Document("Users")
@JsonIgnoreProperties(value = {"enabled", "accountNonExpired", "accountNonLocked", "credentialsNonExpired"})
public class User implements UserDetails {
    @Id
    String id;

    @NotBlank(message = "username must not be blank")
    String username;

    @NotBlank(message = "password must not be blank")
    String password;

    @NotEmpty
    private List<String> roles = new ArrayList<>();
    private List<String> teams = new ArrayList<>();

    public enum Role {
        ROLE_ADMIN, ROLE_USER;
    }

    @Transient
    private boolean enabled = true;
    @Transient
    private boolean accountNonExpired = true;
    @Transient
    private boolean accountNonLocked = true;
    @Transient
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
