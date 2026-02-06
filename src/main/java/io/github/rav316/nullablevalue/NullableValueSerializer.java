package io.github.rav316.nullablevalue;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class NullableValueSerializer extends ValueSerializer<NullableValue<?>> {

    private final ValueSerializer<Object> contentSerializer;

    public NullableValueSerializer() {
        this.contentSerializer = null;
    }

    private NullableValueSerializer(ValueSerializer<Object> contentSerializer) {
        this.contentSerializer = contentSerializer;
    }

    @Override
    public void serialize(NullableValue<?> value, JsonGenerator gen, SerializationContext ctxt) {
        if (!value.isPresent()) {
            gen.writeNull();
            return;
        }
        Object content = value.get();
        if (content == null) {
            gen.writeNull();
        } else if (contentSerializer != null) {
            contentSerializer.serialize(content, gen, ctxt);
        } else {
            gen.writePOJO(content);
        }
    }

    @Override
    public boolean isEmpty(SerializationContext ctxt, NullableValue<?> value) {
        return value == null || !value.isPresent();
    }

    @Override
    public ValueSerializer<?> createContextual(SerializationContext ctxt, BeanProperty property) {
        if (property != null) {
            JavaType contentType = property.getType().containedType(0);
            if (contentType != null) {
                ValueSerializer<Object> cs = ctxt.findValueSerializer(contentType);
                return new NullableValueSerializer(cs);
            }
        }
        return this;
    }
}
