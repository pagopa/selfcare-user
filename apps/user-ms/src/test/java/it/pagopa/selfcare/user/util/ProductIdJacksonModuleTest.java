package it.pagopa.selfcare.user.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.selfcare.user.util.product.ProductIdDeserializer;
import it.pagopa.selfcare.user.util.product.ProductIdJacksonModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class ProductIdJacksonModuleTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        ProductIdDeserializer deserializer = new ProductIdDeserializer();
        ProductIdJacksonModule module = new ProductIdJacksonModule(deserializer);

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
    }

    @Test
    void testDeserializerRegistration() throws JsonProcessingException {
        String json = "\"test-product-id\"";
        String result = objectMapper.readValue(json, String.class);

        assertEquals("test-product-id", result);
    }

}