#!/usr/bin/env groovy

/**
 * Created by Maciej Gasiorowski on 12/07/2017.
 */

package io.jenkins.mobilePipeline

class AndroidUtilities implements Serializable {

    def steps
    def androidHome = "/var/lib/jenkins/android-sdk-new"

    AndroidUtilities(steps) {
        this.steps = steps
    }

    def emulatorPidFinder() {
        def emulatorPid = steps.sh(script: "ps aux | grep -e '[a]vd' | awk '{ print \$2 }'", returnStdout: true).trim()
        return emulatorPid
    }

    def killAllEmulatorsIfRunning() {
        if (emulatorPidFinder()) {
            steps.sh "kill -9 \$(ps aux | grep -e '[a]vd' | awk '{ print \$2 }')"
        }
    }

    def runAndroidEmulator(emulatorName, logcatFileName) {
        def emulatorRunStdout = steps.sh(script: """#!/bin/bash -xe
                    #Start the emulator
                    ${androidHome}/emulator/emulator -avd ${emulatorName} -no-snapshot-load -no-snapshot-save -wipe-data -gpu on &
                    EMULATOR_PID=\$!
                 """, returnStdout: true).trim()
        steps.echo emulatorRunStdout
        for(int i = 0; i < 30; i++) {
            if (!emulatorPidFinder()) {
                steps.error "Emulator didn't start, check logs."
            }
        }
        steps.sh """#!/bin/bash -xe
                    # Wait for Android to finish booting
                    bootanim=""
                    failcounter=0
                    timeout_in_sec=180

                    until [[ "\$bootanim" =~ "stopped" ]]; do
                      bootanim=`${androidHome}/platform-tools/adb -e shell getprop init.svc.bootanim 2>&1 &`
                      if [[ "\$bootanim" =~ "device not found" || "\$bootanim" =~ "device offline"
                        || "\$bootanim" =~ "running" ]]; then
                        let "failcounter += 1"
                        echo "Waiting for emulator to start"
                        if [[ \$failcounter -gt timeout_in_sec ]]; then
                          echo "Timeout (\$timeout_in_sec seconds) reached; failed to start emulator"
                          exit 1
                        fi
                      fi
                      sleep 1
                    done

                    echo "Emulator is ready"

                    # Unlock the Lock Screen
                    ${androidHome}/platform-tools/adb shell input keyevent 82

                    # Clear and capture logcat
                    ${androidHome}/platform-tools/adb logcat -c
                    ${androidHome}/platform-tools/adb logcat -v time > ${logcatFileName}.txt &
                    LOGCAT_PID=\$!
                    ${androidHome}/platform-tools/adb shell settings put global animator_duration_scale 0.0
                    ${androidHome}/platform-tools/adb shell settings put global transition_animation_scale 0.0
                    ${androidHome}/platform-tools/adb shell settings put global window_animation_scale 0.0
                """
    }

    def installApk(apkFilePath) {
        def absoluteapkFilePath = "${steps.pwd()}/${apkFilePath}"
        steps.wrap([$class: "AnsiColorBuildWrapper", "colorMapName": "VGA"]) {
            def apkInstallationStdout = steps.sh(script: """#!/bin/bash -xe
                        ${androidHome}/platform-tools/adb install -r ${absoluteapkFilePath}
                     """, returnStdout: true).trim()
            if (apkInstallationStdout.toLowerCase().contains("Failure".toLowerCase())) {
                steps.error apkInstallationStdout
            }
        }
    }

    def getApkWildcard(apkName) {
        def apkWildcard
        if (apkName) {
            apkWildcard = "**/${apkName}"
        } else {
            apkWildcard = "**/*.apk"
        }
        return apkWildcard
    }

    def getGradleTasks(buildType, gradleTasksRelease, gradleTasksDebug) {
        if (buildType == "release") {
            return gradleTasksRelease
        } else {
            return gradleTasksDebug
        }
    }

    def getApkType(buildType, apkNameRelease, apkNameDebug) {
        if (buildType == "release") {
            return apkNameRelease
        } else {
            return apkNameDebug
        }
    }

    def setDefaultGradleOptions() {
        return "--info --full-stacktrace --profile --console=plain"
    }

    def setDefaultGradleTasks() {
        return ""
    }

    def setAndroidBuildCache(workspace) {
        steps.sh "echo \"\nandroid.buildCacheDir=${workspace}/.gradle/cache\" >> gradle.properties"
    }

    def stashGradleCache() {
        steps.stash name: "gradleCache", includes: "**/.gradle/**", excludes: "**/.gradle/**/proguard.txt", useDefaultExcludes: false
    }

    def unstashGradleCache() {
        try {
            steps.unstash "gradleCache"
        } catch(error) {
            if(!error.toString().toLowerCase().contains("No such saved stash ‘gradleCache’".toLowerCase())) {
                throw error
            }
        }
    }

    def stashAndroidBuildCache() {
        steps.stash name: "androidBuildCache", includes: "**/build/**", useDefaultExcludes: false
    }

    def ustashAndroidBuildCache() {
        try {
            steps.unstash "androidBuildCache"
        } catch(error) {
            if(!error.toString().toLowerCase().contains("No such saved stash ‘androidBuildCache’".toLowerCase())) {
                throw error
            }
        }
    }


    def archieveGradleProfileReport(gradleProfileFilenameSuffix) {
        def gradleProfileHtmlReportFile = steps.findFiles(glob: "**/reports/profile/**.html")
        def gradleProfileHtmlReportFilename = gradleProfileHtmlReportFile[0].name
        def gradleProfileHtmlReportFilenameWithoutExtension = gradleProfileHtmlReportFilename.take(gradleProfileHtmlReportFile[0].name.lastIndexOf('.'))
        def gradleProfileHtmlReportFileExtension = gradleProfileHtmlReportFilename[gradleProfileHtmlReportFilename.lastIndexOf('.')..-1]
        def gradleProfileHtmlReportFilePath = gradleProfileHtmlReportFile[0].path
        def gradleProfileHtmlReportPath = gradleProfileHtmlReportFilePath.take(gradleProfileHtmlReportFilePath.lastIndexOf("/"))
        steps.sh "mv ${gradleProfileHtmlReportFilePath} ${gradleProfileHtmlReportPath}/${gradleProfileHtmlReportFilenameWithoutExtension}-${gradleProfileFilenameSuffix}${gradleProfileHtmlReportFileExtension}"
        steps.archiveArtifacts "**/reports/profile/**"
    }

    def getJunitTestReportFile(junitTestReportFile) {
        if (junitTestReportFile) {
            return "**/${junitTestReportFile.toString()}"
        } else {
            return "**/TEST-*.xml"
        }
    }
}

