pipeline {
    agent any
    options {
        timestamps()
    }
    stages {
        stage('init') {
            steps {
                echo "init start"
                sleep 5
                echo 'init end'
            }
        }
        stage('build') {
            steps {
                parallel 'x86 build': {
                    echo 'x86 build start'
                    sleep 3
                    echo 'x86 build end'
                }, 'arm build': {
                    echo 'arm build start'
                    sleep 3
                    echo 'arm build end'
                }
            }
        }
    }
}
