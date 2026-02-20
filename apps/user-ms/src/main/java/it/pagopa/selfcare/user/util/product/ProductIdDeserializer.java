package it.pagopa.selfcare.user.util.product;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import it.pagopa.selfcare.user.exception.InvalidRequestException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;

import java.io.IOException;

@ApplicationScoped
public class ProductIdDeserializer extends JsonDeserializer<String>
        implements ContextualDeserializer {

    private final boolean enabled;

    public ProductIdDeserializer() {
        this.enabled = false;
    }

    private ProductIdDeserializer(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {

        String value = p.getValueAsString();

        if (!enabled || value == null) {
            return value;
        }

        ProductIdNormalizer normalizer = CDI.current()
                .select(ProductIdNormalizer.class)
                .get();

        try {
            return normalizer.normalize(value);
        } catch (InvalidRequestException ex) {
            throw JsonMappingException.from(p, ex.getMessage(), ex);
        }
    }

    @Override
    public JsonDeserializer<?> createContextual(
            DeserializationContext ctxt,
            BeanProperty property) {

        if (property != null &&
                property.getMember() != null &&
                property.getMember().hasAnnotation(ProductIdParam.class)) {

            return new ProductIdDeserializer(true);
        }

        return this;
    }
}
