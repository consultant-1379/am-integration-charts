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
package com.ericsson.evnfm.acceptance.steps.ui.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openqa.selenium.remote.RemoteWebDriver;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonSteps {

    public static void checkAdditionalAttributes(RemoteWebDriver driver, Map<String, Object> expectedResultsMap, Map<String, Object> actualResultsMap) {
        List<String> listOfGeneralParameters = new ArrayList<String>();
        listOfGeneralParameters.add("releaseName");
        listOfGeneralParameters.add("commandTimeOut");
        listOfGeneralParameters.add("applicationTimeOut");
        listOfGeneralParameters.add("namespace");
        listOfGeneralParameters.add("disableOpenapiValidation");
        listOfGeneralParameters.add("skipJobVerification");
        listOfGeneralParameters.add("persistScaleInfo");

        for(String key : listOfGeneralParameters) {
            for(Map.Entry entry : actualResultsMap.entrySet()) {
                if (entry.getKey().toString().equals(key)) {
                    actualResultsMap.entrySet().remove(entry);
                    break;
                }
            }
        }

        for(Map.Entry entry : expectedResultsMap.entrySet())
        {
            String str = entry.getValue().toString();
            if(entry.getValue().getClass().getName().equals("java.lang.String") && !entry.getKey().toString().contains("configMap")){
                if((str.indexOf('{'))==0){
                    Map<String, Object> map = Arrays.asList(str.replace("{\"", "").replace("\"}", "").replace("\"", "").split(",")).stream().map(s -> s.split(":"))
                            .collect(Collectors.toMap(e -> e[0], e -> (Object)(e[1])));
                    entry.setValue(map);
                }
                else if((str.indexOf('['))==0){
                    List<Object> list = new ArrayList<Object>(Arrays.asList(str.replace("[\"", "").replace("\"]", "").replace("\"", "").split(",")));
                    entry.setValue(list);
                }
            }
        }

        assertThat(expectedResultsMap.entrySet()).containsAll(actualResultsMap.entrySet());
    }

}
