#!/usr/bin/env groovy

/**
 * Created by Maciej Gasiorowski on 29/08/2017.
 */

package io.jenkins.mobilePipeline

class ReactNativeUtilities implements Serializable {

    def steps

    ReactNativeUtilities(steps) {
        this.steps = steps
    }

    def getCheckstyleReportFile(checkstyleReportFile) {
        if (checkstyleReportFile) {
            return "**/${checkstyleReportFile.toString()}"
        } else {
            return "**/eslint_report.xml"
        }
    }

    def stashNpmCache() {
        steps.sh "chmod -R u+w node_modules"
        steps.stash name: "npmCache", includes: "**/node_modules/**", useDefaultExcludes: false
    }

    def unstashNpmCache() {
        try {
            steps.unstash "npmCache"
        } catch(error) {
            if(!error.toString().toLowerCase().contains("No such saved stash ‘npmCache’".toLowerCase())) {
                throw error
            }
        }
    }
}
