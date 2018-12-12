#!/usr/bin/env groovy

/**
 * Created by Maciej Gasiorowski on 25/08/2017.
 *
 * nodeLabel - label where to do static analysis
 * rootBuildScript - optional argument to set root build script
 * stageSuffix - suffix for stage (tests type)
 * fastlaneLane - fastlane lane to execute
 * useBuildCache - optional argument to turn on/off build cache, default true
 * useRubyCache - optional argument to turn on/off ruby build cache, default true
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
            def buildWorkspace = utils.getBuildWorkspace(config.isReactNative, "ios", env.WORKSPACE, config.rootBuildScript)

            stage("${utils.getStageSuffix(config.stageSuffix)} Build") {
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
                    }
                    iosUtils.stashIosBuildCache()
                    iosUtils.stashRubyBuildCache()
                    archiveArtifacts artifacts: "**/*.ipa", allowEmptyArchive: true
                    archiveArtifacts artifacts: "**/*.app.dSYM.zip", allowEmptyArchive: true
                    iosUtils.addXcodebuildLogToArtifacts(env.STAGE_NAME)
                    archiveArtifacts artifacts: "**/culprits.txt", allowEmptyArchive: true
                }
            }
        }
    }
}
