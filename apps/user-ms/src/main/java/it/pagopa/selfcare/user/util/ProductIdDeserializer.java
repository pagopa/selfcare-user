package it.pagopa.selfcare.user.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;

@ApplicationScoped
public class ProductIdDeserializer extends JsonDeserializer<String> {

    @Inject
    ProductIdNormalizer normalizer;

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {

        String value = p.getValueAsString();
        return normalizer.normalizeAndValidateIfPresent(value);
    }
}
