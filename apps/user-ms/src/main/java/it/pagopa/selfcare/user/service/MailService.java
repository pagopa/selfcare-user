package it.pagopa.selfcare.user.service;

import io.smallrye.mutiny.Uni;

import java.util.Map;

public interface MailService {
    Uni<Void> sendMail(String email, String content, String subject);
}
