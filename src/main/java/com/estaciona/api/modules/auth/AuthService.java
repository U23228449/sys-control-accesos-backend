package com.estaciona.api.modules.auth;

import com.estaciona.api.modules.auth.dto.LoginRequest;
import com.estaciona.api.modules.auth.dto.LoginResponse;

/**
 * Contrato del servicio de autenticación.
 */
public interface AuthService {

    LoginResponse login(LoginRequest request);
}
