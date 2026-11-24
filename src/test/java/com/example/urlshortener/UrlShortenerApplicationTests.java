package com.example.urlshortener;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class UrlShortenerApplicationTests {

    private static final String TARGET = "https://example.com/some/long/path?x=1";

    @Autowired
    MockMvc mvc;

    private MvcResult create(String json) throws Exception {
        return mvc.perform(post("/api/urls").contentType(MediaType.APPLICATION_JSON).content(json)).andReturn();
    }

    @Test
    void createRedirectAndStats() throws Exception {
        MvcResult created = create("{\"url\":\"" + TARGET + "\"}");
        assertEquals(201, created.getResponse().getStatus());
        String code = JsonPath.read(created.getResponse().getContentAsString(), "$.code");

        mvc.perform(get("/" + code))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", TARGET));

        mvc.perform(get("/api/urls/" + code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clickCount").value(1))
                .andExpect(jsonPath("$.originalUrl").value(TARGET));
    }

    @Test
    void customAliasMustBeUnique() throws Exception {
        String json = "{\"url\":\"https://example.org\",\"alias\":\"my-link\"}";
        assertEquals(201, create(json).getResponse().getStatus());
        assertEquals(409, create(json).getResponse().getStatus());
    }

    @Test
    void rejectsInvalidUrls() throws Exception {
        assertEquals(400, create("{\"url\":\"ftp://example.com/file\"}").getResponse().getStatus());
        assertEquals(400, create("{\"url\":\"not a url\"}").getResponse().getStatus());
        assertEquals(400, create("{\"url\":\"\"}").getResponse().getStatus());
    }

    @Test
    void unknownCodeReturns404() throws Exception {
        mvc.perform(get("/doesNotExist")).andExpect(status().isNotFound());
    }

    @Test
    void deleteRemovesUrl() throws Exception {
        assertEquals(201, create("{\"url\":\"https://example.net\",\"alias\":\"to-delete\"}").getResponse().getStatus());
        mvc.perform(delete("/api/urls/to-delete")).andExpect(status().isNoContent());
        mvc.perform(get("/to-delete")).andExpect(status().isNotFound());
    }
}
