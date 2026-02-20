package it.pagopa.selfcare.user.util.product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@ApplicationScoped
public class ProductIdValidator
        implements ConstraintValidator<ProductIdParam, String> {

    @Inject
    ProductIdNormalizer normalizer;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.isBlank()) {
            return true;
        }
        return normalizer.isAllowed(value);
    }
}
