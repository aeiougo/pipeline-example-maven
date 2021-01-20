pipeline {
  agent any
  stages {
    stage('checkout') {
      steps {
        git(credentialsId: 'HiveCoreMaven', changelog: true, branch: 'master', url: 'http://192.168.252.34/SobeyHive/HivePackage.git')
      }
    }

  }
}