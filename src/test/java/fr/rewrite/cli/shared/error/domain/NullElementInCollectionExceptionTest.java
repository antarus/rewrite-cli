package fr.rewrite.cli.shared.error.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class NullElementInCollectionExceptionTest {

  @Test
  void shouldGetExceptionInformation() {
    var exception = new NullElementInCollectionException("myField");

    assertThat(exception.type()).isEqualTo(AssertionErrorType.NULL_ELEMENT_IN_COLLECTION);
    assertThat(exception.field()).isEqualTo("myField");
    assertThat(exception.parameters()).isEmpty();
    assertThat(exception.getMessage()).isEqualTo("The field \"myField\" contains a null element");
  }
}
