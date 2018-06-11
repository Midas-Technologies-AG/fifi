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
    stage('Publish test reports') {
      steps {
        junit '*/target/test-reports/*.xml'
        sh "${sbt} coverageReport"
        sh "${sbt} coverageAggregate"
        step([$class: 'ScoveragePublisher', reportDir: 'target/scala-2.12/scoverage-report', reportFile: 'scoverage.xml'])
      }
    }
    stage('Generate and publish API documentation') {
      steps {
        sh "${sbt} doc"
        publishHTML([
          allowMissing: false,
          alwaysLinkToLastBuild: false,
          keepAll: false,
          reportDir: 'aws-wrapper/target/scala-2.12/api',
          reportFiles: 'index.html',
          reportName: 'ScalaDoc aws-wrapper',
          reportTitles: ''
        ])
        publishHTML([
          allowMissing: false,
          alwaysLinkToLastBuild: false,
          keepAll: false,
          reportDir: 'discovery-aws/target/scala-2.12/api',
          reportFiles: 'index.html',
          reportName: 'ScalaDoc discovery-aws',
          reportTitles: ''
        ])
      }
    }
  }
}