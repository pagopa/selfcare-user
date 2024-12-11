package it.pagopa.selfcare.cucumber;

import it.pagopa.selfcare.cucumber.dao.UserGroupRepository;
import it.pagopa.selfcare.cucumber.model.UserGroupEntity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SelfCareCucumberApplication {


    public static void main(String[] args) {
        SpringApplication.run(SelfCareCucumberApplication.class, args);
    }

}
