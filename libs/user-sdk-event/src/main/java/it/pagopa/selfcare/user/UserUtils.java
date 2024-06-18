package it.pagopa.selfcare.user;

import it.pagopa.selfcare.user.model.OnboardedProduct;
import it.pagopa.selfcare.user.model.constants.OnboardedProductState;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;

public class UserUtils {

    public static String uniqueIdNotification(String userInstitutionId, String productId, String productRole) {
        return String.format("%s_%s_%s", userInstitutionId, productId, productRole);
    }

    public static Collection<OnboardedProduct> groupingProductAndReturnMinStateProduct(List<OnboardedProduct> products) {
        Map<String, List<OnboardedProduct>> mapProducts = products.stream()
                .collect(Collectors.groupingBy(product -> String.format("%s_%s", product.getProductId(), product.getProductRole())));

        Map<String, OnboardedProduct> onboardedProductMap = new HashMap<>();
        mapProducts
                .forEach((key, value) -> {
                    OnboardedProductState state = Collections.min(value.stream()
                            .map(OnboardedProduct::getStatus)
                            .toList());
                    value.stream()
                            .filter(product -> state.equals(product.getStatus()))
                            .max(Comparator.comparing(OnboardedProduct::getUpdatedAt, nullsLast(naturalOrder()))
                                    .thenComparing(OnboardedProduct::getCreatedAt, nullsLast(naturalOrder())))
                            .ifPresent(product -> onboardedProductMap.put(key, product));
                });


        return onboardedProductMap.values();
    }
}
