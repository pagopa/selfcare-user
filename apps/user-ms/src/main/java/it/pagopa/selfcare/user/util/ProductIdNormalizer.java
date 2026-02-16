package it.pagopa.selfcare.user.util;

import it.pagopa.selfcare.user.exception.InvalidRequestException;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProductIdNormalizer {

    private final ProductCache productCache;

    public ProductIdNormalizer(ProductCache productCache) {
        this.productCache = productCache;
    }

    public String normalizeAndValidateIfPresent(String productId) {

        if (productId == null || productId.isBlank()) {
            return productId;
        }

        String normalized = productCache.mapToParent(productId);

        if (!productCache.isAllowed(normalized)) {
            throw new InvalidRequestException(
                    "ProductId '%s' is not allowed".formatted(productId)
            );
        }

        return normalized;
    }
}
