#!/usr/bin/env python3

import argparse
import http
import json
import logging
import os
import pathlib
import requests
import yaml

TRUSTED_CERTIFICATES_LIST_NAME = 'onboarding-trusted-certs'
EO_CERTIFICATES_LIST_NAME = 'eo-certs'
CERTIFICATE_AUTHORITY_PATH = 'certificates/intermediate-ca.crt'
TRUSTED_CERTIFICATES_DIRECTORY = 'certificates/trusted'
EO_CERTIFICATES_DIRECTORY = 'certificates'
USERNAME = 'username'
PASSWORD = 'password'


SUBPARSER_NAME = 'command'
LIST_CERTIFICATES_COMMAND = 'list-certificates'
INSTALL_CERTIFICATES_COMMAND = 'install-certificates'
LIST_EO_CERTIFICATES_COMMAND = 'list-eo-certificates'
INSTALL_EO_CERTIFICATES_COMMAND = 'install-eo-certificates'

TRUSTED_CERTIFICATES_PREDICATE = lambda filename: True
EO_CERTIFICATES_PREDICATE = lambda filename: filename.endswith('.crt')

LOG = logging.getLogger(__name__)
logging.basicConfig(format='[%(asctime)s] [%(funcName)s] [%(levelname)s]: %(message)s', level=logging.INFO)


class Certificate:
    def __init__(self, name, certificate):
        self.name = name
        self.certificate = certificate


class EoApi:
    def __init__(self, host_name, username, password):
        self.host_name = host_name
        self.username = username
        self.password = password

    def login(self):
        self._verify_ca_exists()
        auth_url = f'https://{self.host_name}/auth/v1'
        headers = {
            'Content-Type': 'application/json',
            'X-login': self.username,
            'X-password': self.password
        }
        response = requests.post(auth_url, headers=headers, verify=CERTIFICATE_AUTHORITY_PATH)
        if not response.status_code == http.HTTPStatus.OK:
            LOG.error(f'Received exception while trying to obtain auth token: {response.text}')
            raise Exception(f'Could not obtain auth token')
        return response.text

    def list_trusted_certificates(self, trusted_certificate_list_name):
        self._verify_ca_exists()
        token = self.login()
        list_certificates_url = f'https://{self.host_name}/certm/nbi/v3/trusted-certificates/{trusted_certificate_list_name}'
        headers = {
            'Accept': 'application/json',
            'cookie': f'JSESSIONID={token}'
        }
        response = requests.get(list_certificates_url, headers=headers, verify=CERTIFICATE_AUTHORITY_PATH)
        return response

    def install_trusted_certificates(self, trusted_certificate_list_name, certificates):
        self._verify_ca_exists()
        token = self.login()
        install_certificates_url = f'https://{self.host_name}/certm/nbi/v3/trusted-certificates/{trusted_certificate_list_name}'
        headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'cookie': f'JSESSIONID={token}'
        }
        body = {
            'description': 'EO trusted certificate list installed by certificate management script',
            'certificates': self._build_certificates_list(certificates)
        }
        response = requests.put(install_certificates_url, data=json.dumps(body),
                                headers=headers, verify=CERTIFICATE_AUTHORITY_PATH)
        return response

    @staticmethod
    def _build_certificates_list(certificates):
        result = []
        for certificate in certificates:
            result.append({
                'name': certificate.name,
                'certificate': certificate.certificate
            })
        return result

    @staticmethod
    def _verify_ca_exists():
        if not os.path.exists(CERTIFICATE_AUTHORITY_PATH):
            raise Exception(f'EO certificate authority not found at {CERTIFICATE_AUTHORITY_PATH}')


def get_site_values_file():
    matched_site_values_files = list(pathlib.Path('./').glob('site_values_*.yaml'))
    if len(matched_site_values_files) != 1:
        raise Exception('The working directory must contain only one site_values_<version>.yaml file for this '
                        'operation. Please ensure one site_values_<version>.yaml file exists. '
                        f'Files found: {matched_site_values_files}')
    return matched_site_values_files[0]


