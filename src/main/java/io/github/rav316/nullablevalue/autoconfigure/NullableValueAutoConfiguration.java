package io.github.rav316.nullablevalue.autoconfigure;

import io.github.rav316.nullablevalue.NullableValueExtractor;
import io.github.rav316.nullablevalue.NullableValueModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(before = JacksonAutoConfiguration.class)
public class NullableValueAutoConfiguration {

    @Bean
    public NullableValueModule nullableValueModule() {
        return new NullableValueModule();
    }

    @Bean
    public NullableValueExtractor nullableValueExtractor() {
        return new NullableValueExtractor();
    }
}
