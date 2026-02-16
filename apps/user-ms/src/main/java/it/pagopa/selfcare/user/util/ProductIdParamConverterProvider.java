package it.pagopa.selfcare.user.util;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Priority(Priorities.USER)
public class ProductIdParamConverterProvider implements ParamConverterProvider {

    @Inject
    ProductIdNormalizer normalizer;

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType,
                                              Type genericType,
                                              Annotation[] annotations) {

        if (rawType.equals(String.class) && hasProductIdAnnotation(annotations)) {
            return (ParamConverter<T>) new ProductIdParamConverter();
        }

        return null;
    }

    private boolean hasProductIdAnnotation(Annotation[] annotations) {
        for (Annotation a : annotations) {
            if (a.annotationType().equals(ProductIdParam.class)) {
                return true;
            }
        }
        return false;
    }

    class ProductIdParamConverter implements ParamConverter<String> {

        @Override
        public String fromString(String value) {
            return normalizer.normalizeAndValidateIfPresent(value);
        }

        @Override
        public String toString(String value) {
            return value;
        }
    }
}
