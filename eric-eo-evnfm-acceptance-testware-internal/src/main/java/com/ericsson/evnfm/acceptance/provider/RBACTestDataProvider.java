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
package com.ericsson.evnfm.acceptance.provider;

import com.ericsson.evnfm.acceptance.json.Credential;
import com.ericsson.evnfm.acceptance.json.RbacTestParams;
import com.ericsson.evnfm.acceptance.json.Uri;
import com.ericsson.evnfm.acceptance.json.UseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getObjectMapper;

public class RBACTestDataProvider {

    private RBACTestDataProvider(){}

    private static Stream<Arguments> provideRbacRestTestData() throws IOException {
        final InputStream rbacFileStream = RBACTestDataProvider.class
                .getClassLoader().getResourceAsStream("rbac-test-params.json");
        final RbacTestParams rbacTestParams = getObjectMapper().readValue(rbacFileStream, RbacTestParams.class);

        final List<Arguments> testArguments = new ArrayList<>();
        for (final UseCase useCase : rbacTestParams.getUseCases()) {
            for (final Uri uri : useCase.getUris()) {
                String testUri = getUriWithoutPlaceholders(uri);
                for (final String method : uri.getMethods()) {
                    for (final Credential credential : rbacTestParams.getCredentials()) {
                        final String useCaseName = useCase.getName(); // just for triaging
                        final String testUsername = credential.getUsername();
                        final String testPassword = credential.getPassword();
                        final boolean expect403 = !useCase.getAllowedUsers().contains(testUsername);
                        testArguments.add(Arguments.of(
                                useCaseName,
                                testUri,
                                method,
                                testUsername,
                                testPassword,
                                expect403
                        ));
                    }
                }
            }
        }
        return Stream.of(
                testArguments.toArray(new Arguments[0])
        );
    }

    private static String getUriWithoutPlaceholders(Uri uri) {
        String testUri = uri.getUri();
        if (uri.getPlaceholders() != null) {
            for (final String placeholder : uri.getPlaceholders()) {
                testUri = testUri.replace(placeholder, "fakeTestData"); // RBAC tests do not require real parameters in paths
            }
        }
        return testUri;
    }
}
