def fun_checkout(){
    dir("${env.WORKSPACE}/FTEngine2"){
        checkout([$class: 'GitSCM',
                  branches: [[name: '*/master']],
                  doGenerateSubmoduleConfigurations: false,
                  extensions: [],
                  gitTool: 'Default',
                  submoduleCfg: [],
                  userRemoteConfigs: [[url: "${env.GITLAB_URL_FT}",credentialsId: "${env.GITLAB_TOKEN}",]]
                ])
    }
    dir("${env.WORKSPACE}/HivePackage"){
        checkout([$class: 'GitSCM',
                  branches: [[name: '*/master']],
                  doGenerateSubmoduleConfigurations: false,
                  extensions: [],
                  gitTool: 'Default',
                  submoduleCfg: [],
                  userRemoteConfigs: [[url: "${env.GITLAB_URL_PKG}",credentialsId: "${env.GITLAB_TOKEN}",]]
                ])
    }
}

def fun_git(){
    dir("${env.WORKSPACE}/FTEngine2"){
        git branch: 'master', credentialsId: "${env.GITLAB_TOKEN}", url: "${env.GITLAB_URL_FT}"
    }
    dir("${env.WORKSPACE}/HivePackage"){
        git branch: 'master', credentialsId: "${env.GITLAB_TOKEN}", url: "${env.GITLAB_URL_PKG}"
    }
}

def fun_code_review(){
    withSonarQubeEnv(installationName: 'sonarqube', credentialsId: 'global-sonarqube-token') {
        dir("${env.WORKSPACE}/FTEngine2"){
            container('maven') {
                sh "mvn sonar:sonar -Dsonar.java.binaries=target/ -Dsonar.projectKey=Devops-SobeyCloud-Toolkit"
            }
        }
    }
}

def fun_build(){
    dir("${env.WORKSPACE}/FTEngine2"){
        container('maven-jdk-8') {
            sh "mvn clean install -DskipTests"
        }
    }
}

def fun_build_image(){
    dir("${env.WORKSPACE}/DockerBuild"){
        container('devops-tools') {
            sh """
            cp -r ${env.WORKSPACE}/HivePackage/Hive-2.0/docker-library/business/ftengine2 ./${env.APP_NAME_FTENGINE2}
            mkdir -p ./${env.APP_NAME_FTENGINE2}/webapps
            cp ${env.WORKSPACE}/FTEngine2/FT-Web/target/ftengine-web-2.0.war ./${env.APP_NAME_FTENGINE2}/webapps/ftengine.war
            cp ${env.WORKSPACE}/FTEngine2/FT-Solr/target/ftengine-solr-2.0.war ./${env.APP_NAME_FTENGINE2}/webapps/ftsolr.war
            cd ${env.APP_NAME_FTENGINE2}
            sed -i 's#^FROM.*#FROM '${env.DOCKER_HUB_URL}'/'${env.PROJECT_NAME}'/tomcat:8-jre8-alpine-hive-'${env.BUILD_PLATFORM}'#' Dockerfile
            docker build -t ${env.APP_NAME_FTENGINE2}:${env.DOCKER_IMAGE_TAG_FTENGINE2} .
            """
        }
    }
}

