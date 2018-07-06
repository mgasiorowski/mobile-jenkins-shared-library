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

    def setFastlaneXcodeListTimeout() {
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
}
