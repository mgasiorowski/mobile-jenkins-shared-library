#!/usr/bin/env groovy

/**
 * Created by Maciej Gasiorowski on 23/08/2017.
 */

package io.jenkins.mobilePipeline

class IosUtilities implements Serializable {

    def steps

    IosUtilities(steps) {
        this.steps = steps
    }

    def addZshrcConfigFileToShell() {
        steps.echo "source ~/.zshrc"
        return "source \$HOME/.zshrc"
    }

    def setFastlaneXcodeListTimout() {
        steps.echo "FASTLANE_XCODE_LIST_TIMEOUT=30"
        return "FASTLANE_XCODE_LIST_TIMEOUT=30"
    }

    def installProjectEnvironmentRequirements() {
        steps.echo "bundle install --path vendor/bundle"
        return "bundle install --path vendor/bundle"
    }

    def runFastlane(fastlanePassword, fastlaneLane) {
        steps.echo "bundle exec env FASTLANE_PASSWORD=${fastlanePassword} fastlane ios ${fastlaneLane}"
        return "bundle exec env FASTLANE_PASSWORD=${fastlanePassword} fastlane ios ${fastlaneLane}"
    }

    def getJunitTestReportFile(junitTestReportFile) {
        if (junitTestReportFile) {
            return "**/${junitTestReportFile.toString()}"
        } else {
            return "**/report.junit"
        }
    }

    def addXcodebuildLogToArtifacts(stageName) {
        def xcodebuildLogFile = steps.findFiles(glob: "**/output/logs/*.log")
        if (xcodebuildLogFile) {
            def xcodebuildLogFilename = xcodebuildLogFile[0].name
            def xcodebuildLogFileExtension = xcodebuildLogFilename[xcodebuildLogFilename.lastIndexOf('.')..-1]
            def xcodebuildLogFilePath = xcodebuildLogFile[0].path
            def xcodebuildLogPath = xcodebuildLogFilePath.take(xcodebuildLogFilePath.lastIndexOf("/"))
            def xcodebuildStageLogFilename = "xcodebuild-${stageName}${xcodebuildLogFileExtension}".replaceAll("\\s", "")
            steps.echo "mv \"${xcodebuildLogFilePath}\" \"${xcodebuildLogPath}/${xcodebuildStageLogFilename}\""
            steps.sh "mv \"${xcodebuildLogFilePath}\" \"${xcodebuildLogPath}/${xcodebuildStageLogFilename}\""
            steps.archiveArtifacts artifacts: "**/logs/${xcodebuildStageLogFilename}", allowEmptyArchive: true
        } else {
            steps.archiveArtifacts artifacts: "**/logs/xcodebuild.log", allowEmptyArchive: true
        }
    }

    def stashIosBuildCache() {
        steps.sh "chmod -R u+w *"
        steps.stash name: "iosBuildCache", includes: "**/derivedData/**, **/Podfile.lock, **/Pods/**, **/videostar.xcworkspace/**",
                excludes: "**/derivedData/**/*-iphonesimulator/**", useDefaultExcludes: false
    }

    def ustashIosBuildCache(useBuildCache=true) {
        if (!useBuildCache || useBuildCache.toBoolean()) {
            try {
                steps.echo "Stage with build cache"
                steps.unstash "iosBuildCache"
            } catch (error) {
                if (!error.toString().toLowerCase().contains("No such saved stash ‘iosBuildCache’".toLowerCase())) {
                    throw error
                }
            }
        } else {
            steps.echo "Stage without build cache"
        }
    }

    def stashRubyBuildCache() {
        steps.sh "chmod -R u+w *"
        steps.stash name: "rubyBuildCache", includes: "**/.bundle/**, **/Gemfile.lock, **/vendor/**", useDefaultExcludes: false
    }

    def ustashRubyBuildCache(useRubyCache=true) {
        if (!useRubyCache || useRubyCache.toBoolean()) {
            try {
                steps.echo "Stage with ruby cache"
                steps.unstash "rubyBuildCache"
            } catch (error) {
                if (!error.toString().toLowerCase().contains("No such saved stash ‘rubyBuildCache’".toLowerCase())) {
                    throw error
                }
            }
        } else {
            steps.echo "Stage without ruby cache"
        }
    }
}
