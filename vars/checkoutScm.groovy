#!/usr/bin/env groovy

/**
 * Created by Maciej Gasiorowski on 06/07/2017.
 *
 * Checkout scm
 *
 * nodeLabel - label where repository will be checkout
 * filesToStash - optional files to stash, to use them later, (Ant-style include patterns -
 *                                                                 https://ant.apache.org/manual/dirtasks.html#patterns)
 */

import io.jenkins.mobilePipeline.Utilities

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def utils = new Utilities(steps)
    def filesToStash = utils.getFilesToStash(config.filesToStash)

    timeout(10) {
        node("${config.nodeLabel}") {
            stage("Checkout scm") {
                deleteDir()
                checkout scm
                sh "git submodule update --init"

                stash name: "workspace", includes: filesToStash, useDefaultExcludes: false
            }
        }
    }
}
