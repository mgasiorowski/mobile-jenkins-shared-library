#!/usr/bin/env groovy

/**
 * Created by Maciej Gasiorowski on 07/07/2017.
 */

package io.jenkins.mobilePipeline

class StaticAnalysisUtilities implements Serializable {

    def steps

    StaticAnalysisUtilities(steps) {
        this.steps = steps
    }

    def getstatusThresholdsPropertiesFilePath(statusThresholdsPropertiesFile) {
        if (statusThresholdsPropertiesFile) {
            return statusThresholdsPropertiesFile
        } else {
            return "config/jenkins/staticAnalysisStatusThresholds.properties"
        }
    }

    def checkIfExistsThresholdsPropertiesFile(statusThresholdsPropertiesFile) {
        if(!steps.fileExists(statusThresholdsPropertiesFile)) {
            steps.error "There is no status thresholds properties file, you must add it"
        }
    }

    def getAndroidStatusThresholdsPropValues(statusThresholds, statusThresholdsPropertiesFile) {
        def props = steps.readProperties file: statusThresholdsPropertiesFile

        switch (statusThresholds) {
            case "lintUnstableTotalAll":
                return props["lintUnstableTotalAll"].toString()
                break
            case "lintUnstableTotalHigh":
                return props["lintUnstableTotalHigh"].toString()
                break
            case "lintUnstableTotalNormal":
                return props["lintUnstableTotalNormal"].toString()
                break
            case "lintFailedTotalAll":
                return props["lintFailedTotalAll"].toString()
                break
            case "lintFailedTotalHigh":
                return props["lintFailedTotalHigh"].toString()
                break
            case "lintFailedTotalNormal":
                return props["lintFailedTotalNormal"].toString()
                break
            case "pmdUnstableTotalAll":
                return props["pmdUnstableTotalAll"].toString()
                break
            case "pmdUnstableTotalHigh":
                return props["pmdUnstableTotalHigh"].toString()
                break
            case "pmdUnstableTotalNormal":
                return props["pmdUnstableTotalNormal"].toString()
                break
            case "pmdFailedTotalAll":
                return props["pmdFailedTotalAll"].toString()
                break
            case "pmdFailedTotalHigh":
                return props["pmdFailedTotalHigh"].toString()
                break
            case "pmdFailedTotalNormal":
                return props["pmdFailedTotalNormal"].toString()
                break
            case "findbugsUnstableTotalAll":
                return props["findbugsUnstableTotalAll"].toString()
                break
            case "findbugsUnstableTotalHigh":
                return props["findbugsUnstableTotalHigh"].toString()
                break
            case "findbugsUnstableTotalNormal":
                return props["findbugsUnstableTotalNormal"].toString()
                break
            case "findbugsFailedTotalAll":
                return props["findbugsFailedTotalAll"].toString()
                break
            case "findbugsFailedTotalHigh":
                return props["findbugsFailedTotalHigh"].toString()
                break
            case "findbugsFailedTotalNormal":
                return props["findbugsFailedTotalNormal"].toString()
                break
            case "clangUnstableTotalAll":
                return props["clangUnstableTotalAll"].toString()
                break
            case "clangUnstableTotalHigh":
                return props["clangUnstableTotalHigh"].toString()
                break
            case "clangUnstableTotalNormal":
                return props["clangUnstableTotalNormal"].toString()
                break
            case "clangFailedTotalAll":
                return props["clangFailedTotalAll"].toString()
                break
            case "clangFailedTotalHigh":
                return props["clangFailedTotalHigh"].toString()
                break
            case "clangFailedTotalNormal":
                return props["clangFailedTotalNormal"].toString()
                break
            case "swiftlintUnstableTotalAll":
                return props["swiftlintUnstableTotalAll"].toString()
                break
            case "swiftlintUnstableTotalHigh":
                return props["swiftlintUnstableTotalHigh"].toString()
                break
            case "swiftlintUnstableTotalNormal":
                return props["swiftlintUnstableTotalNormal"].toString()
                break
            case "swiftlintFailedTotalAll":
                return props["swiftlintFailedTotalAll"].toString()
                break
            case "swiftlintFailedTotalHigh":
                return props["swiftlintFailedTotalHigh"].toString()
                break
            case "swiftlintFailedTotalNormal":
                return props["swiftlintFailedTotalNormal"].toString()
                break
            case "eslintUnstableTotalAll":
                return props["swiftlintUnstableTotalAll"].toString()
                break
            case "eslintUnstableTotalHigh":
                return props["swiftlintUnstableTotalHigh"].toString()
                break
            case "eslintUnstableTotalNormal":
                return props["swiftlintUnstableTotalNormal"].toString()
                break
            case "eslintFailedTotalAll":
                return props["swiftlintFailedTotalAll"].toString()
                break
            case "eslintFailedTotalHigh":
                return props["swiftlintFailedTotalHigh"].toString()
                break
            case "eslintFailedTotalNormal":
                return props["swiftlintFailedTotalNormal"].toString()
                break
            default:
                return ""
        }

    }
}
