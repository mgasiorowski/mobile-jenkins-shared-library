#!/usr/bin/env groovy

/**
 * Created by Maciej Gasiorowski on 24/08/2017.
 *
 * nodeLabel - label where to do static analysis
 * stageSuffix - suffix for stage (tests type)
 * fastlaneLane - fastlane lane to execute
 * junitTestReportFile - optional file name with unit tests reports
 * useWiremock - optional argument to use wiremock (default false)
 * wiremockVersion - optional argument to set wiremock version to use (default is used version on nodes)
 * wiremockPort - optional argument to set wiremock port to use (default 8080)
 *
 */

import io.jenkins.mobilePipeline.IosUtilities
import io.jenkins.mobilePipeline.Utilities
import io.jenkins.mobilePipeline.ReactNativeUtilities

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    timeout(60) {
        node("${config.nodeLabel}") {
            def iosUtils = new IosUtilities(steps)
            def utils = new Utilities(steps)
            def reactNativeUtils = new ReactNativeUtilities(steps)
            def junitTestReportFile = iosUtils.getJunitTestReportFile(config.junitTestReportFile)
            def buildWorkspace = utils.getBuildWorkspace(config.isReactNative, "ios", env.WORKSPACE)

            stage("${utils.getStageSuffix(config.stageSuffix)}Tests") {
                deleteDir()
                unstash "workspace"
                reactNativeUtils.unstashNpmCache()
                utils.runWiremock(config.useWiremock, env.WORKSPACE, config.wiremockVersion, config.wiremockPort)
                dir(buildWorkspace) {
                    withCredentials([string(credentialsId: "FASTLANE_PASSWORD", variable: "FASTLANE_PASSWORD"), string(credentialsId: "MATCH_PASSWORD", variable: "MATCH_PASSWORD")]) {
                        wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'VGA']) {
                            try {
                                sh """#!/bin/zsh
                                 ${iosUtils.addZshrcConfigFileToShell()}
                                 ${iosUtils.setFastlaneXcodeListTimeout()}
                                 ${iosUtils.installProjectEnvironmentRequirements()}
                                 ${iosUtils.runFastlane(env.FASTLANE_PASSWORD, config.fastlaneLane)}
                              """
                            } catch (exception) {
                                utils.handleException(exception)
                            } finally {
                                junit allowEmptyResults: true, testResults: junitTestReportFile
                                utils.shutdownWiremock(config.useWiremock, config.wiremockPort)
                            }
                        }
                    }
                }
            }
        }
    }
}