def fun_package(){
    dir("${env.WORKSPACE}/Package"){
        container('devops-tools') {
            sh """
            mkdir -p ${env.APP_NAME_FTENGINE2}_template
            cp -r ${env.WORKSPACE}/HivePackage/Hive-2.0/scripts/ftengine2 ./${env.APP_NAME_FTENGINE2}_template/${env.APP_NAME_FTENGINE2}-${env.DOCKER_IMAGE_TAG_FTENGINE2}
            cd ${env.APP_NAME_FTENGINE2}_template/${env.APP_NAME_FTENGINE2}-${env.DOCKER_IMAGE_TAG_FTENGINE2}
            docker save ${env.APP_NAME_FTENGINE2}:${env.DOCKER_IMAGE_TAG_FTENGINE2} | gzip -c > ${env.APP_NAME_FTENGINE2}-${env.DOCKER_IMAGE_TAG_FTENGINE2}-${env.BUILD_PLATFORM}.tar.gz
            docker rmi ${env.APP_NAME_FTENGINE2}:${env.DOCKER_IMAGE_TAG_FTENGINE2}
            """
            script {
                ALL_PRODUCTS = ['mch-2.x','mch-4k','smart-tag','pyramid','mbh-1.x','mbh-cloud','mbh-srg','mbh-saas']
                for(product in ALL_PRODUCTS){
                    sh """
                    cp -r ${env.APP_NAME_FTENGINE2}_template ${env.APP_NAME_FTENGINE2}
                    cd ${env.APP_NAME_FTENGINE2}/${env.APP_NAME_FTENGINE2}-${env.DOCKER_IMAGE_TAG_FTENGINE2}
                    cp -r ${env.WORKSPACE}/HivePackage/Hive-2.0/products/${product}/configs/ftengine2/sobeyhive_config ./
                    cd ..
                    tar -czf ${env.APP_NAME_FTENGINE2}-${env.DOCKER_IMAGE_TAG_FTENGINE2}.tar.gz ${env.APP_NAME_FTENGINE2}-${env.DOCKER_IMAGE_TAG_FTENGINE2}
                    md5sum ${env.APP_NAME_FTENGINE2}-${env.DOCKER_IMAGE_TAG_FTENGINE2}.tar.gz > ${env.APP_NAME_FTENGINE2}-${env.DOCKER_IMAGE_TAG_FTENGINE2}.tar.gz.md5
                    rm -rf ${env.APP_NAME_FTENGINE2}-${env.DOCKER_IMAGE_TAG_FTENGINE2}
                    cd ..
                    mkdir -p /Releases/Hive-2.0/${env.BUILD_PLATFORM}/${product}
                    rm -rf /Releases/Hive-2.0/${env.BUILD_PLATFORM}/${product}/${env.APP_NAME_FTENGINE2}
                    cp -r ${env.APP_NAME_FTENGINE2} /Releases/Hive-2.0/${env.BUILD_PLATFORM}/${product}/${env.APP_NAME_FTENGINE2}
                    mkdir -p /Archives/Hive-2.0/${env.BUILD_PLATFORM}/${product}/${env.APP_NAME_FTENGINE2}
                    cp -r ${env.APP_NAME_FTENGINE2} /Archives/Hive-2.0/${env.BUILD_PLATFORM}/${product}/${env.APP_NAME_FTENGINE2}/${env.DOCKER_IMAGE_TAG_FTENGINE2}
                    rm -rf ${env.APP_NAME_FTENGINE2}
                    """
                }
            }
        }
    }
}

