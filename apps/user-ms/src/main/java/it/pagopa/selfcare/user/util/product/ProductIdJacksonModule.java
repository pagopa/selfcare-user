package it.pagopa.selfcare.user.util.product;


import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProductIdJacksonModule extends SimpleModule {

    public ProductIdJacksonModule(ProductIdDeserializer deserializer) {
        addDeserializer(String.class, deserializer);
    }
}