def get_yaml_property(yaml_object, property_name, parent=None):
    if property_name not in yaml_object:
        if parent is None:
            raise Exception(f'Expected site values to contain {property_name} property')
        else:
            raise Exception(f'Expected {parent} section of site values to contain {property_name}')
    return yaml_object[property_name]


def parse_site_values():
    site_values_file = get_site_values_file()
    with open(site_values_file) as read_stream:
        values = yaml.safe_load(read_stream)
        return values


def load_eo_values():
    values = parse_site_values()
    global_section = get_yaml_property(values, 'global')
    hosts_section = get_yaml_property(global_section, 'hosts', 'global')
    return get_yaml_property(hosts_section, 'vnfm', 'global')


def load_eo_creds():
    creds = {}
    values = parse_site_values()
    common_base_section = get_yaml_property(values, 'eric-oss-function-orchestration-common')
    nbi_section = get_yaml_property(common_base_section, 'eric-eo-evnfm-nbi', 'eric-oss-common-base')
    rbac_section = get_yaml_property(nbi_section, 'eric-evnfm-rbac', 'eric-oss-common-base')
    default_user_section = get_yaml_property(rbac_section, 'defaultUser', 'eric-oss-common-base')
    creds[USERNAME] = get_yaml_property(default_user_section, 'username', 'eric-oss-common-base')
    creds[PASSWORD] = get_yaml_property(default_user_section, 'password', 'eric-oss-common-base')
    return creds


def load_certificate(certificate_file):
    with open(certificate_file) as read_stream:
        return ''.join(read_stream.readlines())


def load_certificates(certificate_directory, filename_filter):
    logging.info(f'Loading certificates from {certificate_directory} directory')
    certificate_files = [filename for filename in os.listdir(certificate_directory)
                         if os.path.isfile(os.path.join(certificate_directory, filename))
                         and filename_filter(filename)]
    if certificate_files.__len__().__eq__(0):
        LOG.info(f'The directory {certificate_directory} is empty')
    return [Certificate(certificate_file, load_certificate(
        os.path.join(certificate_directory, certificate_file))) for certificate_file in certificate_files]


def output_list_certificates_response(response):
    response_json = json.loads(response.text)
    certificates_log_parts = []
    for i, certificate_entry in enumerate(response_json['certificates']):
        certificate_name = certificate_entry["name"]
        certificate = certificate_entry['certificate']
        certificates_log_parts.append(f'{certificate_name}:\n')
        certificates_log_parts.append(certificate)
    certificates_log = '\n'.join(certificates_log_parts)
    LOG.info(f'EO trusted certificates:\n{certificates_log}')


def list_trusted_certificates(eo_api, certificate_list_name):
    logging.info(f'Listing trusted certificates from {certificate_list_name} list')
    response = eo_api.list_trusted_certificates(certificate_list_name)
    if response.status_code == http.HTTPStatus.OK:
        output_list_certificates_response(response)
    elif response.status_code == http.HTTPStatus.NOT_FOUND:
        raise Exception(f'Trusted certificates list {certificate_list_name} not found, '
                        'make sure you have installed trusted certificates before attempting to list them')
    else:
        raise Exception(f'Exception received when listing trusted certificates in {certificate_list_name} '
                        f'list: {response.text}, status code: {response.status_code}')


def install_trusted_certificates(eo_api, certificate_directory,
                                 certificate_list_name, filename_filter):
    logging.info(f'Installing trusted certificates to {certificate_list_name} list')
    certificates = load_certificates(certificate_directory, filename_filter)
    response = eo_api.install_trusted_certificates(certificate_list_name, certificates)
    if response.status_code == http.HTTPStatus.OK:
        if certificates.__len__().__eq__(0):
            LOG.info(f'Successfully removed certificates in {certificate_list_name} list')
        else:
            LOG.info(f'Successfully updated certificates in {certificate_list_name} list, '
                     f'response message: {response.text}')
    elif response.status_code == http.HTTPStatus.CREATED:
        LOG.info(f'Successfully installed certificates to {certificate_list_name} list, '
                 f'response message: {response.text}')
    elif response.status_code == http.HTTPStatus.BAD_REQUEST:
        raise Exception(f'Received bad request error when installing certificates with message {response.text}')
    else:
        raise Exception(
            f'Exception received when installing trusted certificates in {certificate_list_name} list: '
            f'{response.text}, status code: {response.status_code}')


