# End To End TestNG Tests


## General
This is the start of EVNFM moving to TestNG as the test execution engine.

These tests are currently designed to run from Maven using the `acceptance-testng` profile.
It is required to specify a particular suite file, or a list of suite files to run using the
`-Dsurefire.suiteXmlFiles` system parameter.

An example command would be
```
mvn clean install  -P acceptance-testng -Dsurefire.suiteXmlFiles=src/main/resources/suites/upgrade.xml
```

**Note:** See below to run the tests programmatically.


## TestNG Suite Structure
Test execution is controlled by a TestNG Suite, we can use this suite to orchestrate how the test is executed
e.g. parallel or sequential etc. Please see https://testng.org/doc/documentation-main.html#testng-xml for more information.

When constructing the suite file, you should consider a few things 

* You should give your suite a realistic self-explanatory name that informs of the purpose of the suite
* You should give your tests a self-explanatory name that informs what you are testing
* You should consider if you want your tests executed in parallel or in sequence? (see TestNg link above for detail on this)

We also use the suite file to inform the execution where to retrieve the data that is to be used in the test execution.
We do this using the `<paramater>` tag in the suite file e.g.
```
  <parameter name="packagesToOnboard" value="vduOnboarding.yaml"/>
  <parameter name="cnfsToInstantiate" value="vduInstantiate.yaml"/>
```

In the above example we pass the name of the file containing the testdata, then in the dataprovider logic, we read in the
contents of this file, create the java objects and pass these objects to the test case as an array. This allows us to
re-use the same dataprovider & test case logic for different test executions.


## Executing tests using data outside the repo
The wanted position is that test data is outside the test case repo so, to that effect you should pass a full path to the folder that
 contains the test data using the `-DtestDataFolder` system property e.g.
```
-DtestDataFolder=<full path the testdata folder>
```
When this parameter is given, the test data is taken from this folder, not from the repo.

**Note:** The structure of this folder must match the structure of the `src/main/resources/testData` folder in the
`eric-eo-evnfm-testng-acceptance-testware` module in terms of the file names and directory structure.

## Running The Suite Locally
The following section details how to run the TestNG xml suite locally with IntelliJ.

**Prerequisites:**
* An EVNFM environment you can use.
* A valid TestNG suite .xml eg. `upgrade.xml`
* All relevant test data that's needed for the test suite eg. `src/main/resources/testData`
* A valid Csar for example: `spider-app-b-1.0.8-imageless.csar`

**Steps:**
1) In the `testGlobal.properties` file update the evnfmUrl, userName & userPassword to use your EVNFM.
2) Update the values in your testData files to be specific to the csar you are providing. Please also change the namespace in which it will deploy on as to not interfere with anyone else.
3) Then create a new TestNG run configuration.
4) Change **Test kind** to Suite and point the Suite to your xml file eg. `upgrade.xml`. *(If you see an Unable to parse error, ignore as it is usually a bug with IntelliJ)*
5) Add the following to **Vm options:** `-DtestDataFolder=GREAT\PATH\testData`
6) Save and run / debug the configuration.

## Running UI Tests
The UI test must be contained inside a class. The suite file will call all methods inside the class. 
It uses before and after classes annotation methods to configure.
It will create a new user and use that to login into EVNFM. The user can be used in the test methods of that class.
The user used in UI tests will be removed after all test methods in the class have been completed has finished.
When creating a new test class for UI you must extend `UiBase`. The UiBase.class will automatically add this functionality.

When running locally the user can add a parameter to the build to point to a webdriver `-Dwebdriver.gecko.driver=C:\firefox\geckodriver.exe`. 
This can be used when running locally to view the UI tests in the local browser. Only **firefox** is supported currently.

Without the webdriver parameter it will use docker image to run tests. This is used when running in the CI flow.

### CSAR Usage for Onboarding Tests (cnfOnboarding.yaml)
When using packageName to onboard a CSAR, you can either pass a remote location of a package or a local path.
If package is already in the local file system (`csarDownloadPath`) it will skip download and use the existing file.
> Note: Packages saved to `target/csars` folder by default if `csarDownloadPath` is not defined in the properties.

#### cnfOnboarding.yaml
```yaml
packages:
  - packageName: "https://arm.seli.gic.ericsson.se/artifactory/proj-eo-drop-helm/eric-eo/eric-eo-1.20.0-204.tgz"
    vnfdId: "b0b99535-28a1-4531-9c12-7d194b660543"
    skipImageUpload: true
    timeOut: 10
  - packageName: "/home/amadm100/testing-csars/spider-app-a-1.0.8-imageless.csar"
    vnfdId: "b0b99535-28a1-4531-9c12-7d194b660543"
    skipImageUpload: true
    timeOut: 10
```

