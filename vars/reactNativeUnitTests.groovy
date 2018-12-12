#!/usr/bin/env groovy

/**
 * Created by Maciej Gasiorowski on 30/08/2017.
 *
 * nodeLabel - label where install dependencies
 * unitTestsCommand - command to run unit tests
 *
 */

import io.jenkins.mobilePipeline.ReactNativeUtilities

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def reactNativeUtils = new ReactNativeUtilities(steps)

    timeout(60) {
        node("${config.nodeLabel}") {
            stage("Unit Tests") {
                deleteDir()
                unstash "workspace"
                reactNativeUtils.unstashNpmCache()
                wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'VGA']) {
                    sh """#!/bin/bash -xe
                                 ${config.unitTestsCommand}
                              """
                }

            }
        }
    }
}
