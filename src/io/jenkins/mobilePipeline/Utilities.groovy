#!/usr/bin/env groovy

/**
 * Created by Maciej Gasiorowski on 07/07/2017.
 */

package io.jenkins.mobilePipeline

class Utilities implements Serializable {

    def steps

    Utilities(steps) {
        this.steps = steps
    }

    def getBranchName(scm, branchNameEnvironmentVariable) {
        if (branchNameEnvironmentVariable) {
            return branchNameEnvironmentVariable
        } else {
            def branchName = scm.branches[0].name.minus("*/")
            return branchName
        }
    }

    def getFilePath(wildcard) {
        def findedFiles = steps.findFiles(glob: wildcard)
        if (!findedFiles) {
            steps.error("Can't find file with wildcard: ${wildcard}")
        }
        return findedFiles[0].path
    }

    def getFilesToStash(filesToStash) {
        if (filesToStash) {
            return "**/${filesToStash.toString()}"
        } else {
            return "**"
        }
    }

    def getBuildWorkspace(isReactNative=false, platform=null, workspace, rootBuildScript){
        if (isReactNative && isReactNative.toBoolean() && platform == "android" && !rootBuildScript) {
            return "${workspace}/android"
        } else if (isReactNative && isReactNative.toBoolean() && platform == "ios" && !rootBuildScript) {
            return "${workspace}/ios"
        } else if (isReactNative && isReactNative.toBoolean() && platform == "android" && rootBuildScript) {
            return "${workspace}/${rootBuildScript}/android"
        } else if (isReactNative && isReactNative.toBoolean() && platform == "ios" && rootBuildScript) {
            return "${workspace}/${rootBuildScript}/ios"
        } else if (rootBuildScript) {
            return "${workspace}/${rootBuildScript}"
        } else {
            return workspace
        }

    }

    def getStageSuffix(stageSuffix) {
        if (stageSuffix) {
            return "${stageSuffix} - "
        } else {
            return ""
        }
    }

    def runWiremock(useWiremock=false, workspace, wiremockVersion, wiremockPort){
        if (useWiremock && useWiremock.toBoolean()) {
            def wiremockPortConfiguration
            if (wiremockPort) {
                wiremockPortConfiguration = "--port ${wiremockPort}"
            } else {
                wiremockPortConfiguration = ""
            }
            def wiremockJarPath
            if (wiremockVersion) {
                def wiremockMavenUrl = "http://repo1.maven.org/maven2/com/github/tomakehurst/wiremock-standalone/${wiremockVersion}/wiremock-standalone-${wiremockVersion}.jar"
                steps.sh "curl -o ${workspace}/wiremock-standalone-${wiremockVersion}.jar \"${wiremockMavenUrl}\""
                def wiremockFindedFile = steps.findFiles(glob: "**/wiremock-standalone-*.jar")
                wiremockJarPath = wiremockFindedFile[0].path
            } else {
                wiremockJarPath = "~/support/wiremock-standalone-2.6.0.jar"
            }

            steps.sh "java -jar ${wiremockJarPath} ${wiremockPortConfiguration} --root-dir ${workspace}/tools/wiremock/mappings >> wiremock.log 2>&1 &"
        }
    }

    def shutdownWiremock(useWiremock=false, wiremockPortConfiguration){
        if (useWiremock && useWiremock.toBoolean()) {
            def wiremockPort
            if (wiremockPortConfiguration) {
                wiremockPort = wiremockPortConfiguration
            } else {
                wiremockPort = 8080
            }
            steps.sh "curl -X POST --data '' \"http://localhost:${wiremockPort}/__admin/shutdown\""
            steps.archive "**/wiremock.log"
        }

    }

    def handleException(exception) {
        def caughtExceptionMessage = "Caught: ${exception}"
        if(!exception.toString().toLowerCase() == "hudson.AbortException: script returned exit code 143".toLowerCase()) {
            steps.echo caughtExceptionMessage
            steps.currentBuild.result = 'UNSTABLE'
        } else {
            steps.echo caughtExceptionMessage
            steps.error exception.toString()
        }
    }
}
