package it.pagopa.selfcare.user.service;

import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.user.common.PartyRole;
import it.pagopa.selfcare.user.constants.RelationshipState;

import java.util.List;

public interface UserService {

    Uni<Boolean> updateUserStatus(String userId, String institutionId, String productId, PartyRole role, String productRole, RelationshipState status);

    Uni<List<Object>> findAll(Integer size, Integer page, String productId);

    Uni<Object> retrievePerson(String userId, String productId, String institutionId);

    Uni<List<Object>> retrieveBindings(String institutionId, String userId, String[] states, List<String> products);

    Uni<List<Object>> getUserInfo(String userId, String institutionId, String[] states);

}