def process_arguments(arguments):
    if arguments.command is None:
        raise Exception(f'Please specify command, supported commands are: '
                        f'{LIST_CERTIFICATES_COMMAND}, {INSTALL_CERTIFICATES_COMMAND} '
                        f'{LIST_EO_CERTIFICATES_COMMAND}, {INSTALL_EO_CERTIFICATES_COMMAND}')

    host = arguments.host or load_eo_values()
    login = arguments.login or load_eo_creds().get(USERNAME)
    password = arguments.password or load_eo_creds().get(PASSWORD)

    eo_api = EoApi(host, login, password)
    if arguments.command == LIST_CERTIFICATES_COMMAND:
        list_trusted_certificates(eo_api, TRUSTED_CERTIFICATES_LIST_NAME)
    elif arguments.command == INSTALL_CERTIFICATES_COMMAND:
        install_trusted_certificates(eo_api, TRUSTED_CERTIFICATES_DIRECTORY,
                                     TRUSTED_CERTIFICATES_LIST_NAME, TRUSTED_CERTIFICATES_PREDICATE)
    elif arguments.command == LIST_EO_CERTIFICATES_COMMAND:
        list_trusted_certificates(eo_api, EO_CERTIFICATES_LIST_NAME)
    elif arguments.command == INSTALL_EO_CERTIFICATES_COMMAND:
        install_trusted_certificates(eo_api, EO_CERTIFICATES_DIRECTORY,
                                     EO_CERTIFICATES_LIST_NAME, EO_CERTIFICATES_PREDICATE)
    else:
        raise Exception(f'Unknown command {arguments.command}, supported commands are: '
                        f'{LIST_CERTIFICATES_COMMAND}, {INSTALL_CERTIFICATES_COMMAND}'
                        f'{LIST_EO_CERTIFICATES_COMMAND}, {INSTALL_EO_CERTIFICATES_COMMAND}')


def add_login_password_arguments(parser):
    parser.add_argument(
        '--login',
        type=str,
        help=argparse.SUPPRESS,
        default=None,
        required=False
    )
    parser.add_argument(
        '--password',
        type=str,
        help=argparse.SUPPRESS,
        default=None,
        required=False
    )
    parser.add_argument(
        "--host",
        type=str,
        help=argparse.SUPPRESS,
        default=None,
        required=False
    )


def parse_arguments():
    parser = argparse.ArgumentParser()
    subparsers = parser.add_subparsers(dest=SUBPARSER_NAME)

    list_certificates_parse = subparsers.add_parser(
        LIST_CERTIFICATES_COMMAND,
        help=f'Output list of certificates currently installed in {TRUSTED_CERTIFICATES_LIST_NAME} certificates list'
    )
    install_certificates_parser = subparsers.add_parser(
        INSTALL_CERTIFICATES_COMMAND,
        help=f'Install certificates from {TRUSTED_CERTIFICATES_DIRECTORY} '
             f'directory in {TRUSTED_CERTIFICATES_LIST_NAME} certificates list'
    )
    list_eo_certificates_parser = subparsers.add_parser(
        LIST_EO_CERTIFICATES_COMMAND,
        help=f'Output list of certificates currently installed in {EO_CERTIFICATES_LIST_NAME} certificates list'
    )
    install_eo_certificates_parser = subparsers.add_parser(
        INSTALL_EO_CERTIFICATES_COMMAND,
        help=f'Install certificates from {EO_CERTIFICATES_DIRECTORY} '
             f'directory in {EO_CERTIFICATES_LIST_NAME} certificates list'
        )
    add_login_password_arguments(list_certificates_parse)
    add_login_password_arguments(install_certificates_parser)
    add_login_password_arguments(list_eo_certificates_parser)
    add_login_password_arguments(install_eo_certificates_parser)

    return parser.parse_args()


def main():
    arguments = parse_arguments()
    process_arguments(arguments)


if __name__ == '__main__':
    main()
