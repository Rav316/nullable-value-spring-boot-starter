package io.github.rav316.nullablevalue;

import io.github.rav316.nullablevalue.autoconfigure.NullableValueAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = NullableValueIntegrationTest.TestApp.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
@Import({NullableValueAutoConfiguration.class, JacksonAutoConfiguration.class})
class NullableValueIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @SpringBootApplication
    @Import(TestController.class)
    static class TestApp {
    }

    @RestController
    static class TestController {

        @PatchMapping("/test")
        public TestResponse patchTest(@RequestBody TestRequest request) {
            TestResponse response = new TestResponse();
            response.setNamePresent(request.getName().isPresent());
            response.setNameValue(request.getName().isPresent() ? request.getName().get() : null);
            response.setEmailPresent(request.getEmail().isPresent());
            response.setEmailValue(request.getEmail().isPresent() ? request.getEmail().get() : null);
            return response;
        }
    }

    static class TestRequest {
        private NullableValue<String> name = NullableValue.undefined();
        private NullableValue<String> email = NullableValue.undefined();

        public NullableValue<String> getName() { return name; }
        public void setName(NullableValue<String> name) { this.name = name; }
        public NullableValue<String> getEmail() { return email; }
        public void setEmail(NullableValue<String> email) { this.email = email; }
    }

    static class TestResponse {
        private boolean namePresent;
        private String nameValue;
        private boolean emailPresent;
        private String emailValue;

        public boolean isNamePresent() { return namePresent; }
        public void setNamePresent(boolean namePresent) { this.namePresent = namePresent; }
        public String getNameValue() { return nameValue; }
        public void setNameValue(String nameValue) { this.nameValue = nameValue; }
        public boolean isEmailPresent() { return emailPresent; }
        public void setEmailPresent(boolean emailPresent) { this.emailPresent = emailPresent; }
        public String getEmailValue() { return emailValue; }
        public void setEmailValue(String emailValue) { this.emailValue = emailValue; }
    }

    @Test
    void patchWithValue() throws Exception {
        mockMvc.perform(patch("/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "John", "email": "john@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.namePresent").value(true))
                .andExpect(jsonPath("$.nameValue").value("John"))
                .andExpect(jsonPath("$.emailPresent").value(true))
                .andExpect(jsonPath("$.emailValue").value("john@example.com"));
    }

    @Test
    void patchWithNull() throws Exception {
        mockMvc.perform(patch("/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "John", "email": null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.namePresent").value(true))
                .andExpect(jsonPath("$.nameValue").value("John"))
                .andExpect(jsonPath("$.emailPresent").value(true))
                .andExpect(jsonPath("$.emailValue").doesNotExist());
    }

    @Test
    void patchWithUndefined() throws Exception {
        mockMvc.perform(patch("/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "John"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.namePresent").value(true))
                .andExpect(jsonPath("$.nameValue").value("John"))
                .andExpect(jsonPath("$.emailPresent").value(false));
    }
}
