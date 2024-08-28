#!/usr/bin/env python

import os
from pydoc import resolve
from urllib import response
import requests
import argparse
import urllib3
from requests.auth import HTTPBasicAuth
import json
import logging
import sys
import subprocess
import re
from datetime import datetime
import logging
from enum import Enum

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

class Pod(Enum):
  REGISTRY = ({'app': 'eric-lcm-container-registry'})
  POSTGRES = ({'app': 'application-manager-postgres'})

auth = None
#HTTPBasicAuth('<USERNAME>', '<PASSWORD>')
PWD = os.getcwd()
DATE_TIME = datetime.now().strftime("%Y%m%d%H%M%S")
LOG_FILE = f'log_{DATE_TIME}.log'
SELECT_IMAGE_INUSE = '''SELECT ap.provider, ap.product_name, ap.software_version, ap.usage_state, adi.image_id
FROM app_docker_images adi JOIN app_packages ap ON adi.descriptor_id = ap.descriptor_id
ORDER BY CASE WHEN ap.usage_state = 'IN_USE' THEN 0 ELSE 1 END;'''

class DockerREST:
  def __init__(self, base_url, user, password):
    self.base_url = base_url
    self.session = requests.Session()
    if user is not None:
      self.session.auth = (user, password)
    self.session.headers.update(
      {
        "Content-type": "application/json",
        "Accept": "application/vnd.docker.distribution.manifest.v2+json"
      }
    )

  def request(self, endpoint, method='GET'):
    logging.debug("request: endpoint=%s method=%s", endpoint, method)
    response = self.session.request(
      method=method,
      url= self.base_url + endpoint,
      verify=False
    )
    logging.debug("request: response.status_code=%d response.headers=%s", response.status_code, response.headers)
    if len(response.content) > 0:
      logging.debug("request: response content: %s", json.dumps(response.json(), indent=4))
    return response

class Kubectl:
  def __init__(self, kubectl_path, kubeconfig_path, namespace, pod):
    self.kubectl_path = kubectl_path
    self.kubeconfig_path = kubeconfig_path
    self.namespace = namespace
    self.app = pod.value['app']
  def get_registry_pod(self):
    printout = self.exec(["get", "pods", "-l" "app={0}".format(self.app), "--no-headers=true"])
    if len(printout) == 0:
      logging.error("Cannot locate registry pod, empty printout")
      sys.exit(1)
    lines = printout.splitlines()
    if len(lines) != 1:
      logging.error("Cannot locate registry pod, unexpected number of lines")
      sys.exit(1)
    parts = re.split("\s+", lines[0])
    logging.debug("get_registry_pod: parts=%s", parts)
    self.pod = parts[0]
    logging.debug("get_registry_pod: pod=%s", self.pod)

  def exec_in_registry(self, cmd):
    return self.exec(['exec', '-it', self.pod, '--'] + cmd)

  def exec(self, cmd):
    base_args = [
      self.kubectl_path,
      '--namespace={0}'.format(self.namespace),
      '--kubeconfig={0}'.format(self.kubeconfig_path),
    ]
    full_args = base_args + cmd
    logging.info(" ".join(full_args))
    kubectl_exec  = subprocess.run(full_args, stdout=subprocess.PIPE, stdin=subprocess.DEVNULL, universal_newlines=True)
    logging.info(kubectl_exec.stdout)
    logging.debug("exec result=%d", kubectl_exec.returncode)
    if kubectl_exec.returncode != 0:
      logging.error("kubectl command failed return code=%d", kubectl_exec.returncode)
      sys.exit(1)
    return kubectl_exec.stdout

def check_tag(docker, name, tag, problems):
  logging.info(" tag=%s", tag)
  manifest_response = docker.request("/v2/{0}/manifests/{1}".format(name, tag))
  if not manifest_response.ok:
    problem_type = 'manifest'
    error_msg = ''
    if 'application/json' in manifest_response.headers.get('Content-Type'):
      error_msg = manifest_response.json()['errors'][0]['message']
      if 'but accept header does not support' in error_msg:
        problem_type = 'manifest_format'
      elif 'Docker-Content-Digest' not in manifest_response.headers:
        problem_type = 'digest_absent'
    logging.warning("  manifest request failed %d %s", manifest_response.status_code, error_msg)
    problems.append({'type': problem_type, 'repository': name, 'tag': tag})
    return

  manifest = manifest_response.json()
  layers_okay = True
  logging.debug("check_tag: manifest=%s", manifest)
  logging.info("  #layers=%d", len(manifest['layers']))
  for layer in manifest['layers']:
    layer_digest = layer['digest']
    logging.debug("  layer digest=%s", layer_digest)
    response = docker.request(
      "/v2/{0}/blobs/{1}".format(name, layer_digest),
      'HEAD'
    )
    if not response.ok:
      logging.error("%s:%s invalid layer %s", name, tag, layer_digest)
      layers_okay = False

  if not layers_okay:
    problems.append({'type': 'layer', 'repository': name, 'tag': tag})

