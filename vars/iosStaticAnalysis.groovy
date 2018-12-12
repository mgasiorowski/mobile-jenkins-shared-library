#!/usr/bin/env groovy

/**
 * Created by Maciej Gasiorowski on 23/08/2017.
 *
 * Static analysis for ios
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

            stage("${utils.getStageSuffix(config.stageSuffix)}Static analysis") {
                deleteDir()
                unstash "workspace"
                reactNativeUtils.unstashNpmCache()
                dir(buildWorkspace) {
                    iosUtils.ustashRubyBuildCache(config.useRubyCache)
                    iosUtils.ustashIosBuildCache(config.useBuildCache)
                    staticAnalysis.checkIfExistsThresholdsPropertiesFile(statusThresholdsPropertiesFile)
                    withCredentials([string(credentialsId: "FASTLANE_PASSWORD", variable: "FASTLANE_PASSWORD"), string(credentialsId: "MATCH_PASSWORD", variable: "MATCH_PASSWORD")]) {
                        wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'VGA']) {
                            sh """#!/bin/zsh
                                 ${iosUtils.addZshrcConfigFileToShell()}
                                 ${iosUtils.setFastlaneXcodeListTimout()}
                                 ${iosUtils.installProjectEnvironmentRequirements()}
                                 ${iosUtils.runFastlane(env.FASTLANE_PASSWORD, config.fastlaneLane)}
                              """
                        }
                    }

                    iosUtils.stashIosBuildCache()
                    iosUtils.stashRubyBuildCache()

                    step([$class                 : "WarningsPublisher",
                          canResolveRelativePaths: false,
                          parserConfigurations   : [[parserName: "Clang (LLVM based)", pattern: "**/logs/xcodebuild.log"]],
                          unstableTotalAll       : staticAnalysis.getAndroidStatusThresholdsPropValues("clangUnstableTotalAll", statusThresholdsPropertiesFile),
                          unstableTotalHigh      : staticAnalysis.getAndroidStatusThresholdsPropValues("clangUnstableTotalHigh", statusThresholdsPropertiesFile),
                          unstableTotalNormal    : staticAnalysis.getAndroidStatusThresholdsPropValues("clangUnstableTotalNormal", statusThresholdsPropertiesFile),
                          failedTotalAll         : staticAnalysis.getAndroidStatusThresholdsPropValues("clangFailedTotalAll", statusThresholdsPropertiesFile),
                          failedTotalHigh        : staticAnalysis.getAndroidStatusThresholdsPropValues("clangFailedTotalHigh", statusThresholdsPropertiesFile),
                          failedTotalNormal      : staticAnalysis.getAndroidStatusThresholdsPropValues("clangFailedTotalNormal", statusThresholdsPropertiesFile)
                    ])

                    iosUtils.addXcodebuildLogToArtifacts(env.STAGE_NAME)

                    if (currentBuild.result == "FAILURE") {
                        error "Static analysis failed - exceeded threshold"
                    }
                }
            }
        }
    }
}
