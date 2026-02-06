package io.github.rav316.nullablevalue;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.UnwrapByDefault;
import jakarta.validation.valueextraction.ValueExtractor;

@UnwrapByDefault
public class NullableValueExtractor implements ValueExtractor<NullableValue<@ExtractedValue ?>> {

    @Override
    public void extractValues(NullableValue<?> originalValue, ValueReceiver receiver) {
        if (originalValue == null || !originalValue.isPresent()) {
            return;
        }
        receiver.value(null, originalValue.get());
    }
}
