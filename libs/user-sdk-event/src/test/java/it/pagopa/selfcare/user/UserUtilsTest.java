package it.pagopa.selfcare.user;

import it.pagopa.selfcare.onboarding.common.PartyRole;
import it.pagopa.selfcare.user.model.OnboardedProduct;
import it.pagopa.selfcare.user.model.TrackEventInput;
import it.pagopa.selfcare.user.model.constants.OnboardedProductState;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

public class UserUtilsTest {

    @Test
    void groupingProductAndReturnMinStateProduct_whenActiveAndDelete() {

        List<OnboardedProduct> products = new ArrayList<>();
        products.add(dummyOnboardedProduct("example", OnboardedProductState.ACTIVE));
        products.add(dummyOnboardedProduct("example", OnboardedProductState.DELETED));

        Collection<OnboardedProduct> actuals = UserUtils.groupingProductAndReturnMinStateProduct(products);
        assertEquals(1, actuals.size());
        OnboardedProduct actual = actuals.stream().findFirst().orElse(null);
        assertNotNull(actual);
        assertNotNull(actual.getStatus());
        assertEquals(OnboardedProductState.ACTIVE, actual.getStatus());
    }

    @Test
    void groupingProductAndRoleAndReturnMinStateProduct_whenActiveAndDelete() {

        List<OnboardedProduct> products = new ArrayList<>();
        products.add(dummyOnboardedProductWithRole("example", OnboardedProductState.ACTIVE, PartyRole.MANAGER));
        products.add(dummyOnboardedProductWithRole("example", OnboardedProductState.DELETED, PartyRole.MANAGER));

        Collection<OnboardedProduct> actuals = UserUtils.groupingProductWithRoleAndReturnMinStateProduct(products);
        assertEquals(1, actuals.size());
        OnboardedProduct actual = actuals.stream().findFirst().orElse(null);
        assertNotNull(actual);
        assertNotNull(actual.getStatus());
        assertEquals(OnboardedProductState.ACTIVE, actual.getStatus());
    }

    @Test
    void groupingProductAndReturnMinStateProduct_whenMoreRole() {

        List<OnboardedProduct> products = new ArrayList<>();
        products.add(dummyOnboardedProductWithRole("example", OnboardedProductState.ACTIVE, PartyRole.MANAGER));
        products.add(dummyOnboardedProductWithRole("example", OnboardedProductState.DELETED, PartyRole.DELEGATE));
        products.add(dummyOnboardedProductWithRole("example", OnboardedProductState.DELETED, PartyRole.SUB_DELEGATE));

        Collection<OnboardedProduct> actuals = UserUtils.groupingProductWithRoleAndReturnMinStateProduct(products);

        assertNotNull(actuals);
        assertEquals(3, actuals.size());

        OnboardedProduct[] ps = products.toArray(new OnboardedProduct[0]);
        BiFunction<OnboardedProduct, OnboardedProduct, Boolean> getPredicate = (OnboardedProduct a, OnboardedProduct p) -> a.getStatus().equals(p.getStatus()) && a.getRole().equals(p.getRole());
        assertTrue(actuals.stream().anyMatch(act -> getPredicate.apply(act, ps[0])));
        assertTrue(actuals.stream().anyMatch(act -> getPredicate.apply(act, ps[1])));
        assertTrue(actuals.stream().anyMatch(act -> getPredicate.apply(act, ps[2])));
    }

    @Test
    void groupingProductAndReturnMinStateProduct_whenSameRoleAndMoreProductRole() {

        List<OnboardedProduct> products = new ArrayList<>();
        products.add(dummyOnboardedProductWithRole("example1", OnboardedProductState.ACTIVE, PartyRole.OPERATOR));
        products.add(dummyOnboardedProductWithRole("example2", OnboardedProductState.ACTIVE, PartyRole.OPERATOR));

        Collection<OnboardedProduct> actuals = UserUtils.groupingProductWithRoleAndReturnMinStateProduct(products);

        assertNotNull(actuals);
        assertEquals(2, actuals.size());

        OnboardedProduct[] ps = products.toArray(new OnboardedProduct[0]);
        BiFunction<OnboardedProduct, OnboardedProduct, Boolean> getPredicate = (OnboardedProduct a, OnboardedProduct p) -> a.getStatus().equals(p.getStatus()) && a.getRole().equals(p.getRole());
        assertTrue(actuals.stream().anyMatch(act -> getPredicate.apply(act, ps[0])));
        assertTrue(actuals.stream().anyMatch(act -> getPredicate.apply(act, ps[1])));
    }

