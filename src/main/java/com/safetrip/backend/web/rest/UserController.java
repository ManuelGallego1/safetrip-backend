package com.safetrip.backend.web.rest;

import com.safetrip.backend.application.service.UserService;
import com.safetrip.backend.domain.exception.InvalidCredentialsException;
import com.safetrip.backend.domain.model.User;
import com.safetrip.backend.web.dto.request.UserRequest;
import com.safetrip.backend.web.dto.response.ApiResponse;
import com.safetrip.backend.web.dto.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

 private final UserService userService;

 public UserController(UserService userService) {
  this.userService = userService;
 }

 @PatchMapping("/me")
 public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
         @Valid @RequestBody UserRequest request) {

  User updatedUser = userService.updateUser(request);
  UserResponse response = UserResponse.fromDomain(updatedUser);

  return ResponseEntity.ok(
          ApiResponse.success("Usuario actualizado exitosamente", response)
  );
 }

 @GetMapping("/me")
 public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
  var authentication = SecurityContextHolder.getContext().getAuthentication();

  if (authentication == null || !authentication.isAuthenticated()) {
   throw new InvalidCredentialsException("Usuario no autenticado o token inválido");
  }

  Object principal = authentication.getPrincipal();

  if (principal instanceof User user) {
   UserResponse response = UserResponse.fromDomain(user);
   return ResponseEntity.ok(
           ApiResponse.success("Información del usuario obtenida exitosamente", response)
   );
  }

  throw new InvalidCredentialsException("No se pudo obtener la información del usuario autenticado");
 }
}