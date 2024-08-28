# End To End Tests

## Configuration

### General

| Key   |      Value      | Description | Default Value | |
|----------|:-------------:|:-------------|:---:|:---:|
| apiGatewayHost |  String | Host URI where the application is deployed | - | Required |
| vnfmUsername |    String   | User name for the application IDAM | - | Required |
| vnfmPassword | String | Password for the application IDAM | - | Required |

### Onboarding

#### Csar Download Path

| Key   |      Value      | Description | Default Value | |
|----------|:-------------:|:-------------|:---:|:---:|
| csarDownloadPath |  String | Location for CSAR's will be downloaded | `target/csars` | Optional |

#### Packages

| Key   |      Value      | Description | Default Value | |
|----------|:-------------:|:-------------|:---:|:---:|
| packageName |  String | CSAR location to be onboarded | - | Required |
| vnfdId |    String   | Descriptor ID | - | Required |
| packageBeingUpgraded | String | Descriptor ID of the package to upgrade to. (If this package is being used in Upgrade operation) | - | Required  |
| operation | String | Lifecycle operation type this package will be used for. | - | Required |
| testType | String | Type of test the package will be used for. Available options: (rest - ui - bur) | - | Required |
| isMultiYamlVNFD | boolean | If set to true, it will indicate that csar has multiple yamls files in it (currently those yamls are imports used in a vnfd) | false | Optional |
| isContainerVerification | boolean | If set to true, it will verify the resource operations on container level. By default, it will verify the lifecycle operations on pod level. | - | Optional |
| numberCharts | Integer | Number of charts in the multiple helm chart | - | Optional |
| phase | Integer | phase that package will be used in. Only two phases supported. (`1` or `2`) | `0` | Optional |

> By default, `phase` is `0`  and package will be used in Regression flow - Both Rest and UI tests

> If package is a multiple helm chart, `numberCharts` value required.

> Packages should be provided as list:
```json
{
  "onboarding": {
     "csarDownloadPath": "/home/amadm100/release-testing-csars",
     "packages": [
            {
              "packageName": "String",
              "vnfdId": "String",
              "operation": "String",
              "testType": "String",
              "isContainerVerification": "boolean",
              "numberCharts": "integer",
              "phase": "integer"
            }
          ]
        }
}
```
### Cluster

| Key   |      Value      | Description | Default value | |
|----------|:-------------:|:-------------|:---:|:---|
| externalConfigFile | String | Full path to a kube config file that points to a second cluster for multicluster tests | - | Optional |

### Instantiate

| Key      |     Value     | Test used in  | Description   | Default value | |
|----------|:-------------:|:-------------:|:-------------|:--|:---|
cluster | String | Both | The name of the cluster that you want to instantiate to | - | Optional |
namespace | String | Both | Namespace in the cluster that you want to instantiate to | - | Required |
releaseName | String | Both | The name of the resource that you want to instantiate | - | Required |
expectedOperationState | String | Both | The state of the LCM Operation that you expect at the end of the flow | - | Required |
expectedComponentsState | String | UI | The expected state of all the resources components at the end of the flow | - | Required |
applicationTimeOut | String | Both | The application timeout is the maximum time allocated for application instantiation | 3600 | Optional |
commandTimeOut | String | Both | The command timeout is the maximum time allocated for helm or kubectl commands to execute | 300 | Optional |
resourceDescription | String | UI | User defined description of the resource | Empty String | Optional |
cleanUpResources | boolean | Rest | Remove Persistent Volume Claims and Persistent Volumes of an application on failed Instantiate request | - | Optional |
skipVerification | boolean | Rest | Flag indicating whether to bypass the Application Verification Checks | - | Optional |
additionalAttributes | Map<String,Object> | Rest | Map of additional attributes that should be used where Key is the name of the attribute and Value is value you wish to set. Value can be either a String, boolean or Integer | - | Optional |
configurations | Array | Rest | Verifies configmaps data against additional attributes to check if the configmap data is the same. | - | Optional |
day0configuration | Object | Rest | Verifies configuration used to verify day0 configuration which ware provided in vnfd or in additional parameter to instantiate request. | - | Optional |

### Upgrade

| Key      |     Value     | Test used in  | Description   | Default value | |
|----------|:-------------:|:-------------:|:-------------|:---|:---|
| expectedOperationsState | String | Both | The property used to set the expected operation state of the upgraded resource | - | Required |
| expectedComponentsState | String | UI | The property used to set the all of expected components' state of the upgraded resource  | - | Required |
| applicationTimeOut | String | Both | The application timeout is the maximum time allocated for application upgrade | 3600 | Optional |
| commandTimeOut |  String | Both | The command timeout is the maximum time allocated for helm or kubectl commands to execute | 300 | Optional |
| skipVerification | boolean | Rest | Flag indicating whether to bypass the Application Verification Checks | - | Optional |
| additionalAttributes | Map<String,Object> | Both | JSON object used to set the application specific values. Value can be either a String, boolean or Integer | - | Optional |
| configurations | Array | Both | Verifies configmaps data against additional attributes to check if the configmap data is the same. | - | Optional |

