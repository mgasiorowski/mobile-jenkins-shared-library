#!/usr/bin/env groovy

/**
 * Created by Maciej Gasiorowski on 17/07/2017.
 *
 * Upload binary to beta distribution
 *
 * nodeLabel - label where repository will be checkout
 * gradleTasksDebug - gradle tasks to build binary for debug
 * gradleTasksRelease - gradle tasks to build binary for release
 * buildSuffixName - optional suffix for build
 * isReactNative - optional parameter to mark build as react native project
 * rootBuildScript - optional argument to set root build script
 * stageSuffix - optional suffix for stage name
 * stashApk - optional apk files to stash, to use them later, (Ant-style include patterns - https://ant.apache.org/manual/dirtasks.html#patterns)
 * useGradleCache - optional argument to turn on/off gradle cache, default true
 * useBuildCache - optional argument to turn on/off build cache, default true
 *
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

    timeout(60) {
        node("${config.nodeLabel}") {
            def utils = new Utilities(steps)
            def androidUtils = new AndroidUtilities(steps)
            def reactNativeUtils = new ReactNativeUtilities(steps)
            def gradleTasks = androidUtils.getGradleTasks(env.BUILD_TYPE, config.gradleTasksRelease, config.gradleTasksDebug)
            def defaultGradleOptions = androidUtils.setDefaultGradleOptions()
            def defaultGradleTasks = androidUtils.setDefaultGradleTasks()
            def buildWorkspace = utils.getBuildWorkspace(config.isReactNative, "android", env.WORKSPACE, config.rootBuildScript)

            stage("${utils.getStageSuffix(config.stageSuffix)}Beta upload") {
                deleteDir()
                unstash "workspace"
                reactNativeUtils.unstashNpmCache()
                dir(buildWorkspace) {
                    withEnv(["GRADLE_USER_HOME=${env.WORKSPACE}/.gradle"]) {
                        androidUtils.unstashGradleCache(config.useGradleCache)
                        androidUtils.setAndroidBuildCache(env.WORKSPACE)
                        androidUtils.ustashAndroidBuildCache(config.useBuildCache)
                        def branch = utils.getBranchName(scm, env.BRANCH_SELECTOR)
                        sh "chmod +x gradlew"
                        if (config.buildSuffixName) {
                            sh """#!/bin/bash -xe
                              ./gradlew ${defaultGradleOptions} -PversionCode=${env.BUILD_NUMBER} ${defaultGradleTasks} \
                                ${gradleTasks} -PbuildTypeSuffix='-${config.buildSuffixName}'
                           """
                        } else if (branch == "development") {
                            sh """#!/bin/bash -xe
                              ./gradlew ${defaultGradleOptions} -PversionCode=${env.BUILD_NUMBER} ${defaultGradleTasks} \
                                ${gradleTasks} -PbuildTypeSuffix='-dev'
                           """
                        } else if (config.buildSuffixName == null && branch != "development" && branch != "master") {
                            sh """#!/bin/bash -xe
                              ./gradlew ${defaultGradleOptions} -PversionCode=${env.BUILD_NUMBER} ${defaultGradleTasks} \
                                ${gradleTasks} -PbuildTypeSuffix='-${branch}'
                           """
                        } else {
                            sh """#!/bin/bash -xe
                              ./gradlew ${defaultGradleOptions} -PversionCode=${env.BUILD_NUMBER} ${defaultGradleTasks} ${gradleTasks}
                           """
                        }
                    }
                }

                androidUtils.stashGradleCache()
                androidUtils.stashAndroidBuildCache()
                stash name: "apkFiles", includes: androidUtils.getApkWildcard(config.stashApk)
                androidUtils.archieveGradleProfileReport("beta-upload")
                archiveArtifacts "**/*.apk"
            }
        }
    }
}
