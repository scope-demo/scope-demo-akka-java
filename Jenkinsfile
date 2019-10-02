pipeline {
    agent none
    stages {
        stage('Execute Akka Java') {
            agent { docker 'openjdk:8-jdk' }
            steps {
                sh './mvnw clean verify -U -fae'
            }
        }
    }

    post {
        always {
            node('master') {
                archiveArtifacts artifacts: '**/scope_*.log'
                sh 'rm -f scope_*.log'
            }
        }
    }
}