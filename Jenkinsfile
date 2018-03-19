def urlBranch
def workspace
def branch
def branchType
def baseVersion
def version
def pullRequest
def refspecs


node() {
    workspace = pwd()
    branch = env.BRANCH_NAME.replaceAll(/\//, "-")
    branchType = env.BRANCH_NAME.split(/\//)[0]
    urlBranch = env.BRANCH_NAME.replaceAll(/\//, "%252F")
    baseVersion = "${env.BUILD_NUMBER}"
    version = "$branch-$baseVersion"
    env.PROJECT = "AutomationFramework"
    def branchCheckout
    pullRequest = env.CHANGE_ID
    if (pullRequest) {
        branchCheckout = "pr/${pullRequest}"
        refspecs = '+refs/pull/*/head:refs/remotes/origin/pr/*'
    }
    else {
        branchCheckout = env.BRANCH_NAME
        refspecs = '+refs/heads/*:refs/remotes/origin/*'
    }
    stage('Checkout Test Framework') { // for display purposes
        // Get the test code from GitHub repository
        checkout([
                $class           : 'GitSCM',
                branches: [[ name: "*/${branchCheckout}"]],
                userRemoteConfigs: [[
                    credentialsId: 'e08f3fab-ba06-459b-bebb-5d7df5f683a3',
                    url          : 'git@github.com:Coveros/GherkinBuilder.git',
                    refspec      : "${refspecs}"
                                    ]]
        ])
    }
    stage('Run Unit Tests') {
        sh "mvn clean test"
        sh "mv target target-unit"
    }
    stage('Perform SonarQube Analysis') {
        sh """
            mvn clean test -Dskip.unit.tests sonar:sonar \
                    -Dsonar.junit.reportPaths="target/surefire-reports" \
                    -Dsonar.jacoco.reportPath=target/coverage-reports/jacoco-ut.exec \
        """
    }
}