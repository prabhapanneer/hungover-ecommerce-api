node {
  stage('SCM') {
    checkout scm
  }
  stage('SonarQube Analysis') {
    withSonarQubeEnv() {
      sh 'chmod +x gradlew' // Add this line to make gradlew executable
      sh './gradlew clean test jacocoTestReport'
      sh './gradlew  sonarqube'
    }
  }
}