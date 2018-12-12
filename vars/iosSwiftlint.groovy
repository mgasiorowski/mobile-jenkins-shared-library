#!/usr/bin/env groovy

/**
 * Created by Maciej Gasiorowski on 25/08/2017.
 *
 * nodeLabel - label where static analysis will be run
 * rootBuildScript - optional argument to set root build script
 * fastlaneLane - fastlane lane to execute
 * statusThresholdsPropertiesFile - path to status threashold properties file
 * useBuildCache - optional argument to turn on/off build cache, default true
 * useRubyCache - optional argument to turn on/off ruby build cache, default true
 *
 */

import io.jenkins.mobilePipeline.IosUtilities
import io.jenkins.mobilePipeline.StaticAnalysisUtilities
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
            def utils = new Utilities(steps)
            def iosUtils = new IosUtilities(steps)
            def staticAnalysis = new StaticAnalysisUtilities(steps)
            def reactNativeUtils = new ReactNativeUtilities(steps)
            def buildWorkspace = utils.getBuildWorkspace(config.isReactNative, "ios", env.WORKSPACE, config.rootBuildScript)
            def statusThresholdsPropertiesFile = staticAnalysis.getstatusThresholdsPropertiesFilePath(config.statusThresholdsPropertiesFile)
            def junitTestReportFile = iosUtils.getJunitTestReportFile(config.junitTestReportFile)

            stage("${utils.getStageSuffix(config.stageSuffix)}Swiftlint") {
                deleteDir()
                unstash "workspace"
                reactNativeUtils.unstashNpmCache()
                dir(buildWorkspace) {
                    iosUtils.ustashRubyBuildCache(config.useRubyCache)
                    iosUtils.ustashIosBuildCache(config.useBuildCache)
                    withCredentials([string(credentialsId: "FASTLANE_PASSWORD", variable: "FASTLANE_PASSWORD"), string(credentialsId: "MATCH_PASSWORD", variable: "MATCH_PASSWORD")]) {
                        wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'VGA']) {
                            sh """#!/bin/zsh
                                 ${iosUtils.addZshrcConfigFileToShell()}
                                 ${iosUtils.setFastlaneXcodeListTimout()}
                                 ${iosUtils.installProjectEnvironmentRequirements()}
                                 ${iosUtils.runFastlane(env.FASTLANE_PASSWORD, config.fastlaneLane)}
                              """
                        }

                        archiveArtifacts artifacts: junitTestReportFile, allowEmptyArchive: true

                        iosUtils.stashRubyBuildCache()

                        step([$class             : "CheckStylePublisher",
                              pattern            : junitTestReportFile,
                              unstableTotalAll   : staticAnalysis.getAndroidStatusThresholdsPropValues("swiftlintUnstableTotalAll", statusThresholdsPropertiesFile),
                              unstableTotalHigh  : staticAnalysis.getAndroidStatusThresholdsPropValues("swiftlintUnstableTotalHigh", statusThresholdsPropertiesFile),
                              unstableTotalNormal: staticAnalysis.getAndroidStatusThresholdsPropValues("swiftlintUnstableTotalNormal", statusThresholdsPropertiesFile),
                              failedTotalAll     : staticAnalysis.getAndroidStatusThresholdsPropValues("swiftlintFailedTotalAll", statusThresholdsPropertiesFile),
                              failedTotalHigh    : staticAnalysis.getAndroidStatusThresholdsPropValues("swiftlintFailedTotalHigh", statusThresholdsPropertiesFile),
                              failedTotalNormal  : staticAnalysis.getAndroidStatusThresholdsPropValues("swiftlintFailedTotalNormal", statusThresholdsPropertiesFile)
                        ])
                    }
                }
            }
        }
    }
}
