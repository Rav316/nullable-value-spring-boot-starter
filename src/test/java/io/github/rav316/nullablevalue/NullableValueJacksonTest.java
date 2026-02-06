package io.github.rav316.nullablevalue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class NullableValueJacksonTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder()
                .addModule(new NullableValueModule())
                .build();
    }

    static class TestDto {
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

    @Test
    void deserializeWithValue() throws Exception {
        String json = """
                {"name": "John", "email": "john@example.com", "age": 30}
                """;
        TestDto dto = mapper.readValue(json, TestDto.class);

        assertThat(dto.getName().isPresent()).isTrue();
        assertThat(dto.getName().get()).isEqualTo("John");
        assertThat(dto.getEmail().isPresent()).isTrue();
        assertThat(dto.getEmail().get()).isEqualTo("john@example.com");
        assertThat(dto.getAge().isPresent()).isTrue();
        assertThat(dto.getAge().get()).isEqualTo(30);
    }

    @Test
    void deserializeWithNull() throws Exception {
        String json = """
                {"name": "John", "email": null}
                """;
        TestDto dto = mapper.readValue(json, TestDto.class);

        assertThat(dto.getName().isPresent()).isTrue();
        assertThat(dto.getName().get()).isEqualTo("John");
        assertThat(dto.getEmail().isPresent()).isTrue();
        assertThat(dto.getEmail().get()).isNull();
        assertThat(dto.getAge().isPresent()).isFalse();
    }

    @Test
    void deserializeWithUndefined() throws Exception {
        String json = """
                {"name": "John"}
                """;
        TestDto dto = mapper.readValue(json, TestDto.class);

        assertThat(dto.getName().isPresent()).isTrue();
        assertThat(dto.getName().get()).isEqualTo("John");
        assertThat(dto.getEmail().isPresent()).isFalse();
        assertThat(dto.getAge().isPresent()).isFalse();
    }

    @Test
    void serializeWithValue() throws Exception {
        TestDto dto = new TestDto();
        dto.setName(NullableValue.of("John"));
        dto.setEmail(NullableValue.of("john@example.com"));
        dto.setAge(NullableValue.of(30));

        String json = mapper.writeValueAsString(dto);

        assertThat(json).contains("\"name\":\"John\"");
        assertThat(json).contains("\"email\":\"john@example.com\"");
        assertThat(json).contains("\"age\":30");
    }

    @Test
    void serializeWithNull() throws Exception {
        TestDto dto = new TestDto();
        dto.setName(NullableValue.of("John"));
        dto.setEmail(NullableValue.of(null));

        String json = mapper.writeValueAsString(dto);

        assertThat(json).contains("\"name\":\"John\"");
        assertThat(json).contains("\"email\":null");
    }
}
