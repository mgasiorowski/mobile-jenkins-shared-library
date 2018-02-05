#!/usr/bin/env groovy

/**
 * Created by Maciej Gasiorowski on 06/07/2017.
 *
 * Synchronize build numbers
 *
 * Configuration
 * platfom - name of platfom (android or ios)
 * projectName - name of project, if more than one project you can use regex,
 */

import jenkins.model.*

def call(body){
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def searchedJobName = config.platformName + "_" + config.projectName + ".*"
    def searchedQaJobName = searchedJobName + "_qa.*"
    def searchedTestflightJobName = searchedJobName + "_testflight.*"
    def jenkinsInstance = jenkins.model.Jenkins.instance
    def allItems = jenkinsInstance.getAllItems()
    List<String> matchedItems = []
    def highestBuildNumber = 0
    def qaBuildNumber = 0
    for(int i = 0; i < allItems.size(); i++) {
        if((allItems[i].name.matches(searchedJobName)) && !(allItems[i] instanceof com.cloudbees.hudson.plugins.folder.Folder)) {
            matchedItems << allItems[i]
            if ("${JOB_NAME}" =~ searchedQaJobName) {
                qaBuildNumber = allItems[i].nextBuildNumber
            } else if(allItems[i].nextBuildNumber > highestBuildNumber) {
                highestBuildNumber = allItems[i].nextBuildNumber
            }
        }
    }
    for(int i = 0; i < matchedItems.size(); i++) {
        if ("${JOB_NAME}" =~ searchedQaJobName) {
            if (matchedItems[i].name =~ searchedTestflightJobName) {
                matchedItems[i].updateNextBuildNumber(qaBuildNumber)
            }
        }
        matchedItems[i].updateNextBuildNumber(highestBuildNumber)
    }
}