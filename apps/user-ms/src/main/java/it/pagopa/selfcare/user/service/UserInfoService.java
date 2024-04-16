package it.pagopa.selfcare.user.service;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import it.pagopa.selfcare.user.controller.response.UserInfoResponse;

import java.util.List;

public interface UserInfoService {
    Uni<UserInfoResponse> findById(String userId);
    Multi<UserInfoResponse> findById(List<String> userIds);
    Uni<Void> updateUsersEmails(List<String> userIds, int page, int size);
    Uni<Void> updateUsersFromInstitutions(int page, int size, String userId);
}
