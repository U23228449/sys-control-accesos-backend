package com.estaciona.api.modules.usuarios;

import com.estaciona.api.common.exception.DuplicateResourceException;
import com.estaciona.api.common.exception.ResourceNotFoundException;
import com.estaciona.api.modules.roles.RolRepository;
import com.estaciona.api.modules.roles.entity.Rol;
import com.estaciona.api.modules.usuarios.dto.UsuarioFiltroRequest;
import com.estaciona.api.modules.usuarios.dto.UsuarioRequest;
import com.estaciona.api.modules.usuarios.dto.UsuarioResumenProjection;
import com.estaciona.api.modules.usuarios.dto.UsuarioResponse;
import com.estaciona.api.modules.usuarios.dto.UsuarioUpdateEstadoRequest;
import com.estaciona.api.modules.usuarios.dto.UsuarioUpdateMeRequest;
import com.estaciona.api.modules.usuarios.dto.UsuarioUpdateRolRequest;
import com.estaciona.api.modules.usuarios.dto.UsuarioEliminadoResponseDTO;
import com.estaciona.api.modules.usuarios.eliminacion.UsuarioEliminacionValidationStrategy;
import com.estaciona.api.modules.usuarios.entity.Usuario;
import com.estaciona.api.modules.usuarios.update.UsuarioUpdateCommand;
import com.estaciona.api.modules.usuarios.update.UsuarioUpdateCommandFactory;
import com.estaciona.api.modules.usuarios.update.UsuarioUpdateValidationStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.estaciona.api.modules.usuarios.spec.UsuarioSpecifications;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.estaciona.api.modules.roles.entity.RolEnum;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementación de la lógica de negocio para usuarios.
 */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioFactory usuarioFactory;
    private final List<UsuarioUpdateValidationStrategy> updateStrategies;
    private final UsuarioUpdateCommandFactory commandFactory;
    private final List<UsuarioEliminacionValidationStrategy> eliminacionStrategies;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              RolRepository rolRepository,
                              PasswordEncoder passwordEncoder,
                              UsuarioFactory usuarioFactory,
                              List<UsuarioUpdateValidationStrategy> updateStrategies,
                              UsuarioUpdateCommandFactory commandFactory,
                              List<UsuarioEliminacionValidationStrategy> eliminacionStrategies) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioFactory = usuarioFactory;
        this.updateStrategies = updateStrategies;
        this.commandFactory = commandFactory;
        this.eliminacionStrategies = eliminacionStrategies;
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

    @Override
    @Transactional
    public UsuarioResponse actualizarMe(UUID usuarioId, UsuarioUpdateMeRequest request) {
        // 1. Cargar usuario (404 si no existe)
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", usuarioId));

        // 2. Ejecutar todas las estrategias de validación en orden
        for (UsuarioUpdateValidationStrategy strategy : updateStrategies) {
            strategy.validar(usuario, request, passwordEncoder);
        }

        // 3. Actualizar campos del perfil
        usuario.setNombreCompleto(request.nombreCompleto());
        usuario.setCorreo(request.correo());
        usuario.setDocumento(request.documento());

        // 4. Si se quiere cambiar la contraseña, rehashear con BCrypt
        if (request.passwordNuevo() != null) {
            usuario.setPasswordHash(passwordEncoder.encode(request.passwordNuevo()));
        }

        // 5. Persistir
        Usuario actualizado = usuarioRepository.save(usuario);
        return toResponse(actualizado);
    }

    @Override
    @Transactional
    public UsuarioResponse actualizarRol(UUID id, UsuarioUpdateRolRequest request, UUID adminId) {
        // 1. Cargar usuario (404 si no existe)
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        // 2. Cargar rol (404 si no existe o está deshabilitado)
        Rol rolNuevo = rolRepository.findById(request.rolId())
                .filter(Rol::isEnabled)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado o deshabilitado con id: " + request.rolId()));

        // 3. Crear el comando de cambio de rol (valida que no sea el mismo rol)
        UsuarioUpdateCommand comando = commandFactory.crearComandoRol(usuario, rolNuevo);

        // 4. Aplicar el comando y persistir
        comando.apply(usuario);
        Usuario actualizado = usuarioRepository.save(usuario);
        return toResponse(actualizado);
    }

    @Override
    @Transactional
    public UsuarioResponse actualizarEstado(UUID id, UsuarioUpdateEstadoRequest request, UUID adminId) {
        // 1. Cargar usuario (404 si no existe)
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        // Determine target enabled value (toggle if request/enabled is null)
        boolean targetEnabled = (request != null && request.enabled() != null)
                ? request.enabled()
                : !usuario.isEnabled();

        // 2. Crear el comando de cambio de estado (valida reglas de negocio)
        UsuarioUpdateCommand comando = commandFactory.crearComandoEstado(usuario, targetEnabled, adminId);

        // 3. Aplicar el comando y persistir
        comando.apply(usuario);
        Usuario actualizado = usuarioRepository.save(usuario);
        return toResponse(actualizado);
    }

    @Override
    @Transactional
    public UsuarioEliminadoResponseDTO eliminarUsuario(UUID id, UUID adminId) {
        // 1. Cargar usuario (404 si no existe, independiente de enabled)
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        // 2. Ejecutar todas las estrategias de validación de eliminación
        for (UsuarioEliminacionValidationStrategy strategy : eliminacionStrategies) {
            strategy.validar(usuario, adminId, usuarioRepository);
        }

        // 3. Soft delete: deshabilitar el usuario
        usuario.setEnabled(false);
        Usuario guardado = usuarioRepository.save(usuario);

        return new UsuarioEliminadoResponseDTO(
                "Usuario eliminado correctamente.",
                guardado.getId(),
                guardado.getNombreCompleto(),
                guardado.getCorreo(),
                guardado.getDocumento(),
                guardado.getRol().getNombre()
        );
    }

    private record UsuarioResumenProjectionImpl(
            UUID id,
            String nombreCompleto,
            String correo,
            String documento,
            String rolNombre,
            String tipoUsuario,
            Boolean enabled,
            java.time.Instant createdAt
    ) implements UsuarioResumenProjection {
        @Override public UUID getId() { return id; }
        @Override public String getNombreCompleto() { return nombreCompleto; }
        @Override public String getCorreo() { return correo; }
        @Override public String getDocumento() { return documento; }
        @Override public String getRolNombre() { return rolNombre; }
        @Override public String getTipoUsuario() { return tipoUsuario; }
        @Override public Boolean getEnabled() { return enabled; }
        @Override public java.time.Instant getCreatedAt() { return createdAt; }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResumenProjection> consultarUsuarios(UsuarioFiltroRequest filtro, Pageable pageable) {
        if (filtro != null && filtro.rolNombre() != null && !filtro.rolNombre().isBlank()) {
            try {
                RolEnum.valueOf(filtro.rolNombre().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("El rol '" + filtro.rolNombre() + "' no es un rol válido del dominio.");
            }
        }
        Specification<Usuario> spec = UsuarioSpecifications.construir(filtro);
        Page<Usuario> usuarios = usuarioRepository.findAll(spec, pageable);
        if (usuarios.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron usuarios para los criterios aplicados.");
        }
        return usuarios.map(usuario -> new UsuarioResumenProjectionImpl(
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getCorreo(),
                usuario.getDocumento(),
                usuario.getRol().getNombre(),
                usuario.getTipoUsuario(),
                usuario.isEnabled(),
                usuario.getCreatedAt()
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResumenProjection> consultarUsuarios(UsuarioFiltroRequest filtro) {
        if (filtro != null && filtro.rolNombre() != null && !filtro.rolNombre().isBlank()) {
            try {
                RolEnum.valueOf(filtro.rolNombre().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("El rol '" + filtro.rolNombre() + "' no es un rol válido del dominio.");
            }
        }
        Specification<Usuario> spec = UsuarioSpecifications.construir(filtro);
        List<Usuario> usuarios = usuarioRepository.findAll(spec);
        if (usuarios.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron usuarios para los criterios aplicados.");
        }
        return usuarios.stream().map(usuario -> new UsuarioResumenProjectionImpl(
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getCorreo(),
                usuario.getDocumento(),
                usuario.getRol().getNombre(),
                usuario.getTipoUsuario(),
                usuario.isEnabled(),
                usuario.getCreatedAt()
        )).map(impl -> (UsuarioResumenProjection) impl).toList();
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
