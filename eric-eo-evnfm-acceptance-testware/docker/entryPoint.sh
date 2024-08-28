#!/usr/bin/env bash
#
# COPYRIGHT Ericsson 2024
#
#
#
# The copyright to the computer program(s) herein is the property of
#
# Ericsson Inc. The programs may be used and/or copied only with written
#
# permission from Ericsson Inc. or in accordance with the terms and
#
# conditions stipulated in the agreement/contract under which the
#
# program(s) have been supplied.
#


echo "Starting EVNFM Test Execution"

if [ $# -eq 0 ]; then
  echo "No arguments supplied to testware. A single argument representing a json configuration file must be provided"
  exit 1
fi

command_to_run="exec java -jar /acc_tests/eric-eo-evnfm-acceptance-testware.jar $1"

echo "Command to run: ${command_to_run}"

${command_to_run}
