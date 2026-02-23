package it.pagopa.selfcare.user.util.product;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Payload;

import java.lang.annotation.*;


@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonDeserialize(using = ProductIdDeserializer.class)
public @interface ProductIdParam {

    String message() default "Invalid productId";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}