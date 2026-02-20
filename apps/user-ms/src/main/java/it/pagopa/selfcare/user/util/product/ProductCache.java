package it.pagopa.selfcare.user.util.product;

import io.quarkus.scheduler.Scheduled;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.service.ProductService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductCache {

    private final ProductService productService;

    private final AtomicReference<Map<String, Product>> productsByIdRef = new AtomicReference<>();
    private final AtomicReference<Map<String, String>> childToParentRef = new AtomicReference<>();

    @Getter
    private volatile boolean ready = false;

    @Inject
    public ProductCache(ProductService productService) {
        this.productService = productService;
    }

    @PostConstruct
    void init() {
        refreshCache();
    }

    @Scheduled(every="10m") // refresh time
    void refreshCacheScheduled() {
        refreshCache();
    }

    private void refreshCache() {
        try {
            List<Product> products = productService.getProducts(false, false);

            Map<String, Product> productsById = products.stream()
                    .collect(Collectors.toUnmodifiableMap(Product::getId, p -> p));

            Map<String, String> childToParent = products.stream()
                    .filter(p -> p.getParentId() != null && !p.getParentId().isBlank())
                    .collect(Collectors.toUnmodifiableMap(Product::getId, Product::getParentId));

            productsByIdRef.set(productsById);
            childToParentRef.set(childToParent);

            ready = !productsById.isEmpty();

        } catch (Exception ex) {
            ready = false;
        }
    }

    public boolean isAllowed(String productId) {
        if (!ready) {
            return true; // skip validation if cache is unavailable
        }
        return productsByIdRef.get().containsKey(productId);
    }

    public String mapToParent(String productId) {
        if (!ready) {
            return productId; // skip mapping if cache is unavailable
        }
        return childToParentRef.get().getOrDefault(productId, productId);
    }

}
