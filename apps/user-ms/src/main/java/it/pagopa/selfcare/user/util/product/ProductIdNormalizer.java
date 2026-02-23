package it.pagopa.selfcare.user.util.product;

import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.exception.ProductNotFoundException;
import it.pagopa.selfcare.product.service.ProductService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;

@ApplicationScoped
public class ProductIdNormalizer {

    private final ProductService productService;

    @Inject
    public ProductIdNormalizer(ProductService productService) {
        this.productService = productService;
    }

    public String normalize(String productId) {

        if (productId == null || productId.isBlank()) {
            return productId;
        }

        try {
            Product product = productService.getProduct(productId);

            return Optional.ofNullable(product)
                    .map(Product::getParentId)
                    .filter(parent -> parent != null && !parent.isBlank())
                    .orElse(productId);
        } catch (ProductNotFoundException ex) {
            return productId;
        }
    }
}