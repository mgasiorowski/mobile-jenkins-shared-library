#!/usr/bin/env groovy

/**
 * Created by Maciej Gasiorowski on 12/07/2017.
 *
 * Run android monkey tests
 *
 * Configuration
 *
 * nodeLabel - label where binary will be build
 * apkName - name of apk to test
 * packageName - package name value of app for tests
 * eventCount - number of events
 * seedValue - Seed value for pseudo-random number generator. If you re-run the Monkey with the same seed value,
 *             it will generate the same sequence of events.
 * throttleValue - Inserts a fixed delay between events. You can use this option to slow down the Monkey.
 *             If not specified, there is no delay and the events are generated as rapidly as possible.
 * emulatorName - emulator name
 * useWiremock - optional argument to use wiremock (default false)
 * wiremockVersion - optional argument to set wiremock version to use (default is used version on nodes)
 * wiremockPort - optional argument to set wiremock port to use (default 8080)
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

    def logcatFileName = "logcat_monkey"

    if (config.stageNameSuffix) {
        stageNameSuffix = config.stageNameSuffix
        logcatFileName = "logcat_monkey_${stageNameSuffix}"
    } else {
        stageNameSuffix = ""
        logcatFileName = "logcat_monkey"
    }

    if(config.seedValue) {
        seedValue = config.seedValue
    } else {
        seedValue = 1
    }

    if (config.eventCount) {
        eventCount = config.eventCount
    } else {
        eventCount = 1000
    }

    def utils = new Utilities(steps)
    def androidUtils = new AndroidUtilities(steps)
    def apkName = androidUtils.getApkType(env.BUILD_TYPE, config.apkNameRelease, config.apkNameDebug)

    timeout(60) {
        node("${config.nodeLabel}") {
            stage("Monkey tests ${stageNameSuffix}") {
                deleteDir()
                unstash "apkFiles"
                utils.runWiremock(config.useWiremock, env.WORKSPACE, config.wiremockVersion, config.wiremockPort)
                def emulatorName = config.emulatorName
                def apkFilePath = utils.getFilePath(androidUtils.getApkWildcard(apkName))
                androidUtils.killAllEmulatorsIfRunning()
                androidUtils.runAndroidEmulator(emulatorName, logcatFileName)
                androidUtils.installApk(apkFilePath)
                try {
                    def monkeyRunStdout = steps.sh(script:"""#!/bin/bash -xe
                        \$ANDROID_HOME/platform-tools/adb shell monkey -v -v -s ${seedValue} --throttle ${config.throttleValue} \
                         -p ${config.packageName} --kill-process-after-error --pct-syskeys 0  ${eventCount} 2>&1 | tee monkey.txt
                     """, returnStdout: true).trim()
                    steps.echo(monkeyRunStdout)
                    if (monkeyRunStdout.toLowerCase().contains("Monkey aborted due to error".toLowerCase())) {
                        steps.echo monkeyRunStdout
                        currentBuild.result = 'UNSTABLE'
                    } else if (monkeyRunStdout.toLowerCase().contains("No activities found to run, monkey aborted.".toLowerCase())) {
                        steps.error("No activities found to run, monkey aborted.")
                    }
                } catch (exception) {
                    utils.handleException(exception)
                } finally {
                    androidUtils.killAllEmulatorsIfRunning()
                    utils.shutdownWiremock(config.useWiremock, config.wiremockPort)
                    archiveArtifacts "**/logcat*.txt"
                }
                archiveArtifacts "**/monkey.txt"
            }
        }
    }
}
