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
    MAVEN_OPTS = "-Xmx768m -XX:MaxMetaspaceSize=128m"
  }

  stages {
    stage('Confirm Env') {
      steps {
        container('git') {
          script {
            env.LAST_COMMIT_AUTHOR_NAME = sh(script: 'git --no-pager show -s --format=%an', returnStdout: true).trim()
            env.LAST_COMMIT_AUTHOR_EMAIL = sh(script: 'git --no-pager show -s --format=%ae', returnStdout: true).trim()
            env.LAST_COMMIT_TIME = sh(script: 'git --no-pager show -s --date=format:"%Y/%m/%d %T" --format=%ad', returnStdout: true).trim()
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

    stage('Matrix Java + Spring Boot Testing') {
      parallel {
        script {
          def matrixJobs = [:]

          def generateJob = { javaVersion, springVersion ->
            def label = "JAVA=${javaVersion}, SPRING_BOOT=${springVersion}"
            matrixJobs[label] = {
              stage(label) {
                agent {
                  kubernetes {
                    cloud 'SLKE'
                    defaultContainer 'maven'
                    yaml """
kind: Pod
spec:
  securityContext:
    runAsUser: 0
  containers:
  - name: maven
    image: harbor.softleader.com.tw/library/maven:3-eclipse-temurin-${javaVersion}
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
  volumes:
  - name: m2
    persistentVolumeClaim:
      claimName: m2-claim
"""
                  }
                }
                steps {
                  sh "make test JAVA=${javaVersion} SPRING_BOOT=${springVersion}"
                }
              }
            }
          }

          java17_springBootVersions.each { generateJob(17, it) }
          java21_springBootVersions.each { generateJob(21, it) }

          parallel matrixJobs
        }
      }
    }
  }

  post {
    failure {
      script {
        if (env.BRANCH_NAME == 'main'
          && env.LAST_COMMIT_AUTHOR_NAME
          && env.LAST_COMMIT_AUTHOR_EMAIL
          && env.LAST_COMMIT_TIME) {
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
