package io.github.rav316.nullablevalue;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;

public class NullableValueDeserializer extends ValueDeserializer<NullableValue<?>> {

    private final JavaType contentType;
    private final ValueDeserializer<Object> contentDeserializer;

    public NullableValueDeserializer() {
        this.contentType = null;
        this.contentDeserializer = null;
    }

    private NullableValueDeserializer(JavaType contentType, ValueDeserializer<Object> contentDeserializer) {
        this.contentType = contentType;
        this.contentDeserializer = contentDeserializer;
    }

    @Override
    public NullableValue<?> deserialize(JsonParser p, DeserializationContext ctxt) {
        Object value = contentDeserializer != null
                ? contentDeserializer.deserialize(p, ctxt)
                : ctxt.readTreeAsValue(ctxt.readTree(p), contentType != null ? contentType : ctxt.constructType(Object.class));
        return NullableValue.of(value);
    }

    @Override
    public NullableValue<?> getNullValue(DeserializationContext ctxt) {
        return NullableValue.of(null);
    }

    @Override
    public Object getAbsentValue(DeserializationContext ctxt) {
        return NullableValue.undefined();
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        JavaType wrapperType = property != null ? property.getType() : ctxt.getContextualType();
        if (wrapperType != null) {
            JavaType contentType = wrapperType.containedType(0);
            if (contentType != null) {
                ValueDeserializer<Object> cd = ctxt.findContextualValueDeserializer(contentType, property);
                return new NullableValueDeserializer(contentType, cd);
            }
        }
        return this;
    }
}
