#!/usr/bin/env groovy

/**
 * Created by Maciej Gasiorowski on 06/07/2017.
 *
 * Static analysis for android
 *
 * nodeLabel - label where static analysis will be run
 * gradleTasksDebug - gradle tasks to build binary for debug
 * gradleTasksRelease - gradle tasks to build binary for release
 * androidLintResultsFile - optional android lint result file path
 * pmdResultsFile - optional pmd result file path
 * findBugsResultFile - optional findbugs result file path
 * filesToArchieve - optional file to archieve, for ex. report from detekt
 */

import io.jenkins.mobilePipeline.StaticAnalysisUtilities
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
            def androidUtils = new AndroidUtilities(steps)
            def utils = new Utilities(steps)
            def staticAnalysis = new StaticAnalysisUtilities(steps)
            def reactNativeUtils = new ReactNativeUtilities(steps)
            def gradleTasks = androidUtils.getGradleTasks(env.BUILD_TYPE, config.gradleTasksRelease, config.gradleTasksDebug)
            def defaultGradleOptions = androidUtils.setDefaultGradleOptions()
            def statusThresholdsPropertiesFile = staticAnalysis.getstatusThresholdsPropertiesFilePath(config.statusThresholdsPropertiesFile)
            def buildWorkspace = utils.getBuildWorkspace(config.isReactNative, "android", env.WORKSPACE)

            stage("${utils.getStageSuffix(config.stageSuffix)}Static analysis") {
                deleteDir()
                unstash "workspace"
                reactNativeUtils.unstashNpmCache()
                dir(buildWorkspace) {
                    staticAnalysis.checkIfExistsThresholdsPropertiesFile(statusThresholdsPropertiesFile)
                    withEnv(["GRADLE_USER_HOME=${env.WORKSPACE}/.gradle"]) {
                        androidUtils.unstashGradleCache()
                        androidUtils.setAndroidBuildCache(env.WORKSPACE)
                        sh "chmod +x gradlew"
                        sh "./gradlew ${defaultGradleOptions} -PversionCode=${env.BUILD_NUMBER} clean ${gradleTasks}"
                    }

                    parallel(
                            "lint": {
                                if (config.androidLintResultsFile) {
                                    androidLintResultsFile = config.androidLintResultsFile
                                } else {
                                    androidLintResultsFile = "**/lint-results*.xml"
                                }

                                androidLint(canComputeNew: false, pattern: "${androidLintResultsFile}",
                                        unstableTotalHigh: staticAnalysis.getAndroidStatusThresholdsPropValues("lintUnstableTotalHigh", statusThresholdsPropertiesFile),
                                        unstableTotalNormal: staticAnalysis.getAndroidStatusThresholdsPropValues("lintUnstableTotalNormal", statusThresholdsPropertiesFile),
                                        failedTotalHigh: staticAnalysis.getAndroidStatusThresholdsPropValues("lintFailedTotalHigh", statusThresholdsPropertiesFile),
                                        failedTotalNormal: staticAnalysis.getAndroidStatusThresholdsPropValues("lintFailedTotalNormal", statusThresholdsPropertiesFile))
                            },
                            "pmd": {
                                if (config.pmdResultsFile) {
                                    pmdResultsFile = config.pmdResultsFile
                                } else {
                                    pmdResultsFile = "**/pmd*.xml"
                                }

                                step([$class             : "PmdPublisher", pattern: "${pmdResultsFile}",
                                      unstableTotalHigh  : staticAnalysis.getAndroidStatusThresholdsPropValues("pmdUnstableTotalHigh", statusThresholdsPropertiesFile),
                                      unstableTotalNormal: staticAnalysis.getAndroidStatusThresholdsPropValues("pmdUnstableTotalNormal", statusThresholdsPropertiesFile),
                                      failedTotalHigh    : staticAnalysis.getAndroidStatusThresholdsPropValues("pmdFailedTotalHigh", statusThresholdsPropertiesFile),
                                      failedTotalNormal  : staticAnalysis.getAndroidStatusThresholdsPropValues("pmdFailedTotalNormal", statusThresholdsPropertiesFile)])
                            },
                            "findbugs": {
                                if (config.findBugsResultFile) {
                                    findBugsResultFile = config.findBugsResultFile
                                } else {
                                    findBugsResultFile = "**/findbugs*.xml"
                                }

                                step([$class             : "FindBugsPublisher", canComputeNew: false, pattern: "${findBugsResultFile}",
                                      unstableTotalHigh  : staticAnalysis.getAndroidStatusThresholdsPropValues("findbugsUnstableTotalHigh", statusThresholdsPropertiesFile),
                                      unstableTotalNormal: staticAnalysis.getAndroidStatusThresholdsPropValues("findbugsUnstableTotalNormal", statusThresholdsPropertiesFile),
                                      failedTotalHigh    : staticAnalysis.getAndroidStatusThresholdsPropValues("findbugsFailedTotalHigh", statusThresholdsPropertiesFile),
                                      failedTotalNormal  : staticAnalysis.getAndroidStatusThresholdsPropValues("findbugsFailedTotalNormal", statusThresholdsPropertiesFile)])
                            }
                    )

                    androidUtils.stashGradleCache()

                    if (config.filesToArchieve) {
                        filesToArchieve = config.filesToArchieve
                        archiveArtifacts filesToArchieve
                    }

                    androidUtils.archieveGradleProfileReport("static-analysis")

                    if (currentBuild.result == "FAILURE") {
                        error "Static analysis failed - exceeded threshold"
                    }
                }
            }
        }
    }
}