package com.learnmanager.dto.response;

import java.util.List;

public record TestDataGenerationResponse(int usersCreated, List<String> userEmails, String password) {

}