    @Test
    void groupingProductAndReturnMinStateProduct_whenSameRoleAndPendingStates() {

        List<OnboardedProduct> products = new ArrayList<>();
        products.add(dummyOnboardedProductWithRole("example", OnboardedProductState.PENDING, PartyRole.OPERATOR));
        products.add(dummyOnboardedProductWithRole("example", OnboardedProductState.SUSPENDED, PartyRole.OPERATOR));

        Collection<OnboardedProduct> actuals = UserUtils.groupingProductWithRoleAndReturnMinStateProduct(products);

        assertNotNull(actuals);
        assertEquals(1, actuals.size());

        BiFunction<OnboardedProduct, OnboardedProduct, Boolean> getPredicate = (OnboardedProduct a, OnboardedProduct p) -> a.getStatus().equals(p.getStatus()) && a.getRole().equals(p.getRole());
        assertTrue(actuals.stream().anyMatch(act -> getPredicate.apply(act, products.get(1))));
    }

    @Test
    void groupingProductAndReturnMinStateProduct_whenMoreDelete() {

        List<OnboardedProduct> products = new ArrayList<>();
        products.add(dummyOnboardedProduct("example", OnboardedProductState.DELETED));
        products.add(dummyOnboardedProduct("example", OnboardedProductState.DELETED));
        OnboardedProduct expected = dummyOnboardedProduct("example", OnboardedProductState.DELETED);
        products.add(expected);

        Collection<OnboardedProduct> actuals = UserUtils.groupingProductAndReturnMinStateProduct(products);

        assertEquals(1, actuals.size());
        OnboardedProduct actual = actuals.stream().findFirst().orElse(null);
        assertNotNull(actual);
        assertNotNull(actual.getStatus());
        assertEquals(expected, actual);
    }

    @Test
    void groupingProductAndReturnMinStateProduct_whenMoreProduct() {

        List<OnboardedProduct> products = new ArrayList<>();
        products.add(dummyOnboardedProduct("example", OnboardedProductState.DELETED));
        products.add(dummyOnboardedProduct("example-2", OnboardedProductState.DELETED));

        Collection<OnboardedProduct> actuals = UserUtils.groupingProductAndReturnMinStateProduct(products);
        assertEquals(2, actuals.size());
    }

    @Test
    void mapPropsForTrackEvent() {

        TrackEventInput trackEventInput = TrackEventInput.builder().documentKey("documentKey").userId("userId").productId("productId").institutionId("institutionId").build();

        Map<String, String> maps = UserUtils.mapPropsForTrackEvent(trackEventInput);
        assertTrue(maps.containsKey("documentKey"));
        assertTrue(maps.containsKey("userId"));
        assertTrue(maps.containsKey("institutionId"));
        assertTrue(maps.containsKey("productId"));
    }

    OnboardedProduct dummyOnboardedProductWithRole(String productRole, OnboardedProductState state, PartyRole role) {
        OnboardedProduct onboardedProduct = dummyOnboardedProduct(productRole, state);
        onboardedProduct.setRole(role);
        return onboardedProduct;
    }

    OnboardedProduct dummyOnboardedProduct(String productRole, OnboardedProductState state) {
        OnboardedProduct onboardedProduct = new OnboardedProduct();
        onboardedProduct.setProductId("productId");
        onboardedProduct.setProductRole(productRole);
        onboardedProduct.setCreatedAt(OffsetDateTime.of(LocalDate.EPOCH, LocalTime.MIN, ZoneOffset.UTC));
        onboardedProduct.setUpdatedAt(OffsetDateTime.of(LocalDate.EPOCH, LocalTime.MIN, ZoneOffset.UTC));
        onboardedProduct.setStatus(state);
        return onboardedProduct;
    }

