package com.safetrip.backend.application.service.impl;

import com.safetrip.backend.domain.exception.InvalidCredentialsException;
import com.safetrip.backend.domain.exception.UserNotActiveException;
import com.safetrip.backend.domain.exception.UserNotFoundException;
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
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (!user.getIsActive()) {
            throw new UserNotActiveException("La cuenta no está verificada o está inactiva");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        String token = jwtService.generateToken(user.getPhone());

        return LoginResponseMapper.toDto(user, token);
    }

    @Override
    public LoginResponse verifyOtp(LoginOtpRequest loginRequest) {
        User user = userRepository.findByPhone(loginRequest.getPhone())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        // Activar usuario si no está activo
        if (!user.getIsActive()) {
            user = user.activate();
            user = userRepository.save(user);
        }

        String token = verifyOtpUseCase.execute(
                loginRequest.getPhone(),
                loginRequest.getOtp()
        );

        return LoginResponseMapper.toDto(user, token);
    }

    @Override
    @Transactional
    public User registerUser(RegisterRequest registerRequest) {
        User user = registerMapper.toDomain(registerRequest);

        // Validar que no exista el teléfono
        if (userRepository.findByPhone(registerRequest.getPhone()).isPresent()) {
            throw new InvalidCredentialsException("Ya existe un usuario con este número de teléfono");
        }

        // Buscar o crear persona
        Optional<Person> existingPerson = personRepository.findByDocument(
                registerRequest.getPerson().getDocumentType(),
                registerRequest.getPerson().getDocumentNumber()
        );

        Person personToUse = existingPerson.orElseGet(() -> personRepository.save(user.getPerson()));

        // Crear usuario con la persona
        User userToSave = new User(
                null,
                personToUse,
                user.getEmail(),
                user.getPhone(),
                user.getPasswordHash(),
                user.getRole(),
                user.getIsActive(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                null
        );

        try {
            return userRepository.save(userToSave);
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
        sendOtpUseCase.execute(user);

        return "OTP enviado al teléfono " + phoneNumber;
    }

    @Override
    public LoginResponse getUserInfo(User user) {
        return LoginResponseMapper.toDto(user, null);
    }
}