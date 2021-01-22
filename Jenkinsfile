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
            parallel {
                stage('x86 build') {
                    agent {
                        label 'master'
                    }
                    steps {
                        echo 'x86 build start'
                        sleep 5
                        echo 'x86 build end'
                    }
                }
                stage('arm build') {
                    stages {
                        stage('arm-master build') {
                            steps {
                                echo 'arm master build start'
                                sleep 3
                                echo 'arm master build end'
                            }
                        }
                        stage('arm develop build') {
                            steps {
                                echo 'arm develop build start'
                                sleep 3
                                echo 'arm develop build end'
                            }
                        }
                    }
                }
            }
        }
    }
}
