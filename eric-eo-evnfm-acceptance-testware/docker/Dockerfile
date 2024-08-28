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

ARG BASE_IMAGE_VERSION
FROM armdocker.rnd.ericsson.se/proj-am/sles/sles-openjdk17:${BASE_IMAGE_VERSION}
COPY target/eric-eo-evnfm-acceptance-testware.jar /acc_tests/
COPY docker/entryPoint.sh /acc_tests/entryPoint.sh
RUN chmod +x /acc_tests/*.sh
ENTRYPOINT ["/acc_tests/entryPoint.sh"]