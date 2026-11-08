package com.carlosalbertoxw.jwt.api;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Prueba de extremo a extremo de la API sobre el contrato comun de los ejemplos. */
@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void publicCatalogHidesThePriceAndTheFullOneNeedsAToken() throws Exception {
        mvc.perform(get("/api/products/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Teclado mecánico"))
                .andExpect(jsonPath("$[*]", everyItem(not(hasKey("price")))));

        mvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());

        mvc.perform(get("/api/products").header("Authorization", bearer("user", "User123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(89.99));
    }

    @Test
    void loginReturnsTheCommonTokenFormat() throws Exception {
        mvc.perform(json(post("/api/auth/login"), "{\"username\":\"admin\",\"password\":\"Admin123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isString())
                .andExpect(jsonPath("$.refresh_token").isString())
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.expires_in").value(900));
    }

    @Test
    void meReturnsTheClaimsOfTheAccessToken() throws Exception {
        mvc.perform(get("/api/auth/me").header("Authorization", bearer("admin", "Admin123!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("Admin"))
                .andExpect(jsonPath("$.token_expires_at").isNumber());
    }

    @Test
    void onlyAdminCanCreateProducts() throws Exception {
        String product = "{\"name\":\"Webcam HD\",\"description\":\"1080p\",\"price\":75.5}";

        mvc.perform(json(post("/api/products"), product).header("Authorization", bearer("user", "User123!")))
                .andExpect(status().isForbidden());

        mvc.perform(json(post("/api/products"), product).header("Authorization", bearer("admin", "Admin123!")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Webcam HD"))
                .andExpect(jsonPath("$.price").value(75.5));

        mvc.perform(json(post("/api/products"), "{\"name\":\"\",\"price\":-1}")
                        .header("Authorization", bearer("admin", "Admin123!")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void logoutWorksWithoutAccessTokenAndRevokesTheRefreshToken() throws Exception {
        String refreshToken = loginField("user", "User123!", "$.refresh_token");
        String body = "{\"refresh_token\":\"" + refreshToken + "\"}";

        mvc.perform(json(post("/api/auth/logout"), body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        mvc.perform(json(post("/api/auth/refresh"), body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void repeatedFailedLoginsAreRateLimited() throws Exception {
        String bad = "{\"username\":\"limitado\",\"password\":\"mala\"}";
        for (int i = 0; i < 5; i++) {
            mvc.perform(json(post("/api/auth/login"), bad).with(fromIp("10.9.9." + i)))
                    .andExpect(status().isUnauthorized());
        }

        // La cuenta queda bloqueada aunque el siguiente intento venga de otra IP.
        mvc.perform(json(post("/api/auth/login"), bad).with(fromIp("10.9.9.99")))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    void healthIsPublic() throws Exception {
        mvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    private String bearer(String username, String password) throws Exception {
        return "Bearer " + loginField(username, password, "$.access_token");
    }

    private String loginField(String username, String password, String path) throws Exception {
        String response = mvc.perform(json(post("/api/auth/login"),
                        "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, path);
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder builder, String body) {
        return builder.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor fromIp(String ip) {
        return request -> {
            request.setRemoteAddr(ip);
            return request;
        };
    }
}
