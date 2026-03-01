package com.safetrip.backend.application.usecase;

import com.safetrip.backend.application.service.OtpService;
import com.safetrip.backend.domain.exception.InvalidOtpException;
import com.safetrip.backend.domain.exception.UserNotFoundException;
import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.domain.repository.UserRepository;
import com.safetrip.backend.infrastructure.security.JwtService;
import org.springframework.stereotype.Component;

/**
 * Caso de uso para verificar OTP durante el login o recuperación de contraseña.
 * Soporta verificación por teléfono o email.
 */
@Component
public class VerifyOtpUseCase {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtService jwtService;

    public VerifyOtpUseCase(UserRepository userRepository,
                            OtpService otpService,
                            JwtService jwtService) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.jwtService = jwtService;
    }

    /**
     * Ejecuta la verificación del OTP para un identificador dado (email o teléfono).
     *
     * @param identifier El email o teléfono del usuario
     * @param otp El código OTP a verificar
     * @return Token JWT si la verificación es exitosa
     * @throws UserNotFoundException si el usuario no existe
     * @throws InvalidOtpException si el OTP es inválido o ha expirado
     */
    public String execute(String identifier, String otp) {
        // Buscar usuario por email o teléfono
        User user = findUserByIdentifier(identifier);

        // Verificar el OTP
        boolean isValid = otpService.verifyOtp(user, otp);
        if (!isValid) {
            throw new InvalidOtpException("OTP inválido o expirado");
        }

        // Activar usuario si no está activo (primer login)
        if (!user.getIsActive()) {
            User activatedUser = user.activate();
            userRepository.save(activatedUser);
        }

        // Generar y retornar token JWT
        return jwtService.generateToken(user.getUserId());
    }

    /**
     * Busca un usuario por email o teléfono.
     * Intenta primero con email, si no existe intenta con teléfono.
     *
     * @param identifier Email o teléfono del usuario
     * @return Usuario encontrado
     * @throws UserNotFoundException si no se encuentra el usuario
     */
    private User findUserByIdentifier(String identifier) {
        // Intentar buscar por email (si contiene @)
        if (identifier.contains("@")) {
            return userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new UserNotFoundException(
                            "Usuario no encontrado con email: " + identifier));
        }

        // Si no, buscar por teléfono
        return userRepository.findByPhone(identifier)
                .orElseThrow(() -> new UserNotFoundException(
                        "Usuario no encontrado con teléfono: " + identifier));
    }
}