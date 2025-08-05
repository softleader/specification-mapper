#!/usr/bin/env groovy

// 版號查詢: https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter
// 包含 3.0.x 之後, 每個 minor 版本下的最後一個 patch 版本, 但不需包含最新的 minor 版本
def java17_springBootVersions = ['3.0.13', '3.1.12', '3.2.12', '3.3.13', '3.4.8']
// 包含 3.2.x 之後, 每個 minor 版本下的最後一個 patch 版本, 但不需包含最新的 minor 版本
def java21_springBootVersions = ['3.2.12', '3.3.13', '3.4.8']

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

    stage('Stash Source for Matrix') {
      steps {
        // Stash the source code to be used in parallel stages
        stash name: 'source', includes: '**/*', useDefaultExcludes: true
      }
    }

    // 執行當前 pom.xml 以外，還支援的 java, spring 版本的交叉測試
    stage('Matrix Tests') {
      steps {
        script {
          def jobs = [:]

          jobs["Java 17 Tests"] = {
            dir('java17-tests') {
              unstash 'source'
              def java = 17
              container("maven-java${java}") {
                java17_springBootVersions.each { springboot ->
                  def stageName = "JAVA=${java}, SPRING_BOOT=${springboot}"
                  stage(stageName) {
                    sh "make test JAVA=${java} SPRING_BOOT=${springboot}"
                  }
                }
              }
            }
          }

          jobs["Java 21 Tests"] = {
            dir('java21-tests') {
              unstash 'source'
              def java = 21
              container("maven-java${java}") {
                java21_springBootVersions.each { springboot ->
                  def stageName = "JAVA=${java}, SPRING_BOOT=${springboot}"
                  stage(stageName) {
                    sh "make test JAVA=${java} SPRING_BOOT=${springboot}"
                  }
                }
              }
            }
          }

          parallel jobs
        }
      }
      post {
        always {
          // Clean up the workspace to avoid keeping large subdirectories from the matrix tests
          deleteDir()
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
