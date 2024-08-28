> **Warning:**
It is strongly advised that no Package Management operations are running when applying either the Manual Recover Procedure
or the Proactive Script.
>
> **If running Garbage Collection and restarting PODs:**
> ensure that there are no Lifecycle Management Operations ongoing.

><span style="color: red;">Before running the script with the "--fix" flag, the operator should check the list of images to be removed to ensure
that the images used by the packages will not be removed. </span>

>The result of the script “--fix” flag must be saved in order to be able to restore images. To save the logs to a file, you can run the script with the “--file” flag.
the log file will be saved in the root directory

>**Note:**
Executing the Manual Procedure, or running the Proactive Script (registry.py) in Check-fix mode will clean up the Active Site.
>
>Subsequent to a GR Switchover, it may be necessary to clean the new Active Site if a corruption has occurred on the
> Passive Site prior to switchover.
>
> If this is the case, use the Proactive script (registry.py) to identify and optionally fix this issue.



### Background


Under certain circumstances Geo Image Synchronization, a precondition for GR Switchover, will fail to synchronize Docker Images.

This will typically happen for four scenarios


1. Malformed images: With issues in Layers (Pre EO 22.0.0)
2. Malformed images: With NULL or empty tags (Pre EO 22.0.0)
3. Malformed images: Tags with empty content
4. Malformed images: With NAME or MANIFEST unknown

For the scenarios above, GR Image Sync will fail, and GR Switchover will not be allowed/will not be available.

This Readme file describes:

- A Manual recover procedure for GR Image Sync to allow GR Availability to be restored if blocked by any of the four above Malformed Image scenarios.
- A registry.py script that will allow the operator to identify Malformed Images, and, optionally, clean up Malformed Images for scenarios 1 to 4 above.

For scenario 1 and 3, corrupted tags are removed.

For scenario 2 and 4, repositories are moved and the operator can restore these manually should unexpected side effects occur due to their move.


### How to identify if issue exists:

Check Logs for indication of a Geo Image Sync failure due to Malformed Images For example:

> “below images are failing to synchronize, please verify manually:......”

> “Failed to pull layer for image proj-common-assets-cd/moni.............”

> “Call to container registry for tags inside image projcommonassetscd/monitoring/pm/eric-pm-sftp returned empty tags,
> processing with empty for synchronization.....”

