properties([[$class: 'GitLabConnectionProperty', gitLabConnection: 'gitlab']])
pipeline {
    agent {
        docker 'mzagar/jenkins-slave-jdk-maven-git'
    }
    stages {
        stage('Build') {
            steps {
                sh 'ls'
                sh '''
                    mkdir -p maven_local_repo
                    mvn -Dmaven.repo.local=./maven_local_repo clean package
                '''
            }
        }
    }
    post {
        always {
            archive 'testable-regular/target/report.pdf'
        }
        success {
            updateGitlabCommitStatus name: 'jenkins', state: 'success'
        }
        failure {
            updateGitlabCommitStatus name: 'jenkins', state: 'failed'
        }
        unstable {
            updateGitlabCommitStatus name: 'jenkins', state: 'failed'
        }
    }
}
