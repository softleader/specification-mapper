#!/usr/bin/env groovy

def java17_springBootVersions = ['3.0.13', '3.1.6'] // 只需要放 "非最新" 的中版號的最後一版
def java21_springBootVersions = [] // 只需要放 "非最新" 的中版號的最後一版

pipeline {
  agent {
    kubernetes {
      cloud 'SLKE'
      workspaceVolume persistentVolumeClaimWorkspaceVolume(claimName: 'workspace-claim', readOnly: false)
      defaultContainer 'maven-java17'
      yaml """
kind: Pod
spec:
  # All containers should have the same UID
  securityContext:
    runAsUser: 0
  containers:
  - name: maven-java17
    image: harbor.softleader.com.tw/library/maven:3-eclipse-temurin-17
    imagePullPolicy: Always
    command: ['cat']
    tty: true
    resources:
      limits:
        memory: "1Gi"
        cpu: "2"
    volumeMounts:
    - name: m2
      mountPath: /root/.m2
  - name: maven-java21
    image: harbor.softleader.com.tw/library/maven:3-eclipse-temurin-21
    imagePullPolicy: Always
    command: ['cat']
    tty: true
    resources:
      limits:
        memory: "1Gi"
        cpu: "2"
    volumeMounts:
    - name: m2
      mountPath: /root/.m2
  - name: git
    image: alpine/git:v2.32.0
    command: ['cat']
    tty: true
    resources:
      limits:
        memory: "100Mi"
        cpu: "100m"
  volumes:
  - name: m2
    persistentVolumeClaim:
      claimName: m2-claim
"""
    }
  }

  environment {
    MAVEN_OPTS="-Xmx768m -XX:MaxMetaspaceSize=128m"
  }

  stages {

    stage('Confirm Env') {
      steps {
        container('git') {
          script {
            env.LAST_COMMIT_AUTHOR_NAME = sh(
              script: 'git --no-pager show -s --format=%an',
              returnStdout: true
            ).trim()
            env.LAST_COMMIT_AUTHOR_EMAIL = sh(
              script: 'git --no-pager show -s --format=%ae',
              returnStdout: true
            ).trim()
            env.LAST_COMMIT_TIME = sh(
              script: 'git --no-pager show -s --date=format:"%Y/%m/%d %T" --format=%ad',
              returnStdout: true
            ).trim()
          }
        }
        sh 'printenv'
        sh 'java -version'
        sh 'mvn --version'
        echo "${params}"
      }
    }

    stage('Compile and Style Check') {
      steps {
        sh "make compile"
        container('git') {
          sh '[ ! -z "$(git status -s)" ] && exit 1 || echo "Good to go!"'
        }
      }
    }

    // 用當前 pom.xml 定義的 java, spring 版本執行測試，這個組合也會是 release 時所使用的
    stage('Unit Testing') {
      steps {
        sh "make test"
      }
      post {
        always {
          junit "**/target/surefire-reports/**/*.xml"
        }
      }
    }

    // 執行當前 pom.xml 以外，還支援的 java, spring 版本的交叉測試
    stage('Java 17 Testing') {
      steps {
        script {
            for (int s = 0; s < java17_springBootVersions.size(); s++) {
              def java = 17
              def springboot = java17_springBootVersions[s]
              stage("JAVA = ${java}, SPRING_BOOT = ${springboot}"){
                container("maven-java${java}") {
                  sh "make test JAVA=${java} SPRING_BOOT=${springboot}"
                }
              }
            }
        }
      }
    }

    stage('Java 21 Testing') {
      steps {
        script {
            for (int s = 0; s < java21_springBootVersions.size(); s++) {
              def java = 21
              def springboot = java21_springBootVersions[s]
              stage("JAVA = ${java}, SPRING_BOOT = ${springboot}"){
                container("maven-java${java}") {
                  sh "make test JAVA=${java} SPRING_BOOT=${springboot}"
                }
              }
            }
        }
      }
    }
  }

  post {
    failure {
      script {
        if (env.BRANCH_NAME == 'main'
            // 若短時間太密集的 push, 之前的 job 會被 jenkins 中斷，這樣就可能會就連第一步都還沒執行的狀況，但也算是失敗
            // 然而取得 git 資訊就在第一步，所以至少要第一步都有執行完才發佈 slack 吧
            && env.LAST_COMMIT_AUTHOR_NAME && env.LAST_COMMIT_AUTHOR_EMAIL && env.LAST_COMMIT_TIME) {
          slackSend(
            color: "danger",
            channel: "@matt",
            message: "Attention @here, The pipeline <$BUILD_URL|*${env.JOB_NAME} #${env.BUILD_NUMBER}*> has failed! :omg:\n>Last commit by ${env.LAST_COMMIT_AUTHOR_NAME} (${env.LAST_COMMIT_AUTHOR_EMAIL}) @ ${env.LAST_COMMIT_TIME}"
          )
        }
      }
    }
  }
}
