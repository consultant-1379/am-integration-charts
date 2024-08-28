/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.evnfm.acceptance.tests;

import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getObjectMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.ericsson.evnfm.acceptance.common.ConfigurationProvider;
import com.ericsson.evnfm.acceptance.json.Credential;
import com.ericsson.evnfm.acceptance.json.RbacTestParams;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigGeneral;
import com.ericsson.evnfm.acceptance.provider.RBACTestDataProvider;
import com.ericsson.evnfm.acceptance.steps.rest.CommonSteps;
import com.ericsson.evnfm.acceptance.utils.KeycloakHelper;
import reactor.core.publisher.Mono;

public class EndToEndAcceptanceTest {

    private static final Map<String, String> tokens = new HashMap<>();
    private static final List<User> users = new ArrayList<>();

    @BeforeAll
    public static void setup() throws IOException {
        if (!ConfigurationProvider.isPreconfigured()) {
            ConfigurationProvider.setConfiguration("config.json");
        }
        final InputStream rbacFileStream = RBACTestDataProvider.class
                .getClassLoader().getResourceAsStream("rbac-test-params.json");
        final RbacTestParams rbacTestParams = getObjectMapper().readValue(rbacFileStream, RbacTestParams.class);
        final ConfigGeneral generalConfig = ConfigurationProvider.getGeneralConfig();
        for (Credential credential : rbacTestParams.getCredentials()) {
            String userId = KeycloakHelper.createUser(generalConfig, credential.getUsername(), credential.getPassword(), credential.getRole());
            User user = new User(userId, credential.getUsername(), credential.getPassword());
            users.add(user);
            tokens.put(credential.getUsername(), CommonSteps.retrieveToken(credential.getUsername(), credential.getPassword()));
        }

    }

    @ParameterizedTest
    @Execution(ExecutionMode.CONCURRENT)
    @MethodSource("com.ericsson.evnfm.acceptance.provider.RBACTestDataProvider#provideRbacRestTestData")
    public void rbacRESTTest(String useCaseName, String uri, String method, String username, String password, boolean expect403Forbidden) {
        final String fullUri = ConfigurationProvider.getGeneralConfig().getApiGatewayHost() + uri;
        final String jsessionid = tokens.get(username);

        final ClientResponse response = WebClient.create()
                .method(HttpMethod.valueOf(method))
                .uri(fullUri)
                .header("cookie", "JSESSIONID=" + jsessionid)
                .exchangeToMono(Mono::just).block(Duration.ofSeconds(10));

        final String testDataForError = "useCaseName = " + useCaseName +
                ", uri = " + uri +
                ", method = " + method +
                ", username = " + username +
                ", password = " + password +
                ", expect403Forbidden = " + expect403Forbidden;
        if (response == null) Assertions.fail("Failed to receive a valid response");
        if (expect403Forbidden) assertEquals(HttpStatus.FORBIDDEN, response.statusCode(), testDataForError);
        else assertNotEquals(HttpStatus.FORBIDDEN, response.statusCode(), testDataForError);
    }

    @AfterAll
    public static void shutdown() {
        final ConfigGeneral generalConfig = ConfigurationProvider.getGeneralConfig();
        for (User user : users) {
            KeycloakHelper.deleteUser(generalConfig, user);
        }
    }
}
