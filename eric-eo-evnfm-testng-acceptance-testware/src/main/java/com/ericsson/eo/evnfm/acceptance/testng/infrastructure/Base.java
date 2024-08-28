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
package com.ericsson.eo.evnfm.acceptance.testng.infrastructure;

import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jetbrains.annotations.NotNull;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigGeneral;

public abstract class Base {
    private static final int RANDOM_SEED = 10000;
    private static final Random random = new Random();
    protected User user;
    protected List<EvnfmCnf> cnfs = new CopyOnWriteArrayList<>();
    protected ConfigGeneral configGeneral;

    protected Base() {
        configGeneral = getConfigGeneral();
    }

    @BeforeClass(alwaysRun = true)
    public void prepare() {
        if (Objects.equals(EVNFM_INSTANCE.isDracEnabled(), "true")) {
            user = Idam.createUserWithDomainRoles(configGeneral);
        } else {
            user = Idam.createUser(configGeneral);
        }
    }

    public boolean isTestSuitePassedSuccessfully(final ITestContext iTestContext) {
        return iTestContext
                .getFailedTests()
                .getAllMethods()
                .stream()
                .noneMatch(method -> method.getRealClass().equals(this.getClass()));
    }

    @AfterClass(alwaysRun = true)
    public void cleanUpUsers() {
        Idam.deleteUser(configGeneral, user);
    }

    @NotNull
    protected ConfigGeneral getConfigGeneral() {
        ConfigGeneral configGeneral = new ConfigGeneral();
        configGeneral.setIdamHost(EVNFM_INSTANCE.getIdamUrl());
        configGeneral.setIdamRealm(EVNFM_INSTANCE.getIdamRealm());
        configGeneral.setIdamClient(EVNFM_INSTANCE.getIdamClientId());
        configGeneral.setIdamClientSecret(EVNFM_INSTANCE.getIdamClientSecret());
        configGeneral.setIdamAdminUsername(EVNFM_INSTANCE.getIdamAdminUser());
        configGeneral.setIdamAdminPassword(EVNFM_INSTANCE.getIdamAdminPassword());
        configGeneral.setHelmRegistryUrl(EVNFM_INSTANCE.getHelmRegistryUrl());
        configGeneral.setDracEnabled(EVNFM_INSTANCE.isDracEnabled());
        return configGeneral;
    }

    protected static String randomString(String base) {
        return base + randomInt();
    }

    protected static int randomInt() {
        return random.nextInt(RANDOM_SEED);
    }
}
