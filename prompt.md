# ПРОМПТ ДЛЯ СОЗДАНИЯ БИБЛИОТЕКИ NULLABLE-VALUE

Создай Maven/Gradle библиотеку для Spring Boot проектов с названием `nullable-value-spring-boot-starter`.

## Описание
Библиотека предоставляет класс-обёртку `NullableValue<T>`, который позволяет различать три состояния поля при десериализации JSON:
- **undefined** - поле отсутствует в JSON
- **null** - поле присутствует в JSON со значением null
- **значение** - поле присутствует в JSON со значением

Это особенно полезно для PATCH-запросов в REST API, где нужно различать "не изменять поле" (undefined) и "установить поле в null" (null).

## Структура проекта

**Основной package:** `io.github.<твой_username>.nullablevalue` (или другой на твой выбор)

### Файлы для создания:

**1. NullableValue.java** - основной класс-обёртка:
```java
package io.github.rav316.nullablevalue;

import lombok.Getter;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class NullableValue<T> {

    private static final NullableValue<?> UNDEFINED = new NullableValue<>(null, false);

    private final T value;
    @Getter
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
```

**2. NullableValueSerializer.java** - Jackson сериализатор:
```java
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
```

**3. NullableValueDeserializer.java** - Jackson десериализатор:
```java
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
```

**4. NullableValueExtractor.java** - Jakarta Bean Validation экстрактор:
```java
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
```

**5. NullableValueAutoConfiguration.java** - Spring Boot автоконфигурация:
```java
package io.github.rav316.nullablevalue.autoconfigure;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.rav316.nullablevalue.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.module.SimpleModule;

@AutoConfiguration(before = JacksonAutoConfiguration.class)
public class NullableValueAutoConfiguration {

    @Bean
    public SimpleModule nullableValueModule() {
        SimpleModule module = new SimpleModule("NullableValueModule");
        module.addSerializer(NullableValue.class, new NullableValueSerializer());
        module.addDeserializer(NullableValue.class, new NullableValueDeserializer());
        return module;
    }

    @Bean
    public NullableValueExtractor nullableValueExtractor() {
        return new NullableValueExtractor();
    }
}
```

**6. spring.factories** - для Spring Boot 2.x совместимости (создать в `src/main/resources/META-INF/`):
```
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
io.github.<твой_username>.nullablevalue.autoconfigure.NullableValueAutoConfiguration
```

**7. org.springframework.boot.autoconfigure.AutoConfiguration.imports** - для Spring Boot 3.x+ (создать в `src/main/resources/META-INF/spring/`):
```
io.github.<твой_username>.nullablevalue.autoconfigure.NullableValueAutoConfiguration
```

**8. jakarta.validation.valueextraction.ValueExtractor** - для Jakarta Validation (создать в `src/main/resources/META-INF/services/`):
```
io.github.<твой_username>.nullablevalue.NullableValueExtractor
```

## Зависимости

**Для Gradle (build.gradle):**
```gradle
plugins {
    id 'java-library'
    id 'maven-publish'
}

group = 'io.github.<твой_username>'
version = '1.0.0'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    // Jackson 3.x (для tools.jackson)
    compileOnly 'tools.jackson.core:jackson-databind:3.0.4'

    // Lombok
    compileOnly 'org.projectlombok:lombok:1.18.34'
    annotationProcessor 'org.projectlombok:lombok:1.18.34'

    // Jakarta Validation
    compileOnly 'jakarta.validation:jakarta.validation-api:3.1.0'

    // Spring Boot (для автоконфигурации)
    compileOnly 'org.springframework.boot:spring-boot-autoconfigure:4.0.2'
    annotationProcessor 'org.springframework.boot:spring-boot-autoconfigure-processor:4.0.2'

    // Для Jackson 2.x совместимости (опционально)
    compileOnly 'com.fasterxml.jackson.core:jackson-databind:2.18.2'

    // Test dependencies
    testImplementation 'org.springframework.boot:spring-boot-starter-test:4.0.2'
    testImplementation 'org.springframework.boot:spring-boot-starter-web:4.0.2'
    testImplementation 'org.springframework.boot:spring-boot-starter-validation:4.0.2'
}

publishing {
    publications {
        maven(MavenPublication) {
            from components.java
            pom {
                name = 'NullableValue Spring Boot Starter'
                description = 'Spring Boot starter for NullableValue wrapper to distinguish undefined, null and actual values in JSON'
                url = 'https://github.com/<твой_username>/nullable-value-spring-boot-starter'
                licenses {
                    license {
                        name = 'The Apache License, Version 2.0'
                        url = 'http://www.apache.org/licenses/LICENSE-2.0.txt'
                    }
                }
            }
        }
    }
}
```

**Для Maven (pom.xml):**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.github.rav316</groupId>
    <artifactId>nullable-value-spring-boot-starter</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>NullableValue Spring Boot Starter</name>
    <description>Spring Boot starter for NullableValue wrapper</description>

    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <spring-boot.version>4.0.2</spring-boot.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>tools.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>3.0.4</version>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.34</version>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>jakarta.validation</groupId>
            <artifactId>jakarta.validation-api</artifactId>
            <version>3.1.0</version>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure</artifactId>
            <version>${spring-boot.version}</version>
            <optional>true</optional>
        </dependency>
    </dependencies>
</project>
```

## README.md

Создай README.md с примерами использования:
- Как подключить библиотеку
- Пример DTO с NullableValue полями
- Примеры JSON запросов (undefined vs null vs значение)
- Пример обработки в контроллере/сервисе

## Требования

1. **Java 21+**
2. **Поддержка Jackson 3.x** (tools.jackson) - приоритет
3. **Опциональная поддержка Jackson 2.x** (com.fasterxml.jackson) для обратной совместимости
4. **Spring Boot 3.x/4.x**
5. **Jakarta Validation API**
6. **Автоматическая конфигурация** через Spring Boot autoconfigure
7. **Публикация в Maven Central** или GitHub Packages

## Дополнительные задачи

1. Создай юнит-тесты для всех классов
2. Создай интеграционные тесты с Spring Boot
3. Настрой GitHub Actions для CI/CD
4. Добавь badge в README.md (build status, maven central version)
5. Создай LICENSE файл (Apache 2.0 или MIT)

## Пример использования (для README)

```java
// DTO класс
public class UserUpdateDto {
    private NullableValue<String> name;
    private NullableValue<String> email;
    private NullableValue<Integer> age;
    // getters/setters или @Data от Lombok
}

// JSON примеры:
// 1. Поле отсутствует (undefined) - не будет изменено
{"name": "John"}  // email и age = undefined

// 2. Поле null - будет установлено в null
{"name": "John", "email": null}  // email = null, age = undefined

// 3. Поле со значением
{"name": "John", "email": "john@example.com"}

// Обработка в сервисе
public void updateUser(Long id, UserUpdateDto dto) {
    User user = userRepository.findById(id).orElseThrow();

    dto.getName().ifPresent(user::setName);
    dto.getEmail().ifPresent(user::setEmail);
    dto.getAge().ifPresent(user::setAge);

    userRepository.save(user);
}
```

---

**Начни с создания базовой структуры проекта, затем реализуй все классы и тесты.**
