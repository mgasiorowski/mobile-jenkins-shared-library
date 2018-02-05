#!/usr/bin/env groovy

/**
 * Created by Maciej Gasiorowski on 26/07/2017.
 *
 * Trust custom CA for debugging with charles, wireshark etc. on Android < 7
 * https://android-developers.googleblog.com/2016/07/changes-to-trusted-certificate.html
 * https://github.com/levyitay/AddSecurityExceptionAndroid
 *
 * nodeLabel - label which node will be used
 * apkName - apk name to add trust ca
 *
 */

import io.jenkins.mobilePipeline.AndroidUtilities
import io.jenkins.mobilePipeline.Utilities

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def androidUtils = new AndroidUtilities(steps)
    def utils = new Utilities(steps)

    timeout(60) {
        node("${config.nodeLabel}") {
            stage("Add trust CA to apk") {
                deleteDir()
                unstash "apkFiles"
                def apkFilePath = utils.getFilePath(androidUtils.getApkWildcard(config.apkName))
                dir('AddSecurityExceptionAndroid') {
                    git changelog: false, poll: false, url: 'https://github.com/mgasiorowski/AddSecurityExceptionAndroid.git'
                }
                sh """#!/bin/bash -xe
                      sh AddSecurityExceptionAndroid/addSecurityExceptions.sh ${apkFilePath}
                   """
                archiveArtifacts "**/*_ssl.apk"
            }
        }
    }
}
