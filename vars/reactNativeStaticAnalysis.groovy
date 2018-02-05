#!/usr/bin/env groovy

/**
 * Created by Maciej Gasiorowski on 29/08/2017.
 *
 * nodeLabel - label where install dependencies
 * staticAnalysisCommand - command to run static analysis
 */

import io.jenkins.mobilePipeline.ReactNativeUtilities
import io.jenkins.mobilePipeline.StaticAnalysisUtilities

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    timeout(60) {
        node("${config.nodeLabel}") {
            def reactNativeUtils = new ReactNativeUtilities(steps)
            def staticAnalysis = new StaticAnalysisUtilities(steps)
            def checkstyleReportFile = reactNativeUtils.getCheckstyleReportFile(config.checkstyleReportFile)
            def statusThresholdsPropertiesFile = staticAnalysis.getstatusThresholdsPropertiesFilePath(config.statusThresholdsPropertiesFile)

            stage("Static analysis") {
                deleteDir()
                unstash "workspace"
                reactNativeUtils.unstashNpmCache()
                staticAnalysis.checkIfExistsThresholdsPropertiesFile(statusThresholdsPropertiesFile)
                wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'VGA']) {
                    sh """#!/bin/bash -xe
                                 ${config.staticAnalysisCommand}
                              """
                }

                step([$class: "CheckStylePublisher",
                      pattern: checkstyleReportFile,
                      unstableTotalAll: staticAnalysis.getAndroidStatusThresholdsPropValues("eslintUnstableTotalAll", statusThresholdsPropertiesFile),
                      unstableTotalHigh: staticAnalysis.getAndroidStatusThresholdsPropValues("eslintUnstableTotalHigh", statusThresholdsPropertiesFile),
                      unstableTotalNormal: staticAnalysis.getAndroidStatusThresholdsPropValues("eslintUnstableTotalNormal", statusThresholdsPropertiesFile),
                      failedTotalAll: staticAnalysis.getAndroidStatusThresholdsPropValues("eslintFailedTotalAll", statusThresholdsPropertiesFile),
                      failedTotalHigh: staticAnalysis.getAndroidStatusThresholdsPropValues("eslintFailedTotalHigh", statusThresholdsPropertiesFile),
                      failedTotalNormal: staticAnalysis.getAndroidStatusThresholdsPropValues("eslintFailedTotalNormal", statusThresholdsPropertiesFile)
                ])
            }
        }
    }
}
