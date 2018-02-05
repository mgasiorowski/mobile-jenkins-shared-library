#!/usr/bin/env groovy

/**
 * Created by Maciej Gasiorowski on 06/07/2017.
 *
 * Build android binary
 *
 * Configuration
 *
 * nodeLabel - label where binary will be build
 * gradleTasksDebug - gradle tasks to build binary for debug
 * gradleTasksRelease - gradle tasks to build binary for release
 * stashApk - optional apk files to stash, to use them later, (Ant-style include patterns - https://ant.apache.org/manual/dirtasks.html#patterns)
 */

import io.jenkins.mobilePipeline.AndroidUtilities
import io.jenkins.mobilePipeline.Utilities
import io.jenkins.mobilePipeline.ReactNativeUtilities

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def androidUtils = new AndroidUtilities(steps)
    def utils = new Utilities(steps)
    def reactNativeUtils = new ReactNativeUtilities(steps)
    def gradleTasks = androidUtils.getGradleTasks(env.BUILD_TYPE, config.gradleTasksRelease, config.gradleTasksDebug)
    def defaultGradleOptions = androidUtils.setDefaultGradleOptions()

    timeout(60) {
        node("${config.nodeLabel}") {
            def buildWorkspace = utils.getBuildWorkspace(config.isReactNative, "android", env.WORKSPACE)

            stage("${utils.getStageSuffix(config.stageSuffix)}Build binary") {
                deleteDir()
                unstash "workspace"
                reactNativeUtils.unstashNpmCache()
                dir(buildWorkspace) {
                    withEnv(["GRADLE_USER_HOME=${env.WORKSPACE}/.gradle"]) {
                        androidUtils.unstashGradleCache()
                        androidUtils.setAndroidBuildCache(env.WORKSPACE)
                        sh "chmod +x gradlew"
                        sh """#!/bin/bash -xe 
                          ./gradlew ${defaultGradleOptions} -PversionCode=${env.BUILD_NUMBER} clean ${gradleTasks}
                       """
                    }

                    androidUtils.stashGradleCache()
                    stash name: "apkFiles", includes: androidUtils.getApkWildcard(config.stashApk)
                    archiveArtifacts "**/*.apk"
                    androidUtils.archieveGradleProfileReport("build")
                }
            }
        }
    }
}
