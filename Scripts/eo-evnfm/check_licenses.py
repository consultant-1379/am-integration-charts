import requests
import http
import logging
import json
import argparse


LOG = logging.getLogger(__name__)


class LicenseView:
    def __init__(self, licenseKey, status):
        self.licenseKey = licenseKey
        self.status = status


class EoApi:
    def __init__(self, licenseManagerHost, licenseConsumerHost):
        self.licenseManagerHost = licenseManagerHost
        self.licenseConsumerHost = licenseConsumerHost

    def getPermissions(self, application):
        auth_url = f'http://{self.licenseConsumerHost}/lc/v1/{application}/permissions'
        headers = {
            'Content-Type': 'application/json',
        }
        response = requests.get(auth_url, headers=headers)
        if not response.status_code == http.HTTPStatus.OK:
            LOG.error(f'During connection to License Consumer received exception: {response.text}')
            raise Exception(f'Could not fetch licenses from License Manager')
        return json.loads(response.text)

    def getLicenses(self, productType):
        auth_url = f'http://{self.licenseManagerHost}/license-manager/api/v1/licenses'
        headers = {
            'accept': 'application/json',
            'Content-Type': 'application/json',
        }
        response = requests.get(auth_url, headers=headers, json={"productType": productType})
        if not response.status_code == http.HTTPStatus.OK:
            LOG.error(f'During connection to License Manager received exception: {response.text}')
            raise Exception(f'Could not fetch licenses from License Manager')
        return json.loads(response.text)


class LicensesApi:

    def __init__(self, productType, licenseManagerHost, licenseConsumerHost):
        self.eoApi = EoApi(licenseManagerHost, licenseConsumerHost)
        self.productType = productType

    def prepareLicensesView(self):
        licenses = self.eoApi.getLicenses(self.productType)['licensesInfo']
        licenseViews = []
        for license in licenses:
            licenseView = LicenseView(license['license']['keyId'], license['licenseStatus'])
            licenseViews.append(licenseView)
        return licenseViews
    def preparePermissionsView(self):
        applicationPermissions = dict()
        for application in ['cvnfm', 'vmvnfm']:
            applicationPermissions[application] = '[' + ', '.join(self.eoApi.getPermissions(application)) + ']'
        return applicationPermissions



parser = argparse.ArgumentParser(description='''This script in order to gather licenses and their statuses from NELS
In order to use it - you have to do port forward for 2 services: eric-eo-lm-consumer and eric-eo-lm-combined-server from eo deployment. Command examples:

kubectl -n zyurpin-ns port-forward svc/eric-lm-combined-server 8081:8080
kubectl -n zsehecr-ns port-forward svc/eric-eo-lm-consumer 8082:80

After that this script can be used. Example of usage:

python3 check-licenses.py --product Ericsson_Orchestrator --license-manager localhost:8081 --license-consumer localhost:8082

Example output:
Product type: Ericsson_Orchestrator
License manager host: localhost:8081
Product consumer host: localhost:8082

License              Status
--------------------------------------------
FAT1024423           VALID
FAT1024423/6         VALID
FAT1024423/5         VALID
FAT1024423/4         VALID
FAT1024423/3         VALID
FAT1024423/2         VALID



Application          Permissions
--------------------------------------------
cvnfm                [cluster_management, enm_integration, onboarding, lcm_operations]
vmvnfm               [enm_integration, lcm_operations]
''', formatter_class=argparse.RawDescriptionHelpFormatter)

parser.add_argument('--product', help='Product type. Get it from site values', required=True)
parser.add_argument('--license-manager', help='License manager host. Setup port-forward and specify url here', required=True)
parser.add_argument('--license-consumer', help='License consumer host. Setup port-forward and specify url here', required=True)
args = vars(parser.parse_args())

productType = args["product"]
licenseManagerHost = args['license_manager']
licenseConsumerHost = args['license_consumer']

print(f'Product type: {productType}')
print(f'License manager host: {licenseManagerHost}')
print(f'Product consumer host: {licenseConsumerHost}\n')

api = LicensesApi(productType, licenseManagerHost, licenseConsumerHost)

print("{:<20} {:<20}".format('License','Status'))
print("--------------------------------------------")
for license in api.prepareLicensesView():
    print("{:<20} {:<20}".format(license.licenseKey, license.status))

print("\r\n\r\n\r\n{:<20} {:<20}".format('Application','Permissions'))
print("--------------------------------------------")
for key, value in api.preparePermissionsView().items():
    print("{:<20} {:<20}".format(key, value))

