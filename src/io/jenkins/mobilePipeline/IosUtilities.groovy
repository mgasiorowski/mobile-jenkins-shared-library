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

    def stashIosBuildCache() {
        steps.stash name: "iosBuildCache", includes: "**/derivedData/**, **/Podfile.lock, **/Pods, **/videostar.xcworkspace/**",
                excludes: "**/derivedData/**/*-iphonesimulator/**", useDefaultExcludes: false
    }

    def ustashIosBuildCache() {
        try {
            steps.unstash "iosBuildCache"
        } catch(error) {
            if(!error.toString().toLowerCase().contains("No such saved stash ‘iosBuildCache’".toLowerCase())) {
                throw error
            }
        }
    }

    def stashRubyBuildCache() {
        steps.stash name: "rubyBuildCache", includes: "**/.bundle/**, **/Gemfile.lock, **/vendor/**", useDefaultExcludes: false
    }

    def ustashRubyBuildCache() {
        try {
            steps.unstash "rubyBuildCache"
        } catch(error) {
            if(!error.toString().toLowerCase().contains("No such saved stash ‘rubyBuildCache’".toLowerCase())) {
                throw error
            }
        }
    }
}

