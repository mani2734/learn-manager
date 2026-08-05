package com.learnmanager.testsupport;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public abstract class AbstractControllerTest {

  private static final String TEST_USER_EMAIL = "user@learnmanager.local";

  private static final Validator NO_OP_VALIDATOR = new Validator() {

    @Override
    public boolean supports(Class<?> clazz) {
      return true;
    }

    @Override
    public void validate(Object target, Errors errors) {
      // Request validation is intentionally excluded from status-only tests.
    }
  };

  protected MockMvc mockMvc;

  protected MockMvc buildMockMvc(Object controller) {
    return MockMvcBuilders.standaloneSetup(controller).setValidator(NO_OP_VALIDATOR).build();
  }

  protected Authentication authentication() {
    return new TestingAuthenticationToken(TEST_USER_EMAIL, null, "ROLE_USER");
  }

  protected MockHttpServletRequestBuilder json(
      MockHttpServletRequestBuilder requestBuilder) {

    return requestBuilder.contentType(MediaType.APPLICATION_JSON).content("{}");
  }
}