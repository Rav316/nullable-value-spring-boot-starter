package io.github.rav316.nullablevalue.autoconfigure;

import io.github.rav316.nullablevalue.NullableValueExtractor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(name = "jakarta.validation.valueextraction.ValueExtractor")
public class NullableValueValidationAutoConfiguration {

    @Bean
    public NullableValueExtractor nullableValueExtractor() {
        return new NullableValueExtractor();
    }
}
