package com.estaciona.api.security;

import com.estaciona.api.modules.usuarios.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Adaptador de Usuario hacia UserDetails de Spring Security.
 */
public class CustomUserDetails implements UserDetails {

    private final Usuario usuario;

    public CustomUserDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    /** Expone el UUID del usuario para que SecurityContextUtils lo pueda leer. */
    public UUID obtenerUsuarioId() {
        return usuario.getId();
    }

    public Usuario obtenerUsuario() {
        return usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security requiere el prefijo ROLE_ para hasRole(...)
        return List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre()));
    }

    @Override
    public String getPassword() {
        return usuario.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return usuario.getCorreo();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() {
        return usuario.isEnabled();
    }
}
