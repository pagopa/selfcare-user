package it.pagopa.selfcare.user.integration_test;

import io.cucumber.java.Before;
import io.quarkiverse.cucumber.CucumberOptions;
import io.quarkiverse.cucumber.CucumberQuarkusTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.mongodb.MongoTestResource;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Slf4j
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"it.pagopa.selfcare.cucumber.utils", "it.pagopa.selfcare.user.integration_test"},
        plugin = {
                "html:target/cucumber-report/cucumber.html",
                "json:target/cucumber-report/cucumber.json"
        })
public class CucumberSuite extends CucumberQuarkusTest {

    public static void main(String[] args) {
        runMain(CucumberSuite.class, args);
    }

    @BeforeAll
    static void setup() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream("key/public-key.pub")) {
            if (inputStream == null) {
                throw new IOException("Public key file not found in classpath");
            }
            String publicKey = new Scanner(inputStream, StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();
            System.setProperty("JWT-PUBLIC-KEY", publicKey);
        }

        final ComposeContainer composeContainer = new ComposeContainer(new File("src/test/resources/docker-compose.yml"))
                .withLocalCompose(true).withEnv("GITHUB_TOKEN", System.getenv("GITHUB_TOKEN"))
                .waitingFor("azure-cli", Wait.forLogMessage(".*BLOBSTORAGE INITIALIZED.*\\n", 1));
        composeContainer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(composeContainer::stop));
    }

    @Before
    public void setupRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8081;
    }

    @AfterAll
    static void tearDown() {
        log.info("Cucumber tests are finished.");
        System.exit(0);
    }

}
