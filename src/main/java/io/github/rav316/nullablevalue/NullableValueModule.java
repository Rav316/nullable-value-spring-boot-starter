package io.github.rav316.nullablevalue;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.module.SimpleModule;

public class NullableValueModule extends SimpleModule {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public NullableValueModule() {
        super("NullableValueModule");
        addSerializer((Class) NullableValue.class, new NullableValueSerializer());
        addDeserializer((Class) NullableValue.class, new NullableValueDeserializer());
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.configOverride(NullableValue.class)
                .setInclude(JsonInclude.Value.construct(
                        JsonInclude.Include.NON_EMPTY,
                        JsonInclude.Include.NON_EMPTY));
    }
}
