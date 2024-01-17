package it.pagopa.selfcare.product.service;

import it.pagopa.selfcare.product.entity.Product;
import it.pagopa.selfcare.product.entity.ProductRoleInfo;
import it.pagopa.selfcare.user.common.InstitutionType;
import it.pagopa.selfcare.user.common.PartyRole;

import java.util.List;
import java.util.Map;

public interface ProductService {
    public List<Product> getProducts(boolean rootOnly, boolean valid) ;

    void validateRoleMappings(Map<PartyRole, ? extends ProductRoleInfo> roleMappings);

    Product getProduct(String productId);

    void fillContractTemplatePathAndVersion(Product product, InstitutionType institutionType);

    Product getProductIsValid(String productId);
}
