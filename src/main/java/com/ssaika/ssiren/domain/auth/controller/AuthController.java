package com.ssaika.ssiren.domain.auth.controller;

import com.ssaika.ssiren.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

    private final AuthService authService;
}
