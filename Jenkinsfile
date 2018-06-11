def sbt="sbt -Dsbt.log.noformat=true"

pipeline {
  agent any
  stages {
    stage('Clean') {
      steps {
        sh "${sbt} clean"
      }
    }
    stage('Test') {
      environment {
        AWS_ACCESS_KEY_ID = credentials('service-discovery_tests_aws-access-key')
        AWS_SECRET_ACCESS_KEY = credentials('service-discovery_tests_aws-secret-access-key')
      }
      steps {
        sh "${sbt} coverage test"
      }
    }
  }
}