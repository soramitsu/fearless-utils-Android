@Library('jenkins-library@feature/DOPS-2955/update_android_shared_feature') _

def pipeline = new org.android.ShareFeature(
    steps: this,
    agentImage: "build-tools/android-build-box-jdk17:latest",
    lint: true,
    test: true,
    dojoProductType: "fearless"
)

pipeline.runPipeline()