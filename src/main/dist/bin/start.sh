#!/bin/bash
DIST_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )"/.. && pwd)"
LAUNCH_DIR=""

function launch() {
    [ ! -d ${LAUNCH_DIR} ] && mkdir -p ${LAUNCH_DIR}
    cd ${LAUNCH_DIR}
    java -jar ${DIST_DIR}/lib/SimpleCI.jar
}

function launch_in() {
    LAUNCH_DIR="${@}"
}

function load_config() {
    source ${DIST_DIR}/etc/launch.rc
}

load_config

launch