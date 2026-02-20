package it.pagopa.selfcare.user.util.product;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProductIdNormalizer {

    private final ProductCache productCache;

    public ProductIdNormalizer(ProductCache productCache) {
        this.productCache = productCache;
    }

    public String normalize(String productId) {

        if (productId == null || productId.isBlank()) {
            return productId;
        }

        return productCache.mapToParent(productId);
    }

    public boolean isAllowed(String productId) {
        return productCache.isAllowed(productId);
    }
}