def check_repository(docker, repository, problems):
  logging.info("repository name=%s", repository)
  response = docker.request("/v2/{0}/tags/list".format(repository))
  if not response.ok:
    logging.error("tags not found for %s", repository)
    problems.append({'type': 'notfound', 'repository': repository})
    return

  tags = response.json()['tags']
  if tags is None:
    logging.warning("null tags for %s", repository)
    problems.append({'type': 'nulltags', 'repository': repository})
    return

  for tag in tags:
    check_tag(docker, repository, tag, problems)

def get_with_pagination(docker, request, key):
  results = []
  while request is not None:
    response = docker.request(request)
    if not response.ok:
      logging.error("Request failed %s : %d", request, response.status_code)
      return None
    results.extend(response.json()[key])
    if 'Link' in response.headers:
      request = re.search("^<(\S+)>", response.headers['Link']).group(1)
    else:
      request = None
  return results


def check(docker, fix, kubectl):
  repositories = get_with_pagination(docker, '/v2/_catalog', 'repositories')
  if repositories is None:
    return False
  logging.info("#repositories: %d", len(repositories))

  problems = []
  for repository in repositories:
    check_repository(docker, repository, problems)

  if len(problems) > 0:
    logging.warning("Problems detected: %d", len(problems))
    for problem in problems:
      logging.warning(" %s", problem)
      if fix:
        if problem['type'] == 'nulltags' or problem['type'] == 'notfound':
          from_dir = "/var/lib/registry/docker/registry/v2/repositories/{0}".format(problem['repository'])
          # The repository might have / in it, for the mv to work, we need to
          # replace that with _
          to_dir = "/var/lib/registry/{0}.invalid_repo.recoverable.{1}".format(
            problem['repository'].replace("/", "__"),
            DATE_TIME
          )
          logging.warning("  Check folder %s", from_dir)
          is_path_exist = kubectl.exec_in_registry(['/usr/bin/sh',
                                                    '-c',
                                                    'if /usr/bin/test -d {0}; then echo exist; fi'.format(from_dir)])
          if is_path_exist:
            logging.warning("  Moving %s to %s", from_dir, to_dir)
            kubectl.exec_in_registry([
              "/usr/bin/mv",
              from_dir,
              to_dir
            ])
          else:
            logging.warning("  The folder %s does not exist.", from_dir)
        elif problem['type'] == 'layer' or problem['type'] == 'manifest' or problem['type'] == 'digest_absent':
          image = "{0}:{1}".format(problem['repository'], problem['tag'])
          logging.warning("  Removing image tag %s", image)
          rmtag(docker, image)
        else:
          logging.warning("  No fix implemented")


    return False

  return True

def move_image(name, tag):
  from_dir = "/var/lib/registry/docker/registry/v2/repositories/{0}/_manifests/tags/{1}" \
    .format(name, tag)

  to_dir = "/var/lib/registry/{0}.tag_{1}.invalid_image.recoverable.{2}".format(
    name.replace("/", "__"),
    tag,
    DATE_TIME
  )

  logging.warning("  Check folder %s", from_dir)
  is_path_exist = kubectl.exec_in_registry(['/usr/bin/sh',
                                            '-c',
                                            'if /usr/bin/test -d {0}; then echo exist; fi'.format(from_dir)])

  if is_path_exist:
    logging.warning("  Moving %s to %s", from_dir, to_dir)
    kubectl.exec_in_registry([
      "/usr/bin/mv",
      from_dir,
      to_dir
    ])
  else:
    logging.warning("  The folder %s does not exist.", from_dir)

def rmtag(docker, image):
  (name,tag) = image.split(":")

  response = docker.request(
    "/v2/{0}/manifests/{1}".format(name, tag),
    'HEAD'
  )
  if not response.ok or 'Docker-Content-Digest' not in response.headers:
    move_image(name, tag)
    return

  tag_digest = response.headers['Docker-Content-Digest']

  response = docker.request(
    "/v2/{0}/manifests/{1}".format(name, tag_digest),
    'DELETE',
  )
  if not response.ok:
    print(response.status_code)
    print(json.dumps(response.json(), indent=4))

def marked_as_invalid(kubectl):
  ls_result = kubectl.exec_in_registry(['/usr/bin/ls', '/var/lib/registry'])
  registry_folders = ls_result.split()
  invalid_folders = [s for s in registry_folders if "invalid" in s]

  return invalid_folders

