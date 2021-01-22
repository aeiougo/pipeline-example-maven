pipeline {
  agent any
  stages {
    stage('init') {
      steps {
        echo 'init start'
        sleep 5
        echo 'init end'
      }
    }

    stage('build') {
      parallel {
        stage('x86 build') {
          steps {
            echo 'x86 build start'
            sleep 3
            echo 'x86 build end'
          }
        }

        stage('arm build') {
          steps {
            echo 'arm build start'
            sleep 3
            echo 'arm build end'
          }
        }

      }
    }

  }
  options {
    timestamps()
  }
}