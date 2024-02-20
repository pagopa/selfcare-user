package it.pagopa.selfcare.user.service;

import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
@IfBuildProperty(name = "user-ms.mail.connector.type", stringValue = "default", enableIfMissing = true)
public class MailServiceImpl implements MailService {

    private final Mailer mailer;


    @ConfigProperty(name = "user-ms.mail.user-subject-prefix")
    String userMailSubjectPrefix;
    @ConfigProperty(name = "user-ms.mail.no-reply")
    String senderMail;

    public MailServiceImpl(Mailer mailer) {
        this.mailer = mailer;
    }

    @Override
    public Uni<Void> sendMail(String email, String content, String subject) {
        Mail mail = Mail
                .withHtml(email, userMailSubjectPrefix + subject, content)
                .setFrom(senderMail);

        return  Uni.createFrom().item(() -> {
                    mailer.send(mail);
                    return Uni.createFrom().voidItem();
                })
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .onFailure().invoke(throwable -> log.error("Error during send mail to: {} -> exception: {}", email, throwable.getMessage(), throwable))
                .onItem().invoke(() -> log.trace("Message sent successfully to: {}", email))
                .onFailure().recoverWithNull()
                .replaceWithVoid();
    }

}

