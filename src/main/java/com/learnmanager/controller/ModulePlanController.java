package com.learnmanager.controller;

import com.learnmanager.dto.CreateModulePlanRequest;
import com.learnmanager.dto.ModulePlanResponse;
import com.learnmanager.service.ModulePlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/modulePlans/")
@RequiredArgsConstructor
public class ModulePlanController {

  private final ModulePlanService modulePlanService;

  @PostMapping("create")
  @ResponseStatus(HttpStatus.CREATED)
  public ModulePlanResponse create(Authentication authentication, @Valid @RequestBody CreateModulePlanRequest request) {
    return modulePlanService.create(authentication.getName(), request);
  }
}