> `configurations` should be provided as list:
```json
{
  "configurations": [
    {
      "name": "String",
      "verify": [
        {
          "key": "String",
          "attributeKey": "String"
        }
      ]
    }
  ]
}
```
> `day0configuration`  Example below is for auxiliary secret verification.
```json
{
 "day0configuration": {
      "secretData": {
        "name": "day0-secret",
        "data": {
          "login": "testlogin",
          "password": "testpassword"
        }
      }
    }
}
```
> `additionalAttributes` should be provided as map:
```json
{
 "additionalAttributes": {
       "key-boolean": false,
       "key-string": "value-string"
     }
}
```
> In the case of an upgrade where the csar `spider-app-multi-b-v2` version 1.0.24 or higher is used, the additional parameter `upgrade.downsize
.allowed` should be set to `false` in the `additionalAttributes` json:
```json
{
 "additionalAttributes": {
       "upgrade.downsize.allowed": false
     }
}
```
> This is because the `upgrade.downsize.allowed` parameter is set to `true` by default in the `spider-app-multi-b-v2` vnfd for versions 1.0.24 and 
higher.

### Rollback

| Key      |     Value     | Test used in  | Description   | Default value | |
|----------|:-------------:|:-------------:|:-------------|:---:|:---|
| applicationTimeOut |  String | Both | The application timeout is the maximum time allocated for application instantiation | `3600` | Optional |
| expectedComponentsState | String | Both | The property used to set the all of expected components' state of the scaled resource  | - | Optional |
| expectedOperationState | String | Both | The property used to set the expected state of the scaled resource | - | Optional |

### Scale

| Key      |     Value     | Test used in  | Description   | Default value | |
|----------|:-------------:|:-------------:|:-------------|:---:|:---|
| applicationTimeOut |  String | Both | The application timeout is the maximum time allocated for application instantiation | `3600` | Optional |
| commandTimeOut |    String   | Rest | The command timeout is the maximum time allocated for helm or kubectl commands to execute | `600` | Optional |
| aspectId | String | Both | aspect Id value specified in the VNFD of the csar |
| deployments | JSON Object (Map<String,String>) | Both | Key value pair json object where the key is the service and the value is the value you want it to scale towards. This is used to test that it actually scaled to these values | - | Required |
| expectedComponentsState | String | Both | The property used to set the all of expected components' state of the scaled resource  | - | Optional |
| expectedOperationState | String | Both | The property used to set the expected state of the scaled resource | - | Optional |
| numberOfSteps | String | Rest | The property used to set the level of scale | `1` | Optional |

### Terminate

| Key      |     Value     | Test used in  | Description   | Default value | |
|----------|:-------------:|:-------------:|:-------------|:---|:---|
| expectedOperationState | String | Rest | The property used to set the expected state of the terminated resource | - | Required |
| applicationTimeOut | String | Both | The application timeout is the maximum time allocated for application termination | 3600 | Optional |
| commandTimeOut | String | Rest | The command timeout is the maximum time allocated for helm or kubectl commands to execute | 300 | Optional |
| cleanUpResources | boolean | Rest | Remove Persistent Volume Claims and Persistent Volumes of an application | false | Optional |
| skipVerification | boolean | Rest | Flag indicating whether to bypass the Application Verification Checks | false | Optional |
| pvcTimeOut | String | Rest | Specifies the time to wait for Persistent Volume Claims to delete | - | Optional |

## Test Execution

There are 2 test modules that are available in this project.
1. eric-eo-evnfm-acceptance-testware
    - Performance flow
    - Regression flow
        - Rest tests
        - UI tests
        - BUR tests (Phase 1 and Phase 2)
2. eric-eo-evnfm-acceptance-testware-internal
    - RBAC tests

### In Jenkins

These modules are executed in jenkins using a script from the am-ci-flow repository

```bash

am-ci-flow/scripts/end_to_end_acc_tests.sh

```

* This script interrogates the namespace for the gateway url, username and password to use in testing
* It then pipes these into the template.json file and the output is in the config.json file
* Finally the testware jar is executed with this config.json file.

### Locally

You can execute the tests locally in one of two ways:

1. Execute the script.
2. Run the jar directly.

Regardless of which way you execute it there are a number of things in the template.json file you need to change:

1. Change the location of the csars to where they are on your local filesystem.
2. If you don't want to execute upgrade then only provide one csar.
3. If the vnfdIds are different then they need to be changed also.
4. If you are executing against multiple clusters then update the location of the kubeconfig file.
5. Otherwise delete it so that the tests deploy onto the same cluster as the EVNFM instance is on.
6. Change the namespace.
7. Change the release name.

