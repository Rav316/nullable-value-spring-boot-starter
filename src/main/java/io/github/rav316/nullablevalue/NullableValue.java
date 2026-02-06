package io.github.rav316.nullablevalue;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class NullableValue<T> {

    private static final NullableValue<?> UNDEFINED = new NullableValue<>(null, false);

    private final T value;
    private final boolean present;

    private NullableValue(T value, boolean present) {
        this.value = value;
        this.present = present;
    }

    @SuppressWarnings("unchecked")
    public static <T> NullableValue<T> undefined() {
        return (NullableValue<T>) UNDEFINED;
    }

    public static <T> NullableValue<T> of(T value) {
        return new NullableValue<>(value, true);
    }

    public boolean isPresent() {
        return present;
    }

    public T get() {
        if (!present) {
            throw new NoSuchElementException("Value is undefined");
        }
        return value;
    }

    public T orElse(T other) {
        return present ? value : other;
    }

    public void ifPresent(Consumer<? super T> action) {
        if (present) {
            action.accept(value);
        }
    }

    public <U> NullableValue<U> map(Function<? super T, ? extends U> mapper) {
        if (!present) return undefined();
        return value == null ? of(null) : of(mapper.apply(value));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NullableValue<?> that)) return false;
        return present == that.present && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, present);
    }

    @Override
    public String toString() {
        return present ? "NullableValue[" + value + "]" : "NullableValue.undefined";
    }
}
