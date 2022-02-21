package io.reflectoring.testing.web;


import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reflectoring.testing.web.validation.ErrorResult;
import io.reflectoring.testing.web.validation.FieldValidationError;
import org.springframework.test.web.servlet.ResultMatcher;
import static org.assertj.core.api.Java6Assertions.*;

public class ResponseBodyMatchers {

  private ObjectMapper objectMapper = new ObjectMapper();

  public <T> ResultMatcher containsObjectAsJson(Object expectedObject, Class<T> targetClass) {
    return mvcResult -> {
      String json = mvcResult.getResponse().getContentAsString();
      T actualObject = objectMapper.readValue(json, targetClass);
      assertThat(actualObject).isEqualToComparingFieldByField(expectedObject);
    };
  }

  public ResultMatcher containsError(String expectedFieldName, String expectedMessage) {
    return mvcResult -> {
      String json = mvcResult.getResponse().getContentAsString();
      ErrorResult errorResult = objectMapper.readValue(json, ErrorResult.class);
      List<FieldValidationError> fieldErrors = errorResult.getFieldErrors().stream()
              .filter(fieldError -> fieldError.getField().equals(expectedFieldName))
              .filter(fieldError -> fieldError.getMessage().equals(expectedMessage))
              .collect(Collectors.toList());

      assertThat(fieldErrors)
              .withFailMessage("expecting exactly 1 error message with field name '%s' and message '%s'",
                      expectedFieldName,
                      expectedMessage)
              .hasSize(1);
    };
  }

  public ResultMatcher containsErrors(ErrorResult expectedError) {
    return mvcResult -> {
      String json = mvcResult.getResponse().getContentAsString();
      ErrorResult actualError = objectMapper.readValue(json, ErrorResult.class);

      assertThat(actualError.getFieldErrors())
              .withFailMessage("expected error %s doesn't match the actual error %s ",
                      expectedError.getFieldErrors(),
                      actualError.getFieldErrors())
              .hasSameElementsAs(expectedError.getFieldErrors());
    };
  }


  static ResponseBodyMatchers responseBody() {
    return new ResponseBodyMatchers();
  }

}
