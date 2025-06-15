package fr.rewrite.cli.shared.collection.domain;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RewriteBackCollectionsTest {

  @Nested
  @DisplayName("Collections")
  class RewriteBackCollectionsCollectionsTest {

    @Test
    void shouldGetEmptyImmutableCollectionFromNullCollection() {
      Collection<Object> input = null;
      Collection<Object> collection = RewriteBackCollections.immutable(input);

      assertThat(collection).isEmpty();
      assertThatThrownBy(collection::clear).isExactlyInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldGetImmutableCollectionFromMutableCollection() {
      Collection<String> input = new ArrayList<>();
      input.add("value");
      Collection<String> collection = RewriteBackCollections.immutable(input);

      assertThat(collection).containsExactly("value");
      assertThatThrownBy(collection::clear).isExactlyInstanceOf(UnsupportedOperationException.class);
    }
  }

  @Nested
  @DisplayName("Set")
  class RewriteBackCollectionsSetTest {

    @Test
    void shouldGetEmptyImmutableCollectionFromNullCollection() {
      Set<Object> input = null;
      Set<Object> set = RewriteBackCollections.immutable(input);

      assertThat(set).isEmpty();
      assertThatThrownBy(set::clear).isExactlyInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldGetImmutableCollectionFromMutableCollection() {
      Set<String> input = new HashSet<>();
      input.add("value");
      Set<String> set = RewriteBackCollections.immutable(input);

      assertThat(set).containsExactly("value");
      assertThatThrownBy(set::clear).isExactlyInstanceOf(UnsupportedOperationException.class);
    }
  }

  @Nested
  @DisplayName("List")
  class RewriteBackCollectionsListTest {

    @Test
    void shouldGetEmptyImmutableCollectionFromNullCollection() {
      List<Object> input = null;
      List<Object> list = RewriteBackCollections.immutable(input);

      assertThat(list).isEmpty();
      assertThatThrownBy(list::clear).isExactlyInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldGetImmutableCollectionFromMutableCollection() {
      List<String> input = new ArrayList<>();
      input.add("value");
      List<String> list = RewriteBackCollections.immutable(input);

      assertThat(list).containsExactly("value");
      assertThatThrownBy(list::clear).isExactlyInstanceOf(UnsupportedOperationException.class);
    }
  }

  @Nested
  @DisplayName("Map")
  class RewriteBackMapTest {

    @Test
    void shouldGetEmptyImmutableMapFromNullMap() {
      Map<Object, Object> input = null;
      Map<Object, Object> map = RewriteBackCollections.immutable(input);

      assertThat(map).isEmpty();
      assertThatThrownBy(map::clear).isExactlyInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldGetImmutableMapFromMutableMap() {
      Map<String, String> input = new HashMap<>();
      input.put("key", "value");
      Map<String, String> map = RewriteBackCollections.immutable(input);

      assertThat(map).containsExactly(Map.entry("key", "value"));
      assertThatThrownBy(map::clear).isExactlyInstanceOf(UnsupportedOperationException.class);
    }
  }
}