    @Test
    void getSASToken_withValidInputs_shouldReturnValidToken() {
        String resourceUri = "https://example.com/resource";
        String keyName = "keyName";
        String key = "secretKey";

        String sasToken = UserUtils.getSASToken(resourceUri, keyName, key);

        assertNotNull(sasToken);
        assertTrue(sasToken.contains("SharedAccessSignature sr="));
        assertTrue(sasToken.contains("&sig="));
        assertTrue(sasToken.contains("&se="));
        assertTrue(sasToken.contains("&skn="));
    }

    @Test
    void getSASToken_withInvalidEncoding_shouldHandleException() {
        String resourceUri = "https://example.com/resource";
        String keyName = "keyName";
        String key = "secretKey";

        String sasToken = UserUtils.getSASToken(resourceUri, keyName, key);

        assertNotNull(sasToken);
    }

    @Test
    void getHMAC256_withValidInputs_shouldReturnValidHash() {
        String key = "secretKey";
        String input = "inputString";

        String hash = UserUtils.getHMAC256(key, input);

        assertNotNull(hash);
    }

    @Test
    void retrieveFdProduct_withValidProducts_shouldReturnMostRecentlyUpdatedProduct() {
        List<OnboardedProduct> products = new ArrayList<>();
        OnboardedProduct product1 = new OnboardedProduct();
        product1.setProductId("1");
        product1.setUpdatedAt(OffsetDateTime.now().minusDays(1));
        product1.setCreatedAt(OffsetDateTime.now().minusDays(2));
        products.add(product1);

        OnboardedProduct product2 = new OnboardedProduct();
        product2.setProductId("2");
        product2.setUpdatedAt(OffsetDateTime.now());
        product2.setCreatedAt(OffsetDateTime.now().minusDays(1));
        products.add(product2);

        List<String> productIdToCheck = List.of("1", "2");

        List<OnboardedProduct> result = UserUtils.retrieveFdProduct(products, productIdToCheck, false);

        assertNotNull(result);
        assertEquals("2", result.get(0).getProductId());
    }

    @Test
    void retrieveFdProduct_withEmptyProductList() {
        List<OnboardedProduct> products = Collections.emptyList();
        List<String> productIdToCheck = List.of("1", "2");

        List<OnboardedProduct> result = UserUtils.retrieveFdProduct(products, productIdToCheck, false);

        assertEquals(0, result.size());
    }

    @Test
    void retrieveFdProduct_mailChanges() {
        List<OnboardedProduct> products = new ArrayList<>();
        OnboardedProduct product = dummyOnboardedProduct("example", OnboardedProductState.ACTIVE);
        product.setProductId("prod-fd");
        products.add(product);
        List<String> productIdToCheck = List.of("prod-fd", "2");

        List<OnboardedProduct> result = UserUtils.retrieveFdProduct(products, productIdToCheck, true);

        assertEquals(1, result.size());
    }

    @Test
    void retrieveFdProduct_withNoFdProductIds() {
        List<OnboardedProduct> products = new ArrayList<>();
        OnboardedProduct product1 = new OnboardedProduct();
        product1.setProductId("1");
        product1.setUpdatedAt(OffsetDateTime.now().minusDays(1));
        product1.setCreatedAt(OffsetDateTime.now().minusDays(2));
        products.add(product1);

        OnboardedProduct product2 = new OnboardedProduct();
        product2.setProductId("2");
        product2.setUpdatedAt(OffsetDateTime.now());
        product2.setCreatedAt(OffsetDateTime.now().minusDays(1));
        products.add(product2);

        List<String> productIdToCheck = List.of("3", "4");

        List<OnboardedProduct> result = UserUtils.retrieveFdProduct(products, productIdToCheck, false);

        assertEquals(0, result.size());
    }
}
