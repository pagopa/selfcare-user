package it.pagopa.selfcare.user.util;

import io.quarkus.scheduler.Scheduled;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.service.ProductService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductCache {

    private final ProductService productService;

    private final AtomicReference<Map<String, Product>> productsByIdRef = new AtomicReference<>();
    private final AtomicReference<Map<String, String>> childToParentRef = new AtomicReference<>();

    @Inject
    public ProductCache(ProductService productService) {
        this.productService = productService;
        refreshCache();
    }

    @Scheduled(every="10m") // refresh time
    void refreshCacheScheduled() {
        refreshCache();
    }

    private void refreshCache() {
        List<Product> products = productService.getProducts(false, false);

        Map<String, Product> productsById = products.stream()
                .collect(Collectors.toUnmodifiableMap(Product::getId, p -> p));

        Map<String, String> childToParent = products.stream()
                .filter(p -> p.getParentId() != null && !p.getParentId().isBlank())
                .collect(Collectors.toUnmodifiableMap(Product::getId, Product::getParentId));

        productsByIdRef.set(productsById);
        childToParentRef.set(childToParent);
    }

    public boolean isAllowed(String productId) {
        return productsByIdRef.get().containsKey(productId);
    }

    public String mapToParent(String productId) {
        return childToParentRef.get().getOrDefault(productId, productId);
    }

}
