package io.github.rav316.nullablevalue;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NullableValueTest {

    @Test
    void undefinedIsNotPresent() {
        NullableValue<String> value = NullableValue.undefined();
        assertThat(value.isPresent()).isFalse();
    }

    @Test
    void undefinedGetThrows() {
        NullableValue<String> value = NullableValue.undefined();
        assertThatThrownBy(value::get)
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Value is undefined");
    }

    @Test
    void ofNullIsPresent() {
        NullableValue<String> value = NullableValue.of(null);
        assertThat(value.isPresent()).isTrue();
        assertThat(value.get()).isNull();
    }

    @Test
    void ofValueIsPresent() {
        NullableValue<String> value = NullableValue.of("hello");
        assertThat(value.isPresent()).isTrue();
        assertThat(value.get()).isEqualTo("hello");
    }

    @Test
    void orElseReturnsValueWhenPresent() {
        NullableValue<String> value = NullableValue.of("hello");
        assertThat(value.orElse("default")).isEqualTo("hello");
    }

    @Test
    void orElseReturnsOtherWhenUndefined() {
        NullableValue<String> value = NullableValue.undefined();
        assertThat(value.orElse("default")).isEqualTo("default");
    }

    @Test
    void orElseReturnsNullWhenPresentNull() {
        NullableValue<String> value = NullableValue.of(null);
        assertThat(value.orElse("default")).isNull();
    }

    @Test
    void ifPresentExecutesWhenPresent() {
        NullableValue<String> value = NullableValue.of("hello");
        AtomicReference<String> result = new AtomicReference<>();
        value.ifPresent(result::set);
        assertThat(result.get()).isEqualTo("hello");
    }

    @Test
    void ifPresentDoesNothingWhenUndefined() {
        NullableValue<String> value = NullableValue.undefined();
        AtomicReference<String> result = new AtomicReference<>("unchanged");
        value.ifPresent(result::set);
        assertThat(result.get()).isEqualTo("unchanged");
    }

    @Test
    void mapTransformsValue() {
        NullableValue<String> value = NullableValue.of("hello");
        NullableValue<Integer> mapped = value.map(String::length);
        assertThat(mapped.isPresent()).isTrue();
        assertThat(mapped.get()).isEqualTo(5);
    }

    @Test
    void mapReturnsUndefinedWhenUndefined() {
        NullableValue<String> value = NullableValue.undefined();
        NullableValue<Integer> mapped = value.map(String::length);
        assertThat(mapped.isPresent()).isFalse();
    }

    @Test
    void mapReturnsNullWhenNull() {
        NullableValue<String> value = NullableValue.of(null);
        NullableValue<Integer> mapped = value.map(String::length);
        assertThat(mapped.isPresent()).isTrue();
        assertThat(mapped.get()).isNull();
    }

    @Test
    void equalsAndHashCode() {
        NullableValue<String> a = NullableValue.of("hello");
        NullableValue<String> b = NullableValue.of("hello");
        NullableValue<String> c = NullableValue.of("world");
        NullableValue<String> d = NullableValue.undefined();
        NullableValue<String> e = NullableValue.undefined();

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(d);
        assertThat(d).isEqualTo(e);
    }

    @Test
    void toStringShowsValue() {
        assertThat(NullableValue.of("hello").toString()).isEqualTo("NullableValue[hello]");
        assertThat(NullableValue.of(null).toString()).isEqualTo("NullableValue[null]");
        assertThat(NullableValue.undefined().toString()).isEqualTo("NullableValue.undefined");
    }
}
