package com.safetrip.backend.application.service.impl;

import com.safetrip.backend.application.exception.InvalidCredentialsException;
import com.safetrip.backend.application.exception.UserNotActiveException;
import com.safetrip.backend.application.exception.UserNotFoundException;
import com.safetrip.backend.application.mapper.LoginResponseMapper;
import com.safetrip.backend.application.mapper.RegisterRequestMapper;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final RegisterRequestMapper registerMapper;
    private final SendOtpUseCase  sendOtpUseCase;
    private final VerifyOtpUseCase verifyOtpUseCase;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    public AuthServiceImpl(UserRepository userRepository,
                           PersonRepository personRepository,
                           RegisterRequestMapper registerMapper,
                           SendOtpUseCase  sendOtpUseCase,
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
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.getIsActive()) {
            throw new UserNotActiveException("The account is not verified or is inactive");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getEmail());

        return LoginResponseMapper.toDto(user, token);
    }



    @Override
    public LoginResponse verifyOtp(LoginOtpRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.getIsActive()) {
            throw new UserNotActiveException("The account is not verified or is inactive");
        }


        String token =  verifyOtpUseCase.execute(
                loginRequest.getEmail(),
                loginRequest.getOtp()
        );

        return LoginResponseMapper.toDto(user, token);
    }

    @Override
    public User registerUser(RegisterRequest registerRequest) {
        User user = registerMapper.toDomain(registerRequest);

        Person savedPerson = personRepository.save(user.getPerson());

        User userToSave = new User(
                null,
                savedPerson,
                user.getEmail(),
                user.getPhone(),
                user.getPasswordHash(),
                user.getRole(),
                user.getIsActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );

        User savedUser = userRepository.save(userToSave);

        sendOtpUseCase.execute(savedUser);

        return savedUser;
    }

    @Override
    public String sendOtp(String phoneNumber) {
        User user = userRepository.findByPhone(phoneNumber)
                .orElseThrow(() -> new UserNotFoundException("User not found with phone: " + phoneNumber));
        otpService.cleanupExpiredOtps(user);
        sendOtpUseCase.execute(user);

        return "OTP sent to phone " + phoneNumber;
    }
}