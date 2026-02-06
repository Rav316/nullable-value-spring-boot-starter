package io.github.rav316.nullablevalue.autoconfigure;

import io.github.rav316.nullablevalue.NullableValueModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(before = JacksonAutoConfiguration.class)
@ConditionalOnClass(name = "tools.jackson.databind.ObjectMapper")
public class NullableValueAutoConfiguration {

    @Bean
    public NullableValueModule nullableValueModule() {
        return new NullableValueModule();
    }
}
