@Library('jenkins-library@feature/DOPS-2955/update_android_shared_feature') _

def pipeline = new org.android.ShareFeature(
    steps: this,
    agentImage: "build-tools/android-build-box-jdk17:latest",
    sonarProjectKey: "fearless:fearless-utils-Android",
    sonarProjectName: "fearless-utils-Android",
    lint: true,
    test: true,
    dojoProductType: "fearless"
)

pipeline.runPipeline()