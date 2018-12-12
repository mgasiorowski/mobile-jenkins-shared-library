# Stages

Table of Contents
=================
* [Universal](#universal)
    * [Checkout scm](#checkout-scm)
        * [Parameters](#parameters)
    * [Pipeline options](#pipeline-options)
        * [Parameters](#parameters-1)
* [Android](#android)
    * [Static analysis](#static-analysis)
        * [Parameters](#parameters-2)
    * [Unit Tests](#unit-tests)
        * [Parameters](#parameters-3)
    * [UI Tests](#ui-tests)
        * [Parameters](#parameters-4)
    * [Monkey tests](#monkey-tests)
        * [Parameters](#parameters-5)
    * [Build binary](#build-binary)
        * [Parameters](#parameters-6)
    * [Beta upload](#beta-upload)
        * [Parameters](#parameters-7)
    * [Add trust CA to apk](#add-trust-ca-to-apk)
        * [Parameters](#parameters-8)
* [iOS](#ios)
    * [Static analysis](#static-analysis-1)
        * [Parameters](#parameters-9)
    * [Swiftlint](#swiftlint)
        * [Parameters](#parameters-10)
    * [Tests](#tests)
        * [Parameters](#parameters-11)
    * [Build](#build)
        * [Parameters](#parameters-12)
* [React Native](#react-native)
    * [Install Dependencies](#install-dependencies)
        * [Parameters](#parameters-13)
    * [Static analysis](#static-analysis-2)
        * [Parameters](#parameters-14)
    * [Unit Tests](#unit-tests-1)
        * [Parameters](#parameters-15)
    * [Bundle Android Resources](#bundle-android-resources)
        * [Parameters](#parameters-16)

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

Default parameters for gradle tasks:

`--info --full-stacktrace --refresh-dependencies --continue --profile`

Default parameters for emulator run:

`-no-snapshot-load -no-snapshot-save -wipe-data -gpu on`

Disabled animations in emulator:

```
adb shell settings put global animator_duration_scale 0.0
adb shell settings put global transition_animation_scale 0.0
adb shell settings put global window_animation_scale 0.0
```

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
* isReactNative - optional parameter to mark build as react native project
* rootBuildScript - optional argument to set root build script
* gradleTasksDebug - gradle tasks to build binary for debug
* gradleTasksRelease - gradle tasks to build binary for release
* androidLintResultsFile - optional android lint result file path
* pmdResultsFile - optional pmd result file path
* findBugsResultFile - optional findbugs result file path
* filesToArchieve - optional file to archieve, for ex. report from detekt
* useGradleCache - optional argument to turn on/off gradle cache, default true
* useBuildCache - optional argument to turn on/off build cache, default true

### Unit Tests
[Source](https://github.com/mgasiorowski/mobile-jenkins-shared-library/blob/master/vars/androidUnitTests.groovy)

Run android unit tests

#### Parameters
* nodeLabel - label where unit tests will be run
* isReactNative - optional parameter to mark build as react native project
* rootBuildScript - optional argument to set root build script
* stageSuffix - optional suffix for stage name
* junitTestReportFile - optional file name with unit tests reports
* gradleTasksDebug - gradle tasks to build binary for debug
* gradleTasksRelease - gradle tasks to build binary for release
* useWiremock - optional argument to use wiremock (default false)
* wiremockVersion - optional argument to set wiremock version to use (default is used version on 
[nodes](https://github.com/mgasiorowski/mobile-jenkins-shared-library/blob/master/src/io/jenkins/mobilePipeline/Utilities.groovy#L73))
* wiremockPort - optional argument to set wiremock port to use (default 8080)
* useGradleCache - optional argument to turn on/off gradle cache, default true
* useBuildCache - optional argument to turn on/off build cache, default true

### UI Tests
[Source](https://github.com/mgasiorowski/mobile-jenkins-shared-library/blob/master/vars/androidUITests.groovy)

Run android UI tests, you must have configured android emulator or connected device.

#### Parameters
* nodeLabel - label where UI tests will be run
* rootBuildScript - optional argument to set root build script
* stageSuffix - optional suffix for stage name
* isReactNative - optional parameter to mark build as react native project
* junitTestReportFile - optional file name with unit tests reports
* gradleTasksDebug - gradle tasks to build binary for debug
* gradleTasksRelease - gradle tasks to build binary for release
* useWiremock - optional argument to use wiremock (default false)
* wiremockVersion - optional argument to set wiremock version to use (default is used version on 
[nodes](https://github.com/mgasiorowski/mobile-jenkins-shared-library/blob/master/src/io/jenkins/mobilePipeline/Utilities.groovy#L73))
* wiremockPort - optional argument to set wiremock port to use (default 8080)
* useGradleCache - optional argument to turn on/off gradle cache, default true
* useBuildCache - optional argument to turn on/off build cache, default true

### Monkey tests
[Source](https://github.com/mgasiorowski/mobile-jenkins-shared-library/blob/master/vars/androidMonkeyTests.groovy)

Run android monkey tests, you must have configured android emulator or connected device.

You must have already built apk, from other stages.

#### Parameters
* nodeLabel - label where binary will be build
* apkName - name of apk to test
* stageNameSuffix - optional suffix for stage name
* packageName - package name value of app for tests
* eventCount - number of events
* seedValue - Seed value for pseudo-random number generator. If you re-run the Monkey with the same seed value, 
it will generate the same sequence of events.
* throttleValue - Inserts a fixed delay between events. You can use this option to slow down the Monkey.
If not specified, there is no delay and the events are generated as rapidly as possible.
* monkeyCommandOptions - optional argument to set additional monkey test options (
             https://developer.android.com/studio/test/monkey#command-options-reference)
* emulatorName - emulator name
* useWiremock - optional argument to use wiremock (default false)
* wiremockVersion - optional argument to set wiremock version to use (default is used version on 
[nodes](https://github.com/mgasiorowski/mobile-jenkins-shared-library/blob/master/src/io/jenkins/mobilePipeline/Utilities.groovy#L73))
* wiremockPort - optional argument to set wiremock port to use (default 8080)

### Build binary
[Source](https://github.com/mgasiorowski/mobile-jenkins-shared-library/blob/master/vars/androidBuild.groovy)

Build android binary

#### Parameters

 * nodeLabel - label where binary will be build
 * isReactNative - optional parameter to mark build as react native project
 * gradleTasksDebug - gradle tasks to build binary for debug
 * gradleTasksRelease - gradle tasks to build binary for release
 * stashApk - optional apk files to stash, to use them later, (Ant-style include patterns - 
 https://ant.apache.org/manual/dirtasks.html#patterns)
 * stageSuffix - optional suffix for stage name
 * rootBuildScript - optional argument to set root build script
 * useGradleCache - optional argument to turn on/off gradle cache, default true
 * useBuildCache - optional argument to turn on/off build cache, default true

### Beta upload
[Source](https://github.com/mgasiorowski/mobile-jenkins-shared-library/blob/master/vars/androidBetaUpload.groovy)

Upload binary to beta distribution. If you have configured posibility to add build type suffix you can add him with
buildSuffixName parameter, if not script will try to add branch name for all branches except master.

#### Parameters
* nodeLabel - label where repository will be checkout
* gradleTasksDebug - gradle tasks to build binary for debug
* gradleTasksRelease - gradle tasks to build binary for release
* buildSuffixName - optional suffix for build
 * stashApk - optional apk files to stash, to use them later, (Ant-style include patterns - 
 https://ant.apache.org/manual/dirtasks.html#patterns)
* isReactNative - optional parameter to mark build as react native project
* stageSuffix - optional suffix for stage name
* rootBuildScript - optional argument to set root build script
* useGradleCache - optional argument to turn on/off gradle cache, default true
* useBuildCache - optional argument to turn on/off build cache, default true


### Add trust CA to apk
[Source](https://github.com/mgasiorowski/mobile-jenkins-shared-library/blob/master/vars/androidAddTrustCaToApk.groovy)

Trust custom CA for debugging with charles, wireshark etc. on Android < 7
https://android-developers.googleblog.com/2016/07/changes-to-trusted-certificate.html
https://github.com/levyitay/AddSecurityExceptionAndroid
https://github.com/mgasiorowski/AddSecurityExceptionAndroid

You must have built apk, from other stages.

You must have configured apktool.
https://github.com/mgasiorowski/AddSecurityExceptionAndroid#prerequisites

#### Parameters
* nodeLabel - label which node will be used
* apkName - apk name to add trust ca


## iOS

### Static analysis
[Source](https://github.com/mgasiorowski/mobile-jenkins-shared-library/blob/master/vars/iosStaticAnalysis.groovy)

Static analysis for ios, it uses Xcode Analyze.

You must have staticAnalysisStatusThresholds.properties file in config/jenkins directory. If you wan to fail build if thresholds are exceeded, you can use:

```
clangUnstableTotalAll
clangUnstableTotalHigh
clangUnstableTotalNormal
clangFailedTotalAll
clangFailedTotalHigh
clangFailedTotalNormal
```

For example
```
clangUnstableTotalHigh=13
clangUnstableTotalNormal=101
clangFailedTotalHigh=13
clangFailedTotalNormal=101
```

#### Parameters
* nodeLabel - label where static analysis will be run
* fastlaneLane - fastlane lane to execute
* statusThresholdsPropertiesFile - path to status threashold properties file
* isReactNative - optional parameter to mark build as react native project
* stageSuffix - optional suffix for stage name
* rootBuildScript - optional argument to set root build script
* useRubyCache - optional argument to turn on/off ruby build cache as string, default true
* useBuildCache - optional argument to turn on/off build cache as string, default true

### Swiftlint
[Source](https://github.com/mgasiorowski/mobile-jenkins-shared-library/blob/master/vars/iosSwiftlint.groovy)

Static analysis with [Swiftlint](https://github.com/realm/SwiftLint) configured with fastlane.

You must have staticAnalysisStatusThresholds.properties file in config/jenkins directory. If you wan to fail build if thresholds are exceeded, you can use:
```
swiftlintUnstableTotalAll
swiftlintUnstableTotalHigh
swiftlintUnstableTotalNormal
swiftlintFailedTotalAll
swiftlintFailedTotalHigh
swiftlintFailedTotalNormal
```

For example:
```
# Swiftlint
swiftlintUnstableTotalHigh=13
swiftlintUnstableTotalNormal=101
swiftlintFailedTotalHigh=13
swiftlintFailedTotalNormal=101
```

#### Parameters
* nodeLabel - label where static analysis will be run
* fastlaneLane - fastlane lane to execute
* statusThresholdsPropertiesFile - path to status threashold properties file
* isReactNative - optional parameter to mark build as react native project
* stageSuffix - optional suffix for stage name
* rootBuildScript - optional argument to set root build script
* useRubyCache - optional argument to turn on/off ruby build cache as string, default true
* useBuildCache - optional argument to turn on/off build cache as string, default true

### Tests
[Source](https://github.com/mgasiorowski/mobile-jenkins-shared-library/blob/master/vars/iosTests.groovy)

Run test configured with fastlane, unit, e2e etc.

#### Parameters
* nodeLabel - label where to do static analysis
* stageSuffix - suffix for stage (tests type)
* isReactNative - optional parameter to mark build as react native project
* fastlaneLane - fastlane lane to execute
* junitTestReportFile - optional file name with unit tests reports
* useWiremock - optional argument to use wiremock (default false)
* wiremockVersion - optional argument to set wiremock version to use (default is used version on 
[nodes](https://github.com/mgasiorowski/mobile-jenkins-shared-library/blob/master/src/io/jenkins/mobilePipeline/Utilities.groovy#L73))
* wiremockPort - optional argument to set wiremock port to use (default 8080)
* rootBuildScript - optional argument to set root build script
* useRubyCache - optional argument to turn on/off ruby build cache as string, default true
* useBuildCache - optional argument to turn on/off build cache as string, default true

### Build
[Source](https://github.com/mgasiorowski/mobile-jenkins-shared-library/blob/master/vars/iosBuild.groovy)
Build project with fastlane.

#### Parameters
* nodeLabel - label where to do static analysis
* stageSuffix - suffix for stage (tests type)
* isReactNative - optional parameter to mark build as react native project
* fastlaneLane - fastlane lane to execute
* rootBuildScript - optional argument to set root build script
* useRubyCache - optional argument to turn on/off ruby build cache as string, default true
* useBuildCache - optional argument to turn on/off build cache as string, default true

## React Native

### Install Dependencies
[Source](https://github.com/mgasiorowski/mobile-jenkins-shared-library/blob/master/vars/reactNativeInstallDependencies.groovy)

Install dependecies for project.

#### Parameters
* nodeLabel - label where install dependencies
* dependencyInstallationCommand - dependency installation command
* filesToStash - optional files to stash, to use them later, (Ant-style include patterns -
https://ant.apache.org/manual/dirtasks.html#patterns)

### Static analysis
[Source](https://github.com/mgasiorowski/mobile-jenkins-shared-library/blob/master/vars/reactNativeStaticAnalysis.groovy)

Static analysis for React Native components.

You must have staticAnalysisStatusThresholds.properties file in config/jenkins directory. If you wan to fail build if thresholds are exceeded, you can use:
```
eslintUnstableTotalAll
eslintUnstableTotalHighstatusThresholdsPropertiesFile
eslintUnstableTotalNormal
eslintFailedTotalAll
eslintFailedTotalHigh
eslintFailedTotalNormal
```

For example:
```
# Eslint
eslintUnstableTotalHigh=13
eslintUnstableTotalNormal=101
eslintFailedTotalAll=13
eslintFailedTotalHigh=101
```

#### Parameters
* nodeLabel - label where install dependencies
* staticAnalysisCommand - command to run static analysis
* statusThresholdsPropertiesFile - path to status threashold properties file
* checkstyleReportFile - optional parameter with path to report file (https://ant.apache.org/manual/dirtasks.html#patterns), default **/eslint_report.xm 

### Unit Tests
[Source](https://github.com/mgasiorowski/mobile-jenkins-shared-library/blob/master/vars/reactNativeUnitTests.groovy)

Unit tests for React Native components.

#### Parameters
* nodeLabel - label where install dependencies
* unitTestsCommand - command to run unit tests

### Bundle Android Resources
[Source](https://github.com/mgasiorowski/mobile-jenkins-shared-library/blob/master/vars/reactNativeBundleAndroidResources.groovy)

Bundle React Native resources to Android project.

### Parameters
* nodeLabel - label where to bundle resources
* bundleAndroidResourcesCommand - command to bundle android resources
