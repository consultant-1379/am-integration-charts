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
package com.ericsson.evnfm.acceptance.steps.common.rest;

import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigGeneral;
import com.ericsson.evnfm.acceptance.utils.KeycloakHelper;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.ericsson.evnfm.acceptance.utils.Constants.EVNFM_SUPER_USER_ROLE;
import static com.ericsson.evnfm.acceptance.utils.Constants.EVNFM_UI_USER_ROLE;

public class Idam {

    private static final Logger LOGGER = LoggerFactory.getLogger(Idam.class);

    public static User createUser(ConfigGeneral configGeneral) {
        String username = generateRandomString();
        String password = generateRandomPassword();

        LOGGER.info("Create new User: {}", username);

        String clientSecret = KeycloakHelper.getClientSecret(configGeneral);
        configGeneral.setIdamClientSecret(clientSecret);

        String userId = KeycloakHelper.createUser(configGeneral, username, password, EVNFM_SUPER_USER_ROLE, EVNFM_UI_USER_ROLE);

        return new User(userId, username, password);
    }

    public static void deleteUser(ConfigGeneral configGeneral, User user) {
        if (user != null) {
            KeycloakHelper.deleteUser(configGeneral, user);
        } else {
            throw new RuntimeException("User is null, cannot delete user");
        }
    }

    private static String generateRandomString() {
        int leftLimit = 97; // letter 'A' and special char included
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

    }

    private static String generateRandomPassword() {
        String upperCaseLetters = RandomStringUtils.random(5, 65, 90, true, true);
        String lowerCaseLetters = RandomStringUtils.random(5, 97, 122, true, true);
        String numbers = RandomStringUtils.randomNumeric(5);
        String specialChar = RandomStringUtils.random(5, 33, 47, false, false);
        String combinedChars = upperCaseLetters.concat(lowerCaseLetters)
                .concat(numbers)
                .concat(specialChar);
        List<Character> pwdChars = combinedChars.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(pwdChars);
        return pwdChars.stream()
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }
}
