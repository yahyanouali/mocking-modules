package io.reflectoring.testing.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reflectoring.testing.domain.RegisterUseCase;
import io.reflectoring.testing.domain.User;
import io.reflectoring.testing.web.validation.ErrorResult;
import io.reflectoring.testing.web.validation.FieldValidationError;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static io.reflectoring.testing.web.ResponseBodyMatchers.responseBody;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RegisterRestController.class)
class RegisterRestControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private RegisterUseCase registerUseCase;

  @Test
  void whenValidUrlAndMethodAndContentType_thenReturns200() throws Exception {

    UserResource user = new UserResource("Zaphod", "zaphod@galaxy.net");

    mockMvc.perform(post("/forums/42/register")
            .param("sendWelcomeMail", "true")
            .content(objectMapper.writeValueAsString(user))
            .contentType("application/json"))
            .andExpect(status().isOk());

  }

  @Test
  void whenValidInput_thenReturns200() throws Exception {

    UserResource user = new UserResource("Zaphod", "zaphod@galaxy.net");

    mockMvc.perform(post("/forums/{forumId}/register", 42L)
            .contentType("application/json")
            .param("sendWelcomeMail", "true")
            .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isOk());

  }

  @Test
  void whenValidInput_thenMapsToBusinessModel() throws Exception {

    UserResource user = new UserResource("Zaphod", "zaphod@galaxy.net");

    mockMvc.perform(post("/forums/{forumId}/register", 42L)
            .contentType("application/json")
            .param("sendWelcomeMail", "true")
            .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isOk());

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(registerUseCase, times(1)).registerUser(userCaptor.capture(), eq(true));
    assertThat(userCaptor.getValue().getName()).isEqualTo("Zaphod");
    assertThat(userCaptor.getValue().getEmail()).isEqualTo("zaphod@galaxy.net");

  }

  @Test
  void whenValidInput_thenReturnsUserResource() throws Exception {

    UserResource user = new UserResource("Zaphod", "zaphod@galaxy.net");

    MvcResult mvcResult = mockMvc.perform(post("/forums/{forumId}/register", 42L)
            .contentType("application/json")
            .param("sendWelcomeMail", "true")
            .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isOk())
            .andReturn();

    UserResource expectedResponseBody = user;
    String actualResponseBody = mvcResult.getResponse().getContentAsString();
    assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
            objectMapper.writeValueAsString(expectedResponseBody));
  }

  @Test
  void whenValidInput_thenReturnsUserResource_withFluentApi() throws Exception {

    UserResource user = new UserResource("Zaphod", "zaphod@galaxy.net");
    UserResource expectedResponseBody = user;

    mockMvc.perform(post("/forums/{forumId}/register", 42L)
            .contentType("application/json")
            .param("sendWelcomeMail", "true")
            .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isOk())
            .andExpect(responseBody().containsObjectAsJson(expectedResponseBody, UserResource.class));
  }

  @Test
  void whenValidInput_thenReturnsUserResource_withTypedAssertion() throws Exception {

    UserResource user = new UserResource("Zaphod", "zaphod@galaxy.net");

    MvcResult mvcResult = mockMvc.perform(post("/forums/{forumId}/register", 42L)
            .contentType("application/json")
            .param("sendWelcomeMail", "true")
            .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isOk())
            .andReturn();

    UserResource expected = user;
    UserResource actualResponseBody = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserResource.class);
    assertThat(actualResponseBody.getName()).isEqualTo(expected.getName());
    assertThat(actualResponseBody.getEmail()).isEqualTo(expected.getEmail());

  }

  @Test
  void whenNullValue_thenReturns400() throws Exception {

    UserResource user = new UserResource(null, "zaphod@galaxy.net");

    mockMvc.perform(post("/forums/{forumId}/register", 42L)
            .contentType("application/json")
            .param("sendWelcomeMail", "true")
            .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isBadRequest());
  }

  @Test
  void whenNullValue_thenReturns400AndErrorResult() throws Exception {

    UserResource user = new UserResource(null, "zaphod@galaxy.net");

    MvcResult mvcResult = mockMvc.perform(post("/forums/{forumId}/register", 42L)
            .contentType("application/json")
            .param("sendWelcomeMail", "true")
            .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isBadRequest())
            .andReturn();

    ErrorResult expectedErrorResponse = new ErrorResult("name", "must not be null");
    String actualResponseBody = mvcResult.getResponse().getContentAsString();
    String expectedResponseBody = objectMapper.writeValueAsString(expectedErrorResponse);
    assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
  }

  @Test
  void whenNullValue_thenReturns400AndErrorResult_withFluentApi() throws Exception {

    UserResource user = new UserResource(null, "zaphod@galaxy.net");

    mockMvc.perform(post("/forums/{forumId}/register", 42L)
            .contentType("application/json")
            .param("sendWelcomeMail", "true")
            .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isBadRequest())
            .andExpect(responseBody().containsError("name", "must not be null"));
  }

  @Test
  void whenMultipleNullValue_thenReturns400AndErrorResult_withFluentApi() throws Exception {
    UserResource user = new UserResource(null, null);
    ErrorResult expectedError = new ErrorResult(
            List.of(new FieldValidationError("name", "must not be null"),
                    new FieldValidationError("email", "must not be null")));

    mockMvc.perform(post("/forums/{forumId}/register", 42L)
                    .contentType("application/json")
                    .param("sendWelcomeMail", "true")
                    .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isBadRequest())
            .andExpect(responseBody().containsErrors(expectedError));
  }

}