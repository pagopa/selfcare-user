package it.pagopa.selfcare.user.util;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.exception.ProductNotFoundException;
import it.pagopa.selfcare.product.service.ProductService;
import it.pagopa.selfcare.user.util.product.ProductIdNormalizer;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@QuarkusTest
class ProductIdNormalizerTest {

    @InjectMock
    ProductService productService;

    @Inject
    ProductIdNormalizer normalizer;

    @Test
    void shouldReturnNullWhenProductIdIsNull() {
        assertNull(normalizer.normalize(null));
    }

    @Test
    void shouldReturnSameIdWhenProductHasNoParent() {
        String productId = "prod-1";

        Product product = new Product();
        product.setId(productId);
        product.setParentId(null);

        when(productService.getProduct(productId)).thenReturn(product);

        assertEquals(productId, normalizer.normalize(productId));
    }

    @Test
    void shouldReturnParentIdWhenProductHasParent() {
        String productId = "child";
        String parentId = "parent";

        Product product = new Product();
        product.setId(productId);
        product.setParentId(parentId);

        when(productService.getProduct(productId)).thenReturn(product);

        assertEquals(parentId, normalizer.normalize(productId));
    }


    @Test
    void shouldReturnSameIdWhenProductNotFound() {
        String productId = "unknown";

        when(productService.getProduct(productId))
                .thenThrow(new ProductNotFoundException("Not found"));

        assertEquals(productId, normalizer.normalize(productId));
    }
}