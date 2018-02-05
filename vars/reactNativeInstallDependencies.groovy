#!/usr/bin/env groovy

/**
 * Created by Maciej Gasiorowski on 29/08/2017.
 *
 * nodeLabel - label where install dependencies
 * dependencyInstallationCommand - dependency installation command
 * filesToStash - optional files to stash, to use them later, (Ant-style include patterns -
 *                                                                 https://ant.apache.org/manual/dirtasks.html#patterns)
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
            stage("Install Dependencies") {
                deleteDir()
                unstash "workspace"
                wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'VGA']) {
                    sh """#!/bin/bash -xe
                                 ${config.dependencyInstallationCommand}
                              """
                }

                stash name: "workspace", includes: filesToStash, excludes: "**/node_modules/**", useDefaultExcludes: false
                reactNativeUtils.stashNpmCache()

            }
        }
    }
}
