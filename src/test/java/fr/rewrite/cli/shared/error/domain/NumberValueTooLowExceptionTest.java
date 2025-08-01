package fr.rewrite.cli.shared.error.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class NumberValueTooLowExceptionTest {

  @Test
  void shouldGetExceptionInformation() {
    NumberValueTooLowException exception = NumberValueTooLowException.builder().field("myField").minValue("1337").value("42").build();

    assertThat(exception.type()).isEqualTo(AssertionErrorType.NUMBER_VALUE_TOO_LOW);
    assertThat(exception.field()).isEqualTo("myField");
    assertThat(exception.parameters()).containsOnly(entry("min", "1337"), entry("value", "42"));
    assertThat(exception.getMessage()).isEqualTo("Value of field \"myField\" must be at least 1337 but was 42");
  }
}
