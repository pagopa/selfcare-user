package it.pagopa.selfcare.user.service;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.user.common.PartyRole;
import it.pagopa.selfcare.user.constant.OnboardedProductState;
import it.pagopa.selfcare.user.entity.OnboardedProduct;
import it.pagopa.selfcare.user.entity.UserInstitution;
import it.pagopa.selfcare.user.util.QueryUtils;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.bson.Document;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ApplicationScoped
@RequiredArgsConstructor
public class DaoService {

    private static final String CURRENT_PRODUCT_REF = "$[current]";

    private final QueryUtils queryUtils;

    public Uni<Long> updateUserStatusDao(String userId, String institutionId, String productId, PartyRole role, String productRole, OnboardedProductState status) {
        String roleString = Objects.isNull(role) ? null : role.name();

        Map<String, Object> fieldToUpdateMap = new HashMap<>();
        fieldToUpdateMap.put(UserInstitution.Fields.products.name() + CURRENT_PRODUCT_REF + OnboardedProduct.Fields.status.name(), status);
        fieldToUpdateMap.put(UserInstitution.Fields.products.name() + CURRENT_PRODUCT_REF + OnboardedProduct.Fields.updatedAt.name(), OffsetDateTime.now());

        Map<String, Object> parametersMap = queryUtils.createMapForUserUpdateParameter(userId, institutionId, productId, null, roleString , productRole, null);

        return UserInstitution.update(queryUtils.buildUpdateDocument(fieldToUpdateMap))
                .where(queryUtils.buildQueryDocument(parametersMap));
    }

    public Uni<Long> updateUserStatusDaoByRelationshipId(String relationshipId, OnboardedProductState status) {

        Map<String, Object> fieldToUpdateMap = new HashMap<>();
        fieldToUpdateMap.put(UserInstitution.Fields.products.name() + CURRENT_PRODUCT_REF + OnboardedProduct.Fields.status.name(), status);
        fieldToUpdateMap.put(UserInstitution.Fields.products.name() + CURRENT_PRODUCT_REF + OnboardedProduct.Fields.updatedAt.name(), OffsetDateTime.now());

        Map<String, Object> parametersMap = queryUtils.createMapForUserUpdateParameter(null, null, null, null, null , null, relationshipId);

        return UserInstitution.update(queryUtils.buildUpdateDocument(fieldToUpdateMap))
                .where(queryUtils.buildQueryDocument(parametersMap));
    }

    public Uni<List<UserInstitution>> paginatedFindAllWithFilter(Map<String, Object> queryParameter, Integer page, Integer size) {
        Document query = queryUtils.buildQueryDocument(queryParameter);
        return runUserInstitutionFindQuery(query, null).page(page, size).list();
    }

    public Uni<UserInstitution> retrieveFirstFilteredUserInstitution(Map<String, Object> queryParameter) {
        Document query = queryUtils.buildQueryDocument(queryParameter);
        return runUserInstitutionFindQuery(query, null).firstResult();
    }

    public ReactivePanacheQuery<UserInstitution> runUserInstitutionFindQuery(Document query, Document sort) {
        return UserInstitution.find(query, sort);
    }

}
