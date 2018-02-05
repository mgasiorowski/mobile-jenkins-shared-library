#!/usr/bin/env groovy

/**
 * Created by Maciej Gasiorowski on 06/07/2017.
 *
 * Configure pipeline options
 *
 * maxNumberBuildsToKeep - max number of build to keep
 */

def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    if (config.maxNumberBuildsToKeep) {
        maxNumberBuildsToKeep = config.maxNumberBuildsToKeep.toString()
    } else {
        maxNumberBuildsToKeep = "10"
    }

    properties([buildDiscarder(logRotator(numToKeepStr: maxNumberBuildsToKeep))])
}
