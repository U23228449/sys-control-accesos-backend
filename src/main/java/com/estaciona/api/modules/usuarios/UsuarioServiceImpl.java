package com.estaciona.api.modules.usuarios;

import com.estaciona.api.common.exception.DuplicateResourceException;
import com.estaciona.api.common.exception.ResourceNotFoundException;
import com.estaciona.api.modules.roles.RolRepository;
import com.estaciona.api.modules.roles.entity.Rol;
import com.estaciona.api.modules.usuarios.dto.UsuarioRequest;
import com.estaciona.api.modules.usuarios.dto.UsuarioResponse;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de la lógica de negocio para usuarios.
 */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioFactory usuarioFactory;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              RolRepository rolRepository,
                              PasswordEncoder passwordEncoder,
                              UsuarioFactory usuarioFactory) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioFactory = usuarioFactory;
    }

    @Override
    @Transactional
    public UsuarioResponse registrarUsuario(UsuarioRequest request) {
        // 1. Validar unicidad de correo (case-insensitive)
        if (usuarioRepository.existsByCorreoIgnoreCase(request.correo())) {
            throw new DuplicateResourceException("Usuario", "correo", request.correo());
        }

        // 2. Validar unicidad de documento
        if (usuarioRepository.existsByDocumento(request.documento())) {
            throw new DuplicateResourceException("Usuario", "documento", request.documento());
        }

        // 3. Resolver Rol
        Rol rol = rolRepository.findById(request.rolId())
                .filter(Rol::isEnabled)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado o deshabilitado con id: " + request.rolId()));

        // 4. Hashear password
        String passwordHash = passwordEncoder.encode(request.password());

        // 5. Crear entidad vía Factory
        Usuario usuario = usuarioFactory.crear(request, rol, passwordHash);

        // 6. Persistir en la base de datos
        Usuario usuarioPersistido = usuarioRepository.save(usuario);

        // 7. Mapear a respuesta
        return toResponse(usuarioPersistido);
    }

    /** Mapeo manual de Usuario a UsuarioResponse. */
    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getCorreo(),
                usuario.getDocumento(),
                usuario.getRol().getNombre(),
                usuario.getTipoUsuario(),
                usuario.isEnabled(),
                usuario.getCreatedAt()
        );
    }
}