#### script

1. Have your kubectl pointing to the cluster the EVNFM is deployed on.
2. Provide the namespace that the EVNFM you are testing against is deployed in.

```bash

am-ci-flow/scripts/end_to_end_acc_tests.sh my-namespace

```

#### jar

1. Update the gateway, username and password in the template.json file to the system you are testing against.
2. Execute the jar providing relevant options:

##### Flag options

| Flag      |     Value     | Description   | Default value | |
|:----------:|:-------------:|:-------------:|:---:|:---:|
| `-f` | Path to Config file |  Config file will be passed to execution of jar. | - | Required |
| `-t` | Test type | Tests are tagged for execution. | Regression | Optional |
| `-p` | Phase number | Phase stage will be executed. | `0` | Optional |

###### Available Flag Values:

**Config file:** A json formatted configuration file to be used in test modules. Eg: config.json

**Test Types:** Performance, Regression, Rest, UI and BUR

**Phase:** `1` or `2`

> In `Regression` flow, both UI and Rest based tests are executed. BUR tests are not.

> If test type is selected as BUR, phase number option is required. (Ex: `-p 1`)

###### Regression Flow (Rest and UI tests)
```bash

java -jar target/eric-eo-evnfm-acceptance-testware.jar -f ../template.json

```

###### Run a phase (Only 2 phases are supported. `1` or `2`)

```bash

java -jar target/eric-eo-evnfm-acceptance-testware.jar -f ../template.json -t bur -p 1

```

> If there is no phase specified or entered incorrectly in test execution command, tests in the regression flow will be executed by default.

**Flow examples and tags being used:**

    Regression (Default Flow) :  -f template.json                 ->  cluster, onboarding, rest, ui, delete-packages
    Regression                :  -f template.json -t regression   ->  cluster, onboarding, rest, ui, delete-packages
    Performance               :  -f template.json -t performance  ->  performance
    Rest                      :  -f template.json -t rest         ->  cluster, onboarding, rest, delete-packages
    UI                        :  -f template.json -t ui           ->  cluster, onboarding, ui, delete-packages
    Bur (Phase 1)             :  -f template.json -t bur -p 1     ->  cluster, onboarding, bur
    Bur (Phase 2)             :  -f template.json -t bur -p 2     ->  cluster, onboarding, bur, delete-packages

### Phases

There are currently two phases in the regression flow and they can be executed individually.

To execute a phase, add the phases in the configuration file (template.json) for onboarding packages list.
Add `"phase": 1` or `"phase": 2` to the package to use in each phase. Note that the user can also choose between Container or Pod based verification when running phases. 

```json
[
      {
        "packageName": "/home/amadm100/release-testing-csars/spider-app-a-1.0.0.csar",
        "vnfdId": "b0b99535-28a1-4531-9c12-7d194b660543",
        "operation": "instantiate",
        "testType": "rest",
        "isContainerVerification": false,
        "phase": 1
      },
      {
        "packageName": "/home/amadm100/release-testing-csars/spider-app-c-1.0.0.csar",
        "vnfdId": "2ce9484e-85e5-49b7-ac97-445379754e37",
        "packageBeingUpgraded": "36ff67a9-0de4-48f9-97a3-4b0661670934",
        "operation": "upgrade",
        "testType": "ui",
        "isContainerVerification": true,
        "phase": 2
      }
]
```

Run the [jar command](#to-run-a-phase) adding phase flag with the phase number as an argument for which phase to execute.

#### Phase 1

In *Phase 1*, the packages defined in the configuration file will be used to test lifecycle operations using REST based tests.

After *Phase 1*, onboarded packages and instantiated resources will not be cleaned and will remain in the cluster to be used in *Phase 2*.

- Onboard - Will onboard packages as marked *phase 1*.
- Instantiate - Will instantiate the packages as marked *phase 1*.
- Scale - Will scale the package upgraded.

#### Phase 2

In *Phase 2*, the packages defined in the configuration file will be used to test lifecycle operations using REST based tests.
This time, after onboarding the packages defined for *Phase 2*, resources instantiated in *Phase 1* will be used for Upgrade operation.

After Upgrade and Terminate, cluster will be cleaned up including the test resources from *Phase 1*.

- Onboard - Will onboard packages as marked *phase 2*.
- Upgrade - Will use the package for upgrade as marked *phase 2* and upgrade that package to defined package in *packageBeingUpgraded* value.
- Terminate - Will terminate the resources.
- Clean Up Resources - Will clean up all identifiers.
- Delete Onboarded Packages - Will  delete all the packages onboarded including those onboarded in *phase 1*.