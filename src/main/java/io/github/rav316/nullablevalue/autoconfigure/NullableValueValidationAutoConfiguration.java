package io.github.rav316.nullablevalue.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(name = "jakarta.validation.valueextraction.ValueExtractor")
public class NullableValueValidationAutoConfiguration {

    @Bean
    public Object nullableValueExtractor() {
        try {
            Class<?> extractorClass = Class.forName("io.github.rav316.nullablevalue.NullableValueExtractor");
            return extractorClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create NullableValueExtractor", e);
        }
    }
}
