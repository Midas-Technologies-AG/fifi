def sbt="sbt -Dsbt.log.noformat=true"
def scalaVersion="2.12"

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
    stage('Generate API documentation') {
      steps {
        sh "${sbt} unidoc"
      }
    }
    stage('Generate JAR') {
      steps {
        sh "${sbt} package"
      }
    }
  }
  post {
    always {
      junit '*/target/test-reports/*.xml'
      sh "${sbt} coverageReport"
      sh "${sbt} coverageAggregate"
      step([
        $class: 'ScoveragePublisher',
        reportDir: "target/scala-${scalaVersion}/scoverage-report",
        reportFile: 'scoverage.xml'
      ])
    }

    success {
      archiveArtifacts "*/target/scala-${scalaVersion}/*.jar"
      publishHTML([
        allowMissing: false,
        alwaysLinkToLastBuild: false,
        keepAll: false,
        reportDir: "target/scala-${scalaVersion}/unidoc",
        reportFiles: 'index.html',
        reportName: 'ScalaDoc',
        reportTitles: ''
      ])
    }
  }
}