> "{"timestamp":"2022-03- 02T01:00:04.805Z","version":"1.0.0","message":"Unexpected status code has been received from
> rest client: 404 NOT_FOUND. Response:
> {"errors":[{"code":"NAME_UNKNOWN","message":"repository name not known to registry","detail":{"name":
> "proj-bssf/adp-log/release/ericdatasearchenginebragent"}.............................."


If this has occurred:

- Run registry.py in ‘Check’ mode from the EO working directory.

  This will identify the Malformed Image artefacts.


    For more details on the registry.py script., see: “Proactive Script with option to clean up Malformed Images”

> **Note:**
> If another issue has caused the Geo Image Sync to fail, please refer to the Geo Redundancy Deployment Guide – Troubleshooting section
>
> • Run the recovery procedure below

>**Note:**
>Whilst registry.py identifies offending artefacts and their locations:
>It is the responsibility of the operator to know in which CSAR packages those corrupt container images
>belong.

As a Manual Procedure to fix corrupt container images in container registry there are three recommended options:

- **Push the image again**

  A registry restart will be necessary if you have moved or deleted the repository before a push can be proceeded with.
  By simply uploading (i.e. pushing) the container image again. The container registry should populate expected layers, folders and links


- **Clean corrupt image references from container registry file system**

  Operator discretion advised as this involves deletion of corrupted artefacts, example is for “Malformed images: With NAME or MANIFEST unknown”

<br><br>
For OpenShift deployments replace ‘kubectl’ with ‘oc’ in the commands below.

Step 1: Copy the /v2/repository/<image> folder from container registry and paste it in a folder outside container-registry pod

Step 2: Remove the /v2/repository/<image> folder which will remove the "link" information,
1. Get the name of the container registry pod
   a. kubectl -n <evnfm-namepsace> get pods | grep eric-lcm-container
2. Exec onto it
   a. kubectl -n <evnfm-namespace> exec -it <container-registry-pod-name> /bin/sh
3. Change directory to
   a. cd /var/lib/registry/docker/registry/v2/repositories
4. Delete <image> folder

Step 2 (Optional): Run /garbage-collection.sh - which will remove blobs related information for that image

>**Note:**
It is recommended that the Container Registry POD is also restarted once garbage collection has completed.
>
>To do his:
> kubectl -n <evnfm-namespace> delete pod <container-registry-pod-name>

Step 3: (Optional) On board that image again (optional)


### For all cases above:

- GR Image Sync will run at next scheduled slot (default scheduled for 15 minute intervals)
- Check status of GR Image Sync, once successful it may take a number of cycles before Geo Switchover registers as Available again.
- GR Switchover should become available once the GR Back up and GR Image Sync are successful within the GR Cycle Window (= GR Backups, default 15 mins, and Image Sync Cycles are aligned).
- If it is not possible to wait for the Cycle to align again:

  GR BUR Orchestrator pod can be restarted to forcefully restart GR Cycle. Run the following command:
    ```
    kubectl -n <evnfm-namespace> delete pod <eric-gr-bur-orchestrator-POD-name>
    ```

### Proactive Script with option to clean up Malformed Images

The Script registry.py (Kubernetes and OpenShift Script) is provided for use at the operators’ risk.

This script must be loaded in the Deployment Manager’s Host.

This script can be triggered manually, or the operator can create a script, for example driven by cron, to trigger this script.

If this approach is used, the following is strongly recommended:

- Run this Script from Deployment Manager Host
  <br><br>
- Configure cron (timings) to run at off peak times, particularly **outside times when Package Management Activities are ongoing**.

  Package Management activities that run while the registry.py script is running in check – fix mode may lead to inconsistent handling of those packages.
  <br><br>
- Create a script that will pipe the output of the registry.py to file to log actions by registry.py.

The registry.py script works in two modes:

- Check in read-only mode

  Use this mode if you want to identify faulty repositories/tags.
  This effectively gives a consolidated view of Image Issues already available in the GR Logs.


- Check and fix mode

  Use this mode if you want the script to ‘fix’ the GR Image Sync issue related to Malformed Images by:
  <br><br>
  - For scenarios 2 & 4:

    Moving the faulty repository out of the repository tree.
    Repositories are moved and the operator can restore these manually should unexpected side effects occur due to their move.
    <br><br>
  - For scenarios 1 & 3:

    Removing the offending tags.


- Use of “Check and fix” mode is entirely at the risk of the operator.


> **Note:**
> If Malformed Images are detected by the script, it is recommended to rerun the registry.py script after cleaning up (either manually or in Check and Fix mode) the reported issues to ensure that no issues persist until script indicates no issues.
>
>Check and fix mode will not run garbage collection, which means that any blobs associated with the Malformed Images will persist in storage.
>
>Garbage collection can be run manually outside this script if necessary. Check and fix will not restart the GR “BUR Orchestrator” POD.
>
>This can be done manually, outside this script should it not be possible to wait for the GR Cycle to align again.


### Prerequisites

A machine with the following:

- network access to the cluster
- python3 installed
- kube config file

  This kube config file will be used by the script to access the cluster.

- Kubernetes binary
  - In the case of CCD:

    kubectl must be available to the script.

    It must be in the supported range of the version of Kubernetes on the cluster.

  - In the case of OpenShift:

    oc or kubectl must be available to the script

    It must be in the supported range of the version of OpenShift/Kubernetes on the cluster.
-  No Package Management operations are in progress in EVNFM.

### Execution
```
python python3 registry.py --help
```

This will show the arguments.



1. Check in read-only mode

   This is a ‘read only’ mode and looks for repositories with null tags or that report as NOT_FOUND

   Usage:
   ```python
   python3 </path/to/registry.py> --url <Registry URL> --user <RegistryUser> --password <RegistryPassword> --action check
   ```

<br>

2. Recovery mode

   This action "--action recover" is used to restore tags or repositories marked as invalid.

   It is also necessary to pass an argument with the name of a specific folder to restore: "--folder <folder_name>" or a path to a file with a list of folders to restore "--folders <path_to_file>"

><span style="color: red;">**WARNING:** </span>
>
> The script restores only those folders that were created by the current version of the script.
>
> A distinctive feature of the new version of the script will be the presence “recoverable” in the folder name: "__"
>
> Example: “dockerhub-ericsson-remote__busybox.invalid_repo.230722183501.recoverable”

Usage for one folder:
   ```python
   python3 </path/to/registry.py> --url <Registry URL> --user <RegistryUser> --password <RegistryPassword> --action recovery -–folder dockerhub-ericsson-remote__busybox.invalid_repo.230722183501.recoverable --kubectl </path/to/kubectl> - -kubeconfig </path/to/kube/config> --namespace <namespace containing registry pod>
   ```

Usage for folder list:
   ```python
   python3 </path/to/registry.py> --url <Registry URL> --user <RegistryUser> --password <RegistryPassword> --action recovery --folders ./malformed_images.txt --kubectl </path/to/kubectl> --kubeconfig
   ```
<br>

3. the "inuse" action

   It is used for check the records in the database for images that will be found with problems when the "check" action is performed.

   If the found problematic images have some state, the script will display the package name, the package state and the image name
   <br><br>
   Usage:
   ```python
   python3 </path/to/registry.py> --url <Registry URL> --user <RegistryUser> --password <RegistryPassword> --action inuse --kubectl </path/to/kubectl> --kubeconfig </path/to/kube/config> --namespace <namespace containing registry pod>
   ```

<br>

4. Check and fix mode.

   This performs the same checks as the read only mode.
   For each faulty repository found, it will exec a command to move the directory out of the repository tree.
   <br><br>
   Usage:
   ```python
   python3 </path/to/registry.py> --url <Registry URL> --user <RegistryUser> --password <RegistryPassword> --action check --fix --kubectl </path/to/kubectl> --kubeconfig </path/to/kube/config> --namespace <namespace containing registry pod>
   ```


### Parameters:

- --url \<Registry URL>
- --user \<RegistryUser>
- --password \<RegistryPassword>
- --action check

  Check for Malformed Images

- --fix

  Check and fix Malformed Images

- --kubect </path/to/kubectl> in the case of OpenShift: kubectl </path/to/oc>
- kubeconfig </path/to/kube/config>
- namespace <namespace containing registry pod>



### For all cases above:

- GR Image Sync will run at next scheduled slot (default scheduled for 15 minute intervals)
- Check status of GR Image Sync, once successful it may take a number of cycles before Geo Switchover registers as Available again.
- GR Switchover should become available once the GR Back up and GR Image Sync are successful within the GR Cycle Window (= GR Backups, default 15 mins, and Image Sync Cycles are aligned).
- If it is not possible to wait for the Cycle to align again:

  GR BUR Orchestrator pod can be restarted to forcefully restart GR Cycle. Run the following command:
    ```
    kubectl -n <evnfm-namespace> delete pod <eric-gr-bur-orchestrator-POD-name>
    ```



### Additional Information:
a) Recommendations for identifying corrupt container images

A healthy image in CR will have the following characteristics,

1. Each image will have 3 folders in v2/repositories folder - _layers, _manifest, _uploads
2. _manifest folder should have "tags" folder e.g. tags/latest/current/link
3. This "link" file should have a SHA256 digest which will link to a folder in blobs folder e.g. blobs/sha256/<first two numbers from digest>/<that SHA256 digest>/data
4. This "data" file should have details of layers and other characteristics

If an image does not have the expected folders (_layers, _manifest) - Is should be considered as corrupt.

In case of the _uploads folder - When an image is uploaded the docker registry will create this folder if it doesn't exist.

**Example registry.py output when issues are detected:**

>WARNING:root:Problems detected: 2
>
>WARNING:root: {'type': 'digest_absent', 'repository': 'dockerhub-ericsson-remote/busybox', 'tag': '1.75.0'}
>
>WARNING:root: {'type': 'nulltags', 'repository': 'proj-am-fn/cm-agent'}



b) If the script encounters a "nulltags" or "notfound" repository problem and the image is not in the lcm-container-registry by the path ‘../v2/repositories’, then the script will skip moving the image and log it as a "warning"