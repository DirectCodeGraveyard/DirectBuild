#!/usr/bin/env bash
#######################################
# A quickstart script for DirectBuild #
#   Created by: Kenneth Endfinger     #
#######################################
# Options #
USE_BINARY="false"
# End Options #

function check_commands() {
    for cmd in git java; do
        if which ${cmd} > /dev/null 2>&1; then
            # Command Found
            echo -n ""
        else
            # Command not found
            echo "ERROR: Unable to find '${cmd}' on this system. Please install it using your package manager and rerun this script."
            exit 1
        fi
    done
}

function check_java_version() {
    if java -version 2>&1 | awk '/version/ {print $3}' | grep '"1\.8\..*"' > /dev/null 2>&1; then
        # Java Version 8
        echo -n ""
    else
        # Not Java Version 8
        echo "ERROR: Java Version 8 is required."
        exit 1
    fi
}

function build() {
    git clone --recursive --branch master --depth 1 git://github.com/DirectBuild/DirectBuild.git _build_
    if [[ ${?} -ne 0 ]]; then
        echo "ERROR: Failed to clone DirectBuild."
        exit 1
    fi
    cd _build_
    ./gradlew jar
    if [[ ${?} -ne 0 ]]; then
        echo "ERROR: Failed to build DirectBuild. Falling back to binary."
        download_binary
    fi
    cp -R build/libs/DirectBuild.jar ../DirectBuild.jar
    cd ..
    echo "Cleaning Up..."
    rm -rf _build_
}

function download_binary() {
    wget https://kaendfinger.ci.cloudbees.com/job/DirectBuild/lastSuccessfulBuild/artifact/build/libs/DirectBuild.jar -ODirectBuild.jar
    if [[ ${?} -ne 0 ]]; then
        echo "ERROR: Failed to download binary."
    fi
    chmod a+x DirectBuild.jar # For Good Measure
}

if [[ ${USE_BINARY} == true ]]; then
    download_binary
else
    echo "Checking system for needed commands..."
    check_commands
    echo "Checking Java Version..."
    check_java_version
    echo "Building DirectBuild..."
    build
fi
echo "You may now start DirectBuild by typing 'java -jar DirectBuild.jar'"
