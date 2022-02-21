package io.reflectoring.testing.web.validation;

import java.util.ArrayList;
import java.util.List;

import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor
public class ErrorResult {
  List<FieldValidationError> fieldErrors = new ArrayList<>();

  public ErrorResult(String field, String message) {
    this.fieldErrors.add(new FieldValidationError(field, message));
  }

  public ErrorResult(List<FieldValidationError> fieldErrors) {
    this.fieldErrors.addAll(fieldErrors);
  }
}