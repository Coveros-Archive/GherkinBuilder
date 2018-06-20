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
    env.PROJECT = "GherkinBuilder"
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
    stage('Checkout Gherkin Builder') { // for display purposes
        // Get the test code from GitHub repository
        checkout([
            $class           : 'GitSCM',
            branches: [[ name: "*/${branchCheckout}"]],
            userRemoteConfigs: [[
                url          : 'https://github.com/Coveros/GherkinBuilder.git',
                refspec      : "${refspecs}"
            ]]
        ])
    }
    stage('Run Unit Tests') {
        sh "mvn clean test"
    }
    withCredentials([
        string(
            credentialsId: 'sonar-token',
            variable: 'sonartoken'
        ),
        string(
            credentialsId: 'sonar-github',
            variable: 'SONAR_GITHUB_TOKEN'
        )
    ]) {
        stage('Perform SonarQube Analysis') {
            def sonarCmd = "mvn clean compile sonar:sonar -Dsonar.login=${env.sonartoken}"
            if (branchType == 'master') {
                sh "${sonarCmd} -Dsonar.branch=${env.BRANCH_NAME}"
            } else {
                if (pullRequest) {
                    sh "${sonarCmd} -Dsonar.analysis.mode=preview -Dsonar.branch=${env.BRANCH_NAME} -Dsonar.github.pullRequest=${pullRequest} -Dsonar.github.repository=Coveros/${env.PROJECT} -Dsonar.github.oauth=${SONAR_GITHUB_TOKEN}"
                } else {
                    sh "${sonarCmd} -Dsonar.analysis.mode=preview"
                }
            }
        }
    }
}