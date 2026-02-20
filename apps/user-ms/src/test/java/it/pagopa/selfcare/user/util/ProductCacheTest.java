package it.pagopa.selfcare.user.util;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.service.ProductService;
import it.pagopa.selfcare.user.util.product.ProductCache;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static io.smallrye.common.constraint.Assert.assertFalse;
import static io.smallrye.common.constraint.Assert.assertTrue;

@QuarkusTest
class ProductCacheTest {

    @InjectMock
    ProductService productService;

    @Inject
    ProductCache productCache;

    @Test
    void testIsAllowedAndMapToParent() {
        Product parent = new Product();
        parent.setId("p1");
        parent.setParentId(null);

        Product child = new Product();
        child.setId("c1");
        child.setParentId("p1");

        Mockito.when(productService.getProducts(false, false))
                .thenReturn(List.of(parent, child));

        assertTrue(productCache.isAllowed("p1"));
        assertTrue(productCache.isAllowed("c1"));
        assertFalse(productCache.isAllowed("unknown"));

        Assertions.assertEquals("p1", productCache.mapToParent("c1"));
        Assertions.assertEquals("p1", productCache.mapToParent("p1"));
        Assertions.assertEquals("unknown", productCache.mapToParent("unknown"));
    }
}