#### cnfInstantiate.yaml
```yaml
cnfsToInstantiate:
 - vnfdId: "multi-chart-477c-aab3-2b04e6a383"
   vnfInstanceName: e2e-gerrit-testng-rest
   vnfInstanceDescription: Example description
   namespace: e2e-gerrit-testng-rest
   cluster:
    name: <NAME> # Required
    localPath: <PATH> # Optional
   expectedOperationState: Completed
   expectedComponentsState: Running
   cleanUpResources: true
   skipVerification: false
```
**cluster:** This property is the model for ClusterConfig and it will be used in EVNFM.
- **name:** This property will be the name of the cluster config file. (ie. hanh062 or test.config)
- **localPath:** Path to the cluster config file. This property is for the internal commands such as Kubernetes API client or Helm commands. This property will be used to
  pass to API calls instead of EVNFM system.

> If localPath is not a predefined cluster in the clusterConfig folder and if it is an actual path, file name will take precedence for `cluster.name`. (ie. if localPath is /path/to/cluster.config, name will be cluster.config)

For example;
```java
var k8sClient = new KubernetesAPIClient(cnfsToInstantiate.getCluster().getLocalPath());
```
```java
private static final String HELM_RELEASE_GET_VALUES_FROM_NS = "helm3 get values %s -n %s --kubeconfig %s";
String command = String.format(HELM_RELEASE_GET_VALUES_FROM_NS, releaseName, cnaToHeal.getNamespace(), cnaToHeal.getCluster().getLocalPath());
```

> `kubeConfig` is an optional property. **If it is not defined, `cluster` value will be checked if there is a mapped value for it in the cluster
> configs folder.**

Examples after setting values for an EvnfmCnf.ClusterConfig model will be as following:

**Config:**
```yaml
   cluster:
    name: hahn062
```
**Result:**
```java
List<EvnfmCnf> cnfsToInstantiate = configInstantiate.getCnfsToInstantiate();
ClusterConfig clusterConfig = cnfsToInstantiate.get(0).getCluster();
clusterConfig.getName();       // hahn062
clusterConfig.getLocalPath();  // clusterConfigs/hahn062.config 
```

| Config (cluster.name)   |  Config  (cluster.localPath) | Result  (cluster.name) | Result  (cluster.localPath) | Env Variable  (KUBECONFIG_PATH) |
|:----------:|:-------------|:------------|:----------------|:------|
| hahn062 |  null                                       | hahn062       | clusterConfigs/hahn062.config | null                  |
| hahn062 |  hahn061                                    | hahn061       | clusterConfigs/hahn061.config | null                  |
| hahn062 |  /path/to/test.config                       | test.config   | /path/to/test.config          | null                  |
| test1   |  /path/to/test.config                       | test.config   | /path/to/test.config          | null                  |
| hahn062 |  ${KUBECONFIG_PATH:-/path/to/test.config}   | test.config   | /path/to/test.config          | null                  |
| test2   |  ${KUBECONFIG_PATH:-hahn064}                | hahn064       | clusterConfigs/hahn064.config | null                  |
| test3   |  ${KUBECONFIG_PATH:-hahn064}                | test.config   | /path/to/test.config          | /path/to/test.config  |




Value of the `localPath` property can be:
- a path to the cluster configuration file (`/path/to/cluster1.config`)
- one of the predefined cluster name (`hahn062`)
- environment variable that points to a config path (`${KUBE_CONFIG_PATH:-/path/to/test.config}`)

**Current Files in the cluster configs folder:**
- hahn061
- hahn062
- hahn064
- hahn0131

**Acceptable values for `cluster` property:**

Values for `name`:

- hahn062
- /path/to/cluster.config
- ${KUBECONFIG_PATH:-hahn064}

Values for `localPath`:

- "hahn062"                                       # Predefined value
- "/path/to/cluster.config"                       # Actual path
- "${KUBECONFIG_PATH:-hahn064}"                   # As environment variable
- "${KUBECONFIG_PATH:-/path/to/cluster.config}"   # As environment variable

**Adding new cluster file:** Any addition to the folder `am-integration-charts/testng/clusterConfigs` will be mapped with the file name.

Example: If `todd81.config` file will be added, then it is possible to access to this cluster as the name of the file added. (in
this case `todd81`
will be accepted as a kubeconfig value.)
These predefined values are mapped to their kubernetes config file which resides in `am-integration-charts/testng/clusterConfigs`. Any addition to
this folder will be accepted and mapped its file name without its extension (ie. `.config`).

#### testGlobal.properties
```
#[Default EVNFM Details]
evnfmUrl=https://evnfm.hahn131.rnd.gic.ericsson.se
idamUrl=https://iam.hahn131.rnd.gic.ericsson.se
idamAdminUser=admin
idamAdminPassword=test
idamClientId=eo
idamRealm=master

## Optional Parameters
csarDownloadPath=path/to/save/csars
```

Path for downloading Csars can be set using following property (`csarDownloadPath`) in the testGlobal.properties