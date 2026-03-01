package com.safetrip.backend.application.service.impl;

import com.safetrip.backend.domain.exception.InvalidCredentialsException;
import com.safetrip.backend.domain.exception.PasswordMismatchException;
import com.safetrip.backend.domain.exception.UserNotActiveException;
import com.safetrip.backend.domain.exception.UserNotFoundException;
import com.safetrip.backend.domain.model.enums.NotificationType;
import com.safetrip.backend.web.dto.mapper.LoginResponseMapper;
import com.safetrip.backend.web.dto.mapper.RegisterRequestMapper;
import com.safetrip.backend.application.service.AuthService;
import com.safetrip.backend.application.service.OtpService;
import com.safetrip.backend.application.usecase.SendOtpUseCase;
import com.safetrip.backend.application.usecase.VerifyOtpUseCase;
import com.safetrip.backend.domain.model.Person;
import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.domain.repository.PersonRepository;
import com.safetrip.backend.domain.repository.UserRepository;
import com.safetrip.backend.infrastructure.security.JwtService;
import com.safetrip.backend.web.dto.request.LoginOtpRequest;
import com.safetrip.backend.web.dto.request.LoginRequest;
import com.safetrip.backend.web.dto.request.RegisterRequest;
import com.safetrip.backend.web.dto.request.ResetPasswordRequest;
import com.safetrip.backend.web.dto.response.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final RegisterRequestMapper registerMapper;
    private final SendOtpUseCase sendOtpUseCase;
    private final VerifyOtpUseCase verifyOtpUseCase;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    public AuthServiceImpl(UserRepository userRepository,
                           PersonRepository personRepository,
                           RegisterRequestMapper registerMapper,
                           SendOtpUseCase sendOtpUseCase,
                           VerifyOtpUseCase verifyOtpUseCase,
                           JwtService jwtService,
                           PasswordEncoder passwordEncoder,
                           OtpService otpService) {
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.registerMapper = registerMapper;
        this.sendOtpUseCase = sendOtpUseCase;
        this.verifyOtpUseCase = verifyOtpUseCase;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail().toLowerCase())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (!user.getIsActive()) {
            throw new UserNotActiveException("La cuenta no está verificada o está inactiva");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        String token = jwtService.generateToken(user.getUserId());

        return LoginResponseMapper.toDto(user, token);
    }

    @Override
    public LoginResponse verifyOtp(LoginOtpRequest loginRequest) {
        User user;
        String identifier;

        if (loginRequest.getEmail() != null && !loginRequest.getEmail().isEmpty()) {
            identifier = loginRequest.getEmail().trim().toLowerCase();

            user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con email: " + identifier));
        } else {
            identifier = loginRequest.getPhone();
            user = userRepository.findByPhone(identifier)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con teléfono: " + identifier));
        }

        if (!user.getIsActive()) {
            user = user.activate();
            user = userRepository.save(user);
        }

        String token = verifyOtpUseCase.execute(
                identifier,
                loginRequest.getOtp()
        );

        return LoginResponseMapper.toDto(user, token);
    }


    @Override
    @Transactional
    public User registerUser(RegisterRequest registerRequest) {
        User user = registerMapper.toDomain(registerRequest);

        String normalizedEmail = registerRequest.getEmail().trim().toLowerCase();
        String phoneNumber = registerRequest.getPhone();

        // ✅ Validar si existe usuario con este teléfono
        Optional<User> existingUserByPhone = userRepository.findByPhone(phoneNumber);

        if (existingUserByPhone.isPresent()) {
            User existingUser = existingUserByPhone.get();

            // Si el usuario existe Y está activo, lanzar error
            if (existingUser.getIsActive()) {
                throw new InvalidCredentialsException("Ya existe una cuenta activa con este número de teléfono");
            }

            // Si existe pero NO está activo, reenviar OTP y retornar el usuario existente
            otpService.cleanupExpiredOtps(existingUser);
            sendOtpUseCase.execute(existingUser, NotificationType.ALL);
            return existingUser;
        }

        // ✅ Validar si existe usuario con este email
        Optional<User> existingUserByEmail = userRepository.findByEmail(normalizedEmail);

        if (existingUserByEmail.isPresent()) {
            User existingUser = existingUserByEmail.get();

            // Si el usuario existe Y está activo, lanzar error
            if (existingUser.getIsActive()) {
                throw new InvalidCredentialsException("Ya existe una cuenta activa con este correo electrónico");
            }

            // Si existe pero NO está activo, reenviar OTP y retornar el usuario existente
            otpService.cleanupExpiredOtps(existingUser);
            sendOtpUseCase.execute(existingUser, NotificationType.ALL);
            return existingUser;
        }

        // Buscar persona existente por documento
        Optional<Person> existingPersonOpt = personRepository.findByDocument(
                registerRequest.getPerson().getDocumentType(),
                registerRequest.getPerson().getDocumentNumber()
        );

        Person personToUse;

        if (existingPersonOpt.isPresent()) {
            Person existingPerson = existingPersonOpt.get();

            existingPerson.updateFullName(registerRequest.getPerson().getFullName());
            existingPerson.updateAddress(registerRequest.getPerson().getAddress());
            existingPerson.updateDocumentNumber(registerRequest.getPerson().getDocumentNumber());
            existingPerson.updateDocumentType(registerRequest.getPerson().getDocumentType());

            personToUse = personRepository.save(existingPerson);
        } else {
            personToUse = personRepository.save(user.getPerson());
        }

        // Crear usuario con la persona
        User userToSave = new User(
                null,
                personToUse,
                normalizedEmail, // Usar email normalizado
                user.getPhone(),
                user.getPasswordHash(),
                user.getRole(),
                user.getIsActive(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                null
        );

        try {
            User userSaved = userRepository.save(userToSave);
            sendOtpUseCase.execute(userSaved, NotificationType.ALL);
            return userSaved;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al registrar el usuario: " + e.getMessage(),
                    e
            );
        }
    }

    @Override
    public String sendOtp(String phoneNumber) {
        User user = userRepository.findByPhone(phoneNumber)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con teléfono: " + phoneNumber));

        otpService.cleanupExpiredOtps(user);
        sendOtpUseCase.execute(user, NotificationType.WHATSAPP_WITH_EMAIL_FALLBACK);

        return "OTP enviado al teléfono " + phoneNumber;
    }

    @Override
    public String sendPasswordResetOtp(String recipient, NotificationType notificationType) {
        User user;

        // 🔹 Normalizar si es email
        if (notificationType == NotificationType.EMAIL) {
            String normalizedEmail = recipient.trim().toLowerCase();

            user = userRepository.findByEmail(normalizedEmail)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con email: " + normalizedEmail));

            otpService.cleanupExpiredOtps(user);
            sendOtpUseCase.execute(user, NotificationType.EMAIL);
            return "OTP enviado al correo: " + normalizedEmail;

        } else {
            user = userRepository.findByPhone(recipient)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con teléfono: " + recipient));

            otpService.cleanupExpiredOtps(user);
            sendOtpUseCase.execute(user, NotificationType.WHATSAPP);
            return "OTP enviado al WhatsApp: " + recipient;
        }
    }

    @Override
    @Transactional
    public String resetPassword(User user, ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Las contraseñas no coinciden");
        }

        String hashedPassword = passwordEncoder.encode(request.getNewPassword());

        User updatedUser = user.updatePassword(hashedPassword);
        userRepository.save(updatedUser);

        return "Contraseña actualizada exitosamente";
    }

    @Override
    public LoginResponse getUserInfo(User user) {
        return LoginResponseMapper.toDto(user, null);
    }
}