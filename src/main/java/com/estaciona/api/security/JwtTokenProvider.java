package com.estaciona.api.security;

import com.estaciona.api.modules.usuarios.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

/**
 * Genera y valida tokens JWT con algoritmo HS256.
 * Claims mínimos: sub (UUID del usuario), rol, iat, exp.
 */
@Component
public class JwtTokenProvider {

    private final SecretKey claveSecreta;
    private final long minutosExpiracion;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-minutes}") long minutosExpiracion) {
        this.claveSecreta = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.minutosExpiracion = minutosExpiracion;
    }

    /** Genera un token JWT firmado con HS256 para el usuario dado. */
    public String generarToken(Usuario usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getId().toString())
                .claim("rol", usuario.getRol().getNombre())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(minutosExpiracion, ChronoUnit.MINUTES)))
                .signWith(claveSecreta)
                .compact();
    }

    /** Extrae todos los claims del token (lanza excepción si es inválido o expirado). */
    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(claveSecreta)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Extrae el UUID del usuario del claim 'sub'. */
    public UUID extraerUsuarioId(String token) {
        return UUID.fromString(extraerClaims(token).getSubject());
    }

    /** Extrae el nombre del rol del claim 'rol'. */
    public String extraerRol(String token) {
        return extraerClaims(token).get("rol", String.class);
    }

    /** Retorna la duración del token en segundos (para la respuesta de login). */
    public long obtenerExpiracionEnSegundos() {
        return minutosExpiracion * 60;
    }

    /** Valida firma y expiración del token. */
    public boolean validarToken(String token) {
        try {
            extraerClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
