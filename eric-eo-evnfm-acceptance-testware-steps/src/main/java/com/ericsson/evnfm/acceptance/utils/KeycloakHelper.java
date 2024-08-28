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
package com.ericsson.evnfm.acceptance.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.Response;

import com.ericsson.evnfm.acceptance.models.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.configuration.ConfigGeneral;

import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getObjectMapper;

public class KeycloakHelper {

    private KeycloakHelper(){}

    private static Logger logger = LoggerFactory.getLogger(KeycloakHelper.class);

    public static String createUser(ConfigGeneral configGeneral, String username, String password, String... roles) {
        logger.info("Attempting to create user {} with role {}", username, roles);
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(configGeneral.getIdamHost() + "/auth")
                .realm(configGeneral.getIdamRealm())
                .grantType(OAuth2Constants.PASSWORD)
                .clientId(configGeneral.getIdamClient())
                .clientSecret(configGeneral.getIdamClientSecret())
                .username(configGeneral.getIdamAdminUsername())
                .password(configGeneral.getIdamAdminPassword())
                .build();

        UserRepresentation user = getUserRepresentation(username);

        RealmResource realmResource = keycloak.realm(configGeneral.getIdamRealm());
        UsersResource userResource = realmResource.users();


        Response response = null;
        try {
            response = userResource.create(user);
        } catch (Exception e) {
            logger.info("Failed to create user. Error Response: {}", response);
            throw e;
        }
        String userId = CreatedResponseUtil.getCreatedId(response);
        UserResource saveUserResource = userResource.get(userId);

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(password);

        saveUserResource.resetPassword(credentialRepresentation);
        List<RoleRepresentation> rolesList = realmResource.roles().list();
        List<String> rolesList2 = new ArrayList<>();
        for (RoleRepresentation roleOption : rolesList) {
            rolesList2.add(roleOption.toString());
        }
        for (String role : roles) {
            if (!role.isEmpty() && rolesList2.contains(role)) {
                RoleRepresentation realmRole = realmResource.roles().get(role).toRepresentation();
                userResource.get(userId).roles().realmLevel().add(Collections.singletonList(realmRole));
            }
        }
        logger.info("User {} has been created successfully", user.getUsername());
        return userId;
    }

    private static UserRepresentation getUserRepresentation(String username) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setUsername(username);

        return userRepresentation;
    }

    public static void deleteUser(ConfigGeneral configGeneral, User user) {
        String username = user.getUsername();
        logger.info("Attempting to delete user {}", username);
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(configGeneral.getIdamHost() + "/auth")
                .realm(configGeneral.getIdamRealm())
                .grantType(OAuth2Constants.PASSWORD)
                .clientId(configGeneral.getIdamClient())
                .clientSecret(configGeneral.getIdamClientSecret())
                .username(configGeneral.getIdamAdminUsername())
                .password(configGeneral.getIdamAdminPassword())
                .build();


        RealmResource realmResource = keycloak.realm(configGeneral.getIdamRealm());
        UsersResource usersResource = realmResource.users();

        try {
            usersResource.delete(user.getId());
        } catch (Exception e) {
            logger.info("Failed to delete user. {}", username);
            throw e;
        }

        logger.info("User {} has been delete successfully", username);

    }

    public static String getClientSecret(ConfigGeneral configGeneral) {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(configGeneral.getIdamHost() + "/auth")
                .realm(configGeneral.getIdamRealm())
                .grantType(OAuth2Constants.PASSWORD)
                .clientId("admin-cli")
                .username(configGeneral.getIdamAdminUsername())
                .password(configGeneral.getIdamAdminPassword())
                .build();

        RealmResource realmResource = keycloak.realm(configGeneral.getIdamRealm());
        List<ClientRepresentation> byClientId = realmResource.clients().findByClientId(configGeneral.getIdamClient());
        Optional<ClientRepresentation> first = byClientId.stream().filter(item -> item.getClientId().equals(configGeneral.getIdamClient())).findFirst();

        String clientSecret = null;
        if(first.isPresent()) {
            String installationConfig = realmResource.clients().get(first.get().getId()).getInstallationProvider("keycloak-oidc-keycloak-json");
            try {
                JsonNode jsonNode = getObjectMapper().readValue(installationConfig, JsonNode.class);
                clientSecret = jsonNode.get("credentials").get("secret").asText();
            } catch (JsonProcessingException e) {
                logger.info("Could not parse keycloak installation Json to get client secret");
            }
        }

        return clientSecret;
    }
}
