@Library('jenkins-library@feature/DOPS-2406-limit-the-execution' ) _
// Pipeline
new org.android.ShareFeature().call(
    dockerImage: "build-tools/android-build-box-jdk17:latest",
    lint: true,
    test: true
)
