# NullableValue Spring Boot Starter

[![Build](https://github.com/rav316/nullable-value-spring-boot-starter/actions/workflows/ci.yml/badge.svg)](https://github.com/rav316/nullable-value-spring-boot-starter/actions/workflows/ci.yml)

Spring Boot starter providing `NullableValue<T>` — a wrapper that distinguishes three states of a JSON field:

| State         | Meaning                              | Example JSON        |
|---------------|--------------------------------------|---------------------|
| **undefined** | Field is absent in JSON              | `{"name": "John"}`  |
| **null**      | Field is present with `null` value   | `{"email": null}`   |
| **value**     | Field is present with actual value   | `{"age": 30}`       |

This is especially useful for **PATCH requests** in REST APIs where you need to differentiate between "don't change this field" (undefined) and "set this field to null" (null).

## Requirements

- Java 21+
- Spring Boot 3.x / 4.x
- Jackson 3.x (`tools.jackson`)
- Jakarta Validation API 3.1+

## Installation

### Add JitPack repository

First, add the JitPack repository to your build configuration:

**Gradle:**
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}
```

**Maven:**
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

### Add dependency

**Gradle:**
```gradle
dependencies {
    implementation 'io.github.rav316:nullable-value-spring-boot-starter:1.0.0'
}
```

**Maven:**
```xml
<dependency>
    <groupId>io.github.rav316</groupId>
    <artifactId>nullable-value-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

### DTO class

```java
public class UserUpdateDto {
    private NullableValue<String> name = NullableValue.undefined();
    private NullableValue<String> email = NullableValue.undefined();
    private NullableValue<Integer> age = NullableValue.undefined();

    public NullableValue<String> getName() { return name; }
    public void setName(NullableValue<String> name) { this.name = name; }

    public NullableValue<String> getEmail() { return email; }
    public void setEmail(NullableValue<String> email) { this.email = email; }

    public NullableValue<Integer> getAge() { return age; }
    public void setAge(NullableValue<Integer> age) { this.age = age; }
}
```

### JSON examples

```jsonc
// 1. Field is absent (undefined) — will not be changed
{"name": "John"}
// email = undefined, age = undefined

// 2. Field is null — will be set to null
{"name": "John", "email": null}
// email = NullableValue.of(null), age = undefined

// 3. Field has a value
{"name": "John", "email": "john@example.com", "age": 30}
```

### Controller

```java
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id,
                                           @RequestBody UserUpdateDto dto) {
        User user = userService.update(id, dto);
        return ResponseEntity.ok(user);
    }
}
```

### Service

```java
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User update(Long id, UserUpdateDto dto) {
        User user = userRepository.findById(id).orElseThrow();

        dto.getName().ifPresent(user::setName);
        dto.getEmail().ifPresent(user::setEmail);
        dto.getAge().ifPresent(user::setAge);

        return userRepository.save(user);
    }
}
```

### Validation

`NullableValue` supports Jakarta Bean Validation. Constraints are applied to the wrapped value only when the field is present:

```java
public class UserUpdateDto {
    private NullableValue<@NotBlank String> name = NullableValue.undefined();
    private NullableValue<@Email String> email = NullableValue.undefined();
    private NullableValue<@Min(0) @Max(150) Integer> age = NullableValue.undefined();

    // getters/setters
}
```

## NullableValue API

| Method                        | Description                                              |
|-------------------------------|----------------------------------------------------------|
| `NullableValue.undefined()`   | Creates an undefined (absent) value                      |
| `NullableValue.of(value)`     | Creates a present value (value can be `null`)            |
| `isPresent()`                 | Returns `true` if the field was present in JSON          |
| `get()`                       | Returns the value; throws if undefined                   |
| `orElse(other)`               | Returns the value if present, otherwise `other`          |
| `ifPresent(consumer)`         | Executes the consumer if the value is present            |
| `map(function)`               | Transforms the value if present                          |

## Auto-configuration

The starter automatically registers:
- Jackson module for serialization/deserialization of `NullableValue`
- Jakarta Bean Validation `ValueExtractor` for `NullableValue`

No additional configuration is required.

## License

[Apache License, Version 2.0](LICENSE)