def fun_deploy_to_test(){
    wrap([$class: 'BuildUser']) {
        dingtalk (
            robot: '55492183-f968-48cb-874f-c5e59d8ece74',
            type: 'MARKDOWN',
            title: '审批流程',
            text: [
                "### 部署审批",
                "> - 任务名称：${JOB_NAME}",
                "> - 部署平台：${params.Platform}",
                "> - 部署产品：${params.Product}",
                "> - 部署版本：${env.DOCKER_IMAGE_TAG}",
                "> - 部署环境：${env.DEPLOY_URL}",
                "> - 部署发起：${BUILD_USER}",
                "> - 审批流程：[点击进入审批页面](${JENKINS_URL}blue/organizations/jenkins/${JOB_NAME}/detail/${JOB_NAME}/${BUILD_NUMBER}/pipeline)",
            ],
            at: [
              '15202872754',
              '15184323306'
            ]
        )
    }
    script {
        input(
            message: "是否将 ${env.APP_NAME_FTENGINE2} 部署到 ${env.DEPLOY_URL} 测试环境？", ok: "Yes", submitter: "devops", submitterParameter: "APPROVER"
        )
        withCredentials([usernamePassword(credentialsId: "${env.DEPLOY_CREDENTIAL}", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
            container('devops-tools') {
                sh """
                sshpass -p ${PASSWORD} ssh -o StrictHostKeyChecking=no ${USERNAME}@${env.DEPLOY_URL} "rm -rf /tmp/${env.APP_NAME_FTENGINE2}"
                sshpass -p ${PASSWORD} scp -r -o StrictHostKeyChecking=no /Releases/Hive-2.0/${env.BUILD_PLATFORM}/${params.Product}/${env.APP_NAME_FTENGINE2} ${USERNAME}@${env.DEPLOY_URL}:/tmp/${env.APP_NAME_FTENGINE2}
                sshpass -p ${PASSWORD} scp -r -o StrictHostKeyChecking=no ${env.WORKSPACE}/HivePackage/Hive-2.0/scripts/upgrade_app_cluster_onekey.sh ${USERNAME}@${env.DEPLOY_URL}:/tmp/${env.APP_NAME_FTENGINE2}
                sshpass -p ${PASSWORD} ssh -o StrictHostKeyChecking=no ${USERNAME}@${env.DEPLOY_URL} "/bin/bash /tmp/${env.APP_NAME_FTENGINE2}/upgrade_app_cluster_onekey.sh"
                """
            }
        }
    }
}

def fun_upload_to_ftp(){
    wrap([$class: 'BuildUser']) {
        dingtalk (
            robot: '55492183-f968-48cb-874f-c5e59d8ece74',
            type: 'MARKDOWN',
            title: '审批流程',
            text: [
                "### 上传审批",
                "> - 任务名称：${JOB_NAME}",
                "> - 上传平台：${params.Platform}",
                "> - 上传产品：mch-2.x",
                "> - 上传版本：${env.DOCKER_IMAGE_TAG}",
                "> - 上传地址：ftp.sobey.com",
                "> - 上传发起：${BUILD_USER}",
                "> - 审批流程：[点击进入审批页面](${JENKINS_URL}blue/organizations/jenkins/${JOB_NAME}/detail/${JOB_NAME}/${BUILD_NUMBER}/pipeline)",
            ],
            at: [
              '15202872754',
              '15184323306'
            ]
        )
    }
    script {
        input(
            message: "是否将 ${env.APP_NAME_FTENGINE2} 上传到 ftp.sobey.com？", ok: "Yes", submitter: "devops", submitterParameter: "APPROVER"
        )
        container('devops-tools') {
            sh """
            lftp -c "mirror -R /Releases/Hive-2.0/${env.BUILD_PLATFORM}/mch-2.x/${env.APP_NAME_FTENGINE2} ftp://songqiuling:TSuNzGWrjP@ftp.sobey.com/广电/产品版本/β版本/MCH/MCH_2.0_研发补丁/【app_src】_linux端/hive/${env.APP_NAME_FTENGINE2}/${env.DOCKER_IMAGE_TAG_FTENGINE2}"
            """
        }
    }
}

pipeline {
    parameters{
        choice(name: 'Platform', choices: ['All','linux-amd64','linux-arm64'],description: '选择平台')
        choice(name: 'Product', choices: ['mch-2.x','mch-4k','smart-tag','pyramid','mbh-1.x','mbh-cloud','mbh-srg','mbh-saas'],description: '选择产品（当勾选 “部署到测试环境” 时，署到对应产品测试环境）')
        booleanParam(name: 'CodeReview', defaultValue: false, description: '代码检查')
        booleanParam(name: 'Package', defaultValue: true, description: '制作安装包（\\\\172.16.148.248\\Releases\\Hive-2.0）')
        booleanParam(name: 'Deploy', defaultValue: false, description: '部署到测试环境（部署到 “选择产品” 对应的测试环境）')
        booleanParam(name: 'UPloadToFTP', defaultValue: false, description: '上传到 ftp 服务器（仅上传产品 mch-2.x）')
    }
    environment {
        CURRENT_TIMESTAMP = new Date().format('yyMMddHHmm')
        //DATE = new Date().format('yyyyMMddHHmm')

        APP_NAME_FTENGINE2 = "ftengine2"

        APP_VERSION = '1.0'
        DOCKER_IMAGE_TAG = "${env.APP_VERSION}.${env.CURRENT_TIMESTAMP}"
        DOCKER_IMAGE_TAG_FTENGINE2 = "${env.DOCKER_IMAGE_TAG}"

        //DOCKER_HUB_URL = "172.16.148.233"
        PROJECT_NAME="hive"
        //DOCKER_HUB_CREDENTIAL = 'DockerHub-233'

        GITLAB_TOKEN = 'HiveCoreMaven'
        GITLAB_URL_FT = 'http://192.168.252.34/SobeyHive/FTEngine2.git'
        GITLAB_URL_PKG = 'http://192.168.252.34/SobeyHive/HivePackage.git'

        DEPLOY_URL_230 = "172.16.148.230"
        DEPLOY_CREDENTIAL_230 = "Root-230"

        DEPLOY_URL_238 = "172.16.148.238"
        DEPLOY_CREDENTIAL_238 = "Root-238"

        DEPLOY_URL_TMP_A = "${params.Product == "mch-2.x" ? "${env.DEPLOY_URL_230}" : 'null'}"
        DEPLOY_URL_TMP_B = "${params.Product == "mch-4k" ? "${env.DEPLOY_URL_230}" : "${env.DEPLOY_URL_TMP_A}"}"
        DEPLOY_URL_TMP_C = "${params.Product[0..2] == "mbh" ? "${env.DEPLOY_URL_238}" : "${env.DEPLOY_URL_TMP_B}"}"
        DEPLOY_URL = "${env.DEPLOY_URL_TMP_C}"

        DEPLOY_CREDENTIAL_TMP_A = "${params.Product == "mch-2.x" ? "${env.DEPLOY_CREDENTIAL_230}" : 'null'}"
        DEPLOY_CREDENTIAL_TMP_B = "${params.Product == "mch-4k" ? "${env.DEPLOY_CREDENTIAL_230}" : "${env.DEPLOY_CREDENTIAL_TMP_A}"}"
        DEPLOY_CREDENTIAL_TMP_C = "${params.Product[0..2] == "mbh" ? "${env.DEPLOY_CREDENTIAL_238}" : "${env.DEPLOY_CREDENTIAL_TMP_B}"}"
        DEPLOY_CREDENTIAL = "${env.DEPLOY_CREDENTIAL_TMP_C}"
    }

    agent any
	options {
		buildDiscarder(logRotator(numToKeepStr: '10'))
		disableConcurrentBuilds()
		timeout(time: 24, unit: 'HOURS')
        timestamps()
	}

    stages {
        stage('build') {
            failFast true
            parallel {
                stage('AMD64') {
                    when {
                        anyOf {
                            environment name: 'Platform', value: 'All'
                            environment name: 'Platform', value: 'linux-amd64'
                        }
                    }
                    environment {
                        BUILD_PLATFORM = 'linux-amd64'
                    }
                    agent {
                    kubernetes {
                        cloud "kubernetes-linux-amd64"
                        defaultContainer 'jnlp'
                        yaml """
                apiVersion: v1
                kind: Pod
                spec:
                  containers:
                  - name: jnlp
                    image: ${env.DOCKER_HUB_URL}/hive/inbound-agent:4.3-8-linux-amd64
                    imagePullPolicy: "IfNotPresent"
                  - name: maven-jdk-8
                    image: ${env.DOCKER_HUB_URL}/hive/maven:3.6.3-adoptopenjdk-8-linux-amd64
                    imagePullPolicy: "IfNotPresent"
                    resources:
                      requests:
                        cpu: "100m"
                        memory: "256Mi"
                    command:
                    - "/bin/sh"
                    - "-c"
                    args:
                    - "cat"
                    tty: true
                    volumeMounts:
                    - mountPath: "/root/.m2"
                      name: "volume-1"
                      readOnly: false
                  - name: devops-tools
                    image: ${env.DOCKER_HUB_URL}/hive/devops-tools:1.0.0-linux-amd64
                    imagePullPolicy: "IfNotPresent"
                    command:
                    - "/bin/sh"
                    - "-c"
                    args:
                    - "cat"
                    tty: true
                    volumeMounts:
                    - mountPath: "/var/run/docker.sock"
                      name: "volume-0"
                      readOnly: true
                    - mountPath: "/Releases"
                      name: "volume-2"
                      readOnly: false
                    - mountPath: "/Archives"
                      name: "volume-3"
                      readOnly: false
                  volumes:
                  - name: "volume-0"
                    hostPath:
                      path: "/var/run/docker.sock"
                      readOnly: true
                  - name: "volume-1"
                    nfs:
                      server: "172.16.148.233"
                      path: "/share/devops/maven/Hive2.0-linux-amd64"
                      readOnly: false
                  - name: "volume-2"
                    nfs:
                      server: "172.16.148.248"
                      path: "/share/Releases"
                      readOnly: false
                  - name: "volume-3"
                    nfs:
                      server: "172.16.148.248"
                      path: "/share/Archives"
                      readOnly: false
                """
                        }
                    }
                    stages {
                        stage('源码更新'){
                            steps {
                                fun_git()
                            }
                        }

                        stage('源码检查') {
                            when {
                                expression { return params.CodeReview }
                            }
                            steps {
                                fun_code_review()
                            }
                        }

                        stage('源码编译') {
                            when {
                                environment name: 'Platform', value: "${params.Platform}"
                            }
                            steps {
                                fun_build()
                            }
                        }

                        stage('构建镜像') {
                            when {
                                environment name: 'Platform', value: "${params.Platform}"
                            }
                            steps {
                                fun_build_image()
                            }
                        }

                        stage('制作安装包') {
                            when {
                                expression { return params.Package }
                                environment name: 'Platform', value: "${params.Platform}"
                            }
                            steps {
                                fun_package()
                            }
                        }

                        stage('部署测试') {
                            when {
                                expression { return params.Deploy }
                            }
                            options {
                                timeout(time: 12, unit: 'HOURS')
                            }
                            steps {
                                fun_deploy_to_test()
                            }
                        }

                        stage('上传 FTP') {
                            when {
                                expression { return params.UPloadToFTP }
                            }
                            steps {
                                fun_upload_to_ftp()
                            }
                        }
                    }
                }
                stage('ARM64') {
                    when {
                        anyOf {
                            environment name: 'Platform', value: 'All'
                            environment name: 'Platform', value: 'linux-arm64'
                        }
                    }
                    environment {
                        BUILD_PLATFORM = 'linux-arm64'
                    }
                    agent {
                    kubernetes {
                        cloud "kubernetes-linux-arm64"
                        defaultContainer 'jnlp'
                        yaml """
                apiVersion: v1
                kind: Pod
                spec:
                  containers:
                  - name: jnlp
                    image: ${env.DOCKER_HUB_URL}/hive/inbound-agent:4.3-8-linux-arm64
                    imagePullPolicy: "IfNotPresent"
                  - name: maven-jdk-8
                    image: ${env.DOCKER_HUB_URL}/hive/maven:3.6.3-adoptopenjdk-8-linux-arm64
                    imagePullPolicy: "IfNotPresent"
                    resources:
                      requests:
                        cpu: "100m"
                        memory: "256Mi"
                    command:
                    - "/bin/sh"
                    - "-c"
                    args:
                    - "cat"
                    tty: true
                    volumeMounts:
                    - mountPath: "/root/.m2"
                      name: "volume-1"
                      readOnly: false
                  - name: devops-tools
                    image: ${env.DOCKER_HUB_URL}/hive/devops-tools:1.0.0-linux-arm64
                    imagePullPolicy: "IfNotPresent"
                    command:
                    - "/bin/sh"
                    - "-c"
                    args:
                    - "cat"
                    tty: true
                    volumeMounts:
                    - mountPath: "/var/run/docker.sock"
                      name: "volume-0"
                      readOnly: true
                    - mountPath: "/Releases"
                      name: "volume-2"
                      readOnly: false
                    - mountPath: "/Archives"
                      name: "volume-3"
                      readOnly: false
                  volumes:
                  - name: "volume-0"
                    hostPath:
                      path: "/var/run/docker.sock"
                      readOnly: true
                  - name: "volume-1"
                    nfs:
                      server: "172.16.148.233"
                      path: "/share/devops/maven/Hive2.0-linux-arm64"
                      readOnly: false
                  - name: "volume-2"
                    nfs:
                      server: "172.16.148.248"
                      path: "/share/Releases"
                      readOnly: false
                  - name: "volume-3"
                    nfs:
                      server: "172.16.148.248"
                      path: "/share/Archives"
                      readOnly: false
                """
                        }
                    }
                    stages {
                        stage('源码更新'){
                            steps {
                                fun_git()
                            }
                        }

                        stage('源码检查') {
                            when {
                                expression { return params.CodeReview }
                            }
                            steps {
                                fun_code_review()
                            }
                        }

                        stage('源码编译') {
                            when {
                                environment name: 'Platform', value: "${params.Platform}"
                            }
                            steps {
                                fun_build()
                            }
                        }

                        stage('构建镜像') {
                            when {
                                environment name: 'Platform', value: "${params.Platform}"
                            }
                            steps {
                                fun_build_image()
                            }
                        }

                        stage('制作安装包') {
                            when {
                                expression { return params.Package }
                                environment name: 'Platform', value: "${params.Platform}"
                            }
                            steps {
                                fun_package()
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            wrap([$class: 'BuildUser']) {
                dingtalk (
                    robot: '55492183-f968-48cb-874f-c5e59d8ece74',
                    type: 'MARKDOWN',
                    title: '项目构建信息',
                    text: [
                        "### 构建信息",
                        "> - 任务名称：${JOB_NAME}",
                        "> - 构建平台：${params.Platform}",
                        "> - 构建版本：${DOCKER_IMAGE_TAG}",
                        "> - 构建发起：${BUILD_USER}",
                        "> - 持续时间：${currentBuild.durationString.replace(' and counting', '')}",
                        "> - 构建结果：<font color=#52c41a>成功</font>",
                        "> - 构建日志：[点击查看详情](${JENKINS_URL}blue/organizations/jenkins/${JOB_NAME}/detail/${JOB_NAME}/${BUILD_NUMBER}/pipeline)"
                    ]
                )
            }
        }
        failure {
            wrap([$class: 'BuildUser']) {
                dingtalk (
                    robot: '55492183-f968-48cb-874f-c5e59d8ece74',
                    type: 'MARKDOWN',
                    title: '项目构建信息',
                    text: [
                        "### 构建信息",
                        "> - 任务名称：${JOB_NAME}",
                        "> - 构建平台：${params.Platform}",
                        "> - 构建版本：${DOCKER_IMAGE_TAG}",
                        "> - 构建发起：${BUILD_USER}",
                        "> - 持续时间：${currentBuild.durationString.replace(' and counting', '')}",
                        "> - 构建结果：<font color=#f5222d>失败</font>",
                        "> - 构建日志：[点击查看详情](${JENKINS_URL}blue/organizations/jenkins/${JOB_NAME}/detail/${JOB_NAME}/${BUILD_NUMBER}/pipeline)",
                        //"> - 更新记录："
                    ]
                )
            }
        }
        aborted {
            wrap([$class: 'BuildUser']) {
                dingtalk (
                    robot: '55492183-f968-48cb-874f-c5e59d8ece74',
                    type: 'MARKDOWN',
                    title: '项目构建信息',
                    text: [
                        "### 构建信息",
                        "> - 任务名称：${JOB_NAME}",
                        "> - 构建平台：${params.Platform}",
                        "> - 构建版本：${DOCKER_IMAGE_TAG}",
                        "> - 构建发起：${BUILD_USER}",
                        "> - 持续时间：${currentBuild.durationString.replace(' and counting', '')}",
                        "> - 构建结果：<font color=#13c2c2>取消</font>",
                        "> - 构建日志：[点击查看详情](${JENKINS_URL}blue/organizations/jenkins/${JOB_NAME}/detail/${JOB_NAME}/${BUILD_NUMBER}/pipeline)"
                    ]
                )
            }
        }
    }
}
