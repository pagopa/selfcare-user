package it.pagopa.selfcare.cucumber.config;

import io.cucumber.spring.CucumberContextConfiguration;
import it.pagopa.selfcare.cucumber.SelfCareCucumberApplication;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = SelfCareCucumberApplication.class)
public class CucumberTestConfig {

}
