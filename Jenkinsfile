@Library('jenkins-library' ) _
// Pipeline
new org.android.ShareFeature().call(
    dockerImage: "build-tools/android-build-box-jdk17:latest",
    lint: true,
    test: true
)