def check_image_in_use(kubectl):
  repositories = get_with_pagination(docker, '/v2/_catalog', 'repositories')
  if repositories is None:
    return False

  problems = []
  for repository in repositories:
    check_repository(docker, repository, problems)

  psql_result = kubectl.exec_in_registry(['/usr/bin/psql', '-U', 'postgres', '-d', 'onboarding', '--csv', '--no-align', '-t', '-c', SELECT_IMAGE_INUSE])
  psql_result_in_array = [line for line in psql_result.split('\n') if line]

  images_in_use = []
  for columns_in_string in psql_result_in_array:
    columns_in_array = columns_in_string.split('|')
    images_in_use.append({'package_name': ".".join(columns_in_array[:3]), 'state': columns_in_array[3], 'image': columns_in_array[4]})

  find_by_repo = lambda image_in_use, problem: image_in_use["image"].split(":")[0] == problem["repository"]
  find_by_repo_tag = lambda image_in_use, problem: image_in_use["image"] == problem["repository"] + ":" + problem["tag"]

  problem_images_in_use = []
  for problem in problems:
    if problem['type'] == 'nulltags' or problem['type'] == 'notfound':
      matcher = find_by_repo
    elif problem['type'] == 'layer' or problem['type'] == 'manifest' or problem['type'] == 'digest_absent':
      matcher = find_by_repo_tag
    else:
      logging.warning("Unknown problem type for\n {0}".format(problem))
      continue

    problem_images_in_use.extend([image_in_use for image_in_use in images_in_use if matcher(image_in_use, problem)])

  logging.warning("Found states for images: %d", len(problem_images_in_use))
  if (problem_images_in_use):
    for p in problem_images_in_use:
      logging.warning(" %s", p)

def recovery(kubectl, folders):
  logging.info("input invalid folders: {0}".format(folders))
  invalid_folders = marked_as_invalid(kubectl)
  for folder in folders:
    if folder not in invalid_folders:
      logging.error("{0} folder missing".format(folder))
      sys.exit(0)

    from_dir = "/var/lib/registry/{0}/".format(folder)

    if "invalid_image" in folder:
      pattern = r"^(.*?)\.tag_(.*?)\.invalid"
      matches = re.search(pattern, folder)
      repo_name = matches.group(1).replace("__", "/")
      tag_number = matches.group(2)
      to_dir = "/var/lib/registry/docker/registry/v2/repositories/{0}/_manifests/tags/{1}" \
        .format(repo_name, tag_number)
    elif "invalid_repo" in folder:
      repo_name = folder.split('.invalid')[0].replace("__", "/")
      to_dir = "/var/lib/registry/docker/registry/v2/repositories/{0}".format(repo_name)

    logging.warning(" Recovery from %s to %s", from_dir, to_dir)
    kubectl.exec_in_registry([
      "/usr/bin/mv",
      from_dir,
      to_dir])

parser = argparse.ArgumentParser(description='Consistency Check for Docker Registry')
parser.add_argument('--url', help='Registry URL', required=True)
parser.add_argument('--user', help='user')
parser.add_argument('--password', help='password')
parser.add_argument('--action', help='Action to perform', choices=['check', 'rmtag', 'recovery', 'inuse'], required=True)
parser.add_argument('--image', help='Image')
parser.add_argument('--fix', help='Fix issues found', action="store_true")
parser.add_argument('--kubectl', help='Path to kubectl binary')
parser.add_argument('--kubeconfig', help='Path to kube config file')
parser.add_argument('--namespace', help='Namespace of registry pod')
parser.add_argument('--file', help='Redirect output to file', action="store_true")

#recovery
parser.add_argument('--folder', help='Invalid folder list')
parser.add_argument('--folders', help='Invalid folder list')

parser.add_argument('--debug', help='debug logging', action="store_true")
args = parser.parse_args()

logging_level = logging.INFO
if args.debug:
  logging_level = logging.DEBUG

docker = DockerREST(args.url, args.user, args.password)

kubectl = None
if args.file:
  logging.basicConfig(filename=LOG_FILE, filemode="a",
                      level=logging_level, format="[%(asctime)s] [%(levelname)s]: %(message)s")
else:
  logging.basicConfig(level=logging_level)

if args.fix:
  kubectl = Kubectl(args.kubectl, args.kubeconfig, args.namespace, Pod.REGISTRY)
  kubectl.get_registry_pod()

if args.action == 'check':
  okay = check(docker, args.fix, kubectl)
  if okay:
    sys.exit(0)
  else:
    sys.exit(1)

if args.action == 'inuse':
  kubectl = Kubectl(args.kubectl, args.kubeconfig, args.namespace, Pod.POSTGRES)
  kubectl.get_registry_pod()
  check_image_in_use(kubectl)

if args.action == 'recovery':
  if args.folder and args.folders:
    logging.warning("Can be only 'folder' or 'folders'")
    sys.exit(1)
  elif not args.folder and not args.folders:
    logging.warning("Folders list is empty")
    sys.exit(1)
  elif args.folder:
    folder_list = [args.folder]
  else:
    with open(args.folders, "r") as file:
      folder_list = file.read().splitlines()
  kubectl = Kubectl(args.kubectl, args.kubeconfig, args.namespace, Pod.REGISTRY)
  kubectl.get_registry_pod()
  recovery(kubectl, folder_list)

elif args.action == 'rmtag':
  rmtag(docker, args.image)
