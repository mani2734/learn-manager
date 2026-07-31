package com.learnmanager.controller;

import com.learnmanager.dto.StartTimerRequest;
import com.learnmanager.dto.TimerResponse;
import com.learnmanager.service.TimerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/timers/")
@RequiredArgsConstructor
public class TimerController {

  private final TimerService timerService;

  @PostMapping("start")
  @ResponseStatus(HttpStatus.CREATED)
  public TimerResponse start(Authentication authentication, @Valid @RequestBody StartTimerRequest request) {
    return timerService.start(authentication.getName(), request);
  }

  @GetMapping("getActive")
  public TimerResponse getActive(Authentication authentication) {
    return timerService.getActive(authentication.getName());
  }
}