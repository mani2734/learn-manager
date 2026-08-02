package com.learnmanager.controller;

import com.learnmanager.dto.response.AdminUserResponse;
import com.learnmanager.dto.response.TestDataGenerationResponse;
import com.learnmanager.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/")
@RequiredArgsConstructor
public class AdminController {

  private final AdminService adminService;

  @GetMapping("users/getAll")
  public List<AdminUserResponse> getAllUsers() {
    return adminService.getAllUsers();
  }

  @GetMapping("testData/generate")
  public TestDataGenerationResponse generateTestData() {
    return adminService.generateTestData();
  }

  @GetMapping("users/get/{id}")
  public AdminUserResponse getUserById(@PathVariable Long id) {
    return adminService.getUserById(id);
  }

  @PutMapping("users/deactivate/{id}")
  public AdminUserResponse deactivateUser(@PathVariable Long id) {
    return adminService.deactivateUser(id);
  }

  @PutMapping("users/activate/{id}")
  public AdminUserResponse activateUser(@PathVariable Long id) {
    return adminService.activateUser(id);
  }
}