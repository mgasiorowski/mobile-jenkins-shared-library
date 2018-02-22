# Stages

## Universal

### Checkout scm
[Source](https://github.com/mgasiorowski/mobile-jenkins-shared-library/blob/master/vars/checkoutScm.groovy)

#### Parameters
* nodeLabel - label where repository will be checkout
* filesToStash - optional files to stash, to use them later, (Ant-style include patterns - 
 https://ant.apache.org/manual/dirtasks.html#patterns)
 
### Pipeline options

[Source](https://github.com/mgasiorowski/mobile-jenkins-shared-library/blob/master/vars/pipelineOptions.groovy)

Configure pipeline options

#### Parameters
* maxNumberBuildsToKeep - max number builds to keep

## Android

### Static analysis
[Source](https://github.com/mgasiorowski/mobile-jenkins-shared-library/blob/master/vars/androidStaticAnalysis.groovy)

You must have **staticAnalysisStatusThresholds.properties** file in **config/jenkins** directory.
If you wan to fail build if thresholds are exceeded, you can use:

```
lintUnstableTotalAll
lintUnstableTotalHigh
lintUnstableTotalNormal
lintFailedTotalAll
lintFailedTotalHigh
lintFailedTotalNormal
pmdUnstableTotalAll
pmdUnstableTotalHigh
pmdUnstableTotalNormal
pmdFailedTotalAll
pmdFailedTotalHigh
pmdFailedTotalNormal
findbugsUnstableTotalAll
findbugsUnstableTotalHigh
findbugsUnstableTotalNormal
findbugsFailedTotalAll
findbugsFailedTotalHigh
findbugsFailedTotalNormal
```

For example:
```
# Android lint
lintUnstableTotalHigh=13
lintUnstableTotalNormal=101
lintFailedTotalHigh=13
lintFailedTotalNormal=101
# PMD
pmdUnstableTotalHigh=17
pmdUnstableTotalNormal=278
pmdFailedTotalHigh=17
pmdFailedTotalNormal=278
# FindBugs
findbugsUnstableTotalHigh=8
findbugsUnstableTotalNormal=49
findbugsFailedTotalHigh=8
findbugsFailedTotalNormal=49
```

#### Parameters
* nodeLabel - label where static analysis will be run
* gradleTasksDebug - gradle tasks to build binary for debug
* gradleTasksRelease - gradle tasks to build binary for release
* androidLintResultsFile - optional android lint result file path
* pmdResultsFile - optional pmd result file path
* findBugsResultFile - optional findbugs result file path
* filesToArchieve - optional file to archieve, for ex. report from detekt
