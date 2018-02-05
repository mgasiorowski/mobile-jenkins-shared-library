#!/usr/bin/env groovy

/**
 * Created by Maciej Gasiorowski on 30/08/2017.
 *
 * nodeLabel - label where install dependencies
 * bundleAndroidResourcesCommand - command to bundle android resources
 *
 */

import io.jenkins.mobilePipeline.Utilities
import io.jenkins.mobilePipeline.ReactNativeUtilities

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def utils = new Utilities(steps)
    def reactNativeUtils = new ReactNativeUtilities(steps)
    def filesToStash = utils.getFilesToStash(config.filesToStash)

    timeout(60) {
        node("${config.nodeLabel}") {
            stage("Bundle Android Resources") {
                deleteDir()
                unstash "workspace"
                reactNativeUtils.unstashNpmCache()
                wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'VGA']) {
                    sh """#!/bin/bash -xe
                                 ${config.bundleAndroidResourcesCommand}
                              """
                }

            }
        }
    }
}
