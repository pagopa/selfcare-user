package it.pagopa.selfcare.user.util;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;


@Documented
@Constraint(validatedBy = ProductIdValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@JsonDeserialize(using = ProductIdDeserializer.class)
public @interface ProductIdParam {

    String message() default "Invalid productId";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}