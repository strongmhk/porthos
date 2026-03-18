// ============================================================
// Noticore QA CI/CD Pipeline
//
// [실행 환경]
// Jenkins는 모니터링 서버의 Docker 컨테이너에서 실행
// 빌드는 eclipse-temurin:17-jdk-jammy Docker 에이전트 사용
// 배포는 SSH로 별도 QA 서버에 JAR 전송
//
// [Jenkins 컨테이너 필요 조건]
//   - Docker socket 마운트 (/var/run/docker.sock)
//   - 플러그인: Pipeline, Docker Pipeline, Credentials Binding,
//               SSH Agent, Slack Notification
//
// [등록 필요한 Jenkins Credentials]
//   qa-server-host  : Secret Text  (QA 서버 IP 또는 hostname)
//   qa-ssh-key      : SSH Username with private key (ubuntu 계정)
//   slack-token     : Secret Text  (Slack Bot Token)
// ============================================================

pipeline {
    agent any

    triggers {
        // If the job is configured for SCM polling, define it here as well so the behavior
        // is consistent even when the UI trigger isn't set.
        pollSCM('H/5 * * * *')
    }

    environment {
        APP_NAME    = 'noticore'
        // Fallback only; the Build stage auto-detects the real jar name from build/libs.
        JAR_NAME    = 'noticore-0.0.1-SNAPSHOT.jar'
        QA_USER     = 'ubuntu'
        DEPLOY_PATH = '/home/ubuntu/porthos'
        SERVICE     = 'noticore-qa'
        MGMT_PORT   = '8082'
    }

    options {
        disableConcurrentBuilds()
        timeout(time: 20, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
    }

    stages {

        // ----------------------------------------------------------
        // 1. 소스 체크아웃
        // ----------------------------------------------------------
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    // Prefer Jenkins-provided branch vars (works in multibranch),
                    // fall back to git only when needed.
                    def branch = (env.BRANCH_NAME ?: env.GIT_BRANCH ?: '').trim()
                    if (!branch || branch == 'HEAD') {
                        branch = sh(
                            script: "git name-rev --name-only --no-undefined HEAD 2>/dev/null | sed 's#^remotes/##'",
                            returnStdout: true
                        ).trim()
                    }
                    env.GIT_BRANCH = (branch ?: 'unknown').replaceFirst(/^origin\\//, '')
                }
                echo "Branch: ${env.GIT_BRANCH} | Build: #${env.BUILD_NUMBER}"
            }
        }

        // ----------------------------------------------------------
        // 2. 빌드 (Java 17 Docker 에이전트)
        //    reuseNode true → Checkout 워크스페이스 공유
        //    gradle-cache 볼륨 → 의존성 캐시 재사용
        // ----------------------------------------------------------
        stage('Build') {
            agent {
                docker {
                    image 'eclipse-temurin:17-jdk-jammy'
                    reuseNode true
                    args "--entrypoint='' --network host -v gradle-cache:/root/.gradle"
                }
            }
            steps {
                sh './gradlew clean build -x test --no-daemon'
                // Make artifact availability explicit and resilient across nodes/workspaces.
                script {
                    sh '''
                        echo "Build outputs:"
                        ls -la build || true
                        ls -la build/libs || true
                    '''

                    // Prefer the non-plain jar (Spring Boot executable) and fall back gracefully.
                    def detectedJar = sh(
                        script: 'ls -1 build/libs/*.jar 2>/dev/null | grep -v -E \'(-plain\\.jar$|plain\\.jar$)\' | head -n 1 || true',
                        returnStdout: true
                    ).trim()
                    if (!detectedJar) {
                        detectedJar = sh(
                            script: "ls -1 build/libs/*.jar 2>/dev/null | head -n 1 || true",
                            returnStdout: true
                        ).trim()
                    }
                    if (!detectedJar) {
                        error("No jar produced under build/libs/*.jar")
                    }

                    env.JAR_NAME = detectedJar.tokenize('/').last()
                    echo "Detected jar: ${env.JAR_NAME}"
                    sh "test -f build/libs/${env.JAR_NAME}"
                }

                stash name: 'qa-artifacts', includes: "build/libs/${env.JAR_NAME},Dockerfile"
            }
        }

        // ----------------------------------------------------------
        // 3. 테스트 (Java 17 Docker 에이전트)
        // ----------------------------------------------------------
        stage('Test') {
            agent {
                docker {
                    image 'eclipse-temurin:17-jdk-jammy'
                    reuseNode true
                    args "--entrypoint='' --network host -v gradle-cache:/root/.gradle"
                }
            }
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    sh './gradlew test --no-daemon'
                }
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'build/test-results/**/*.xml'
                }
            }
        }

        // ----------------------------------------------------------
        // 4. QA 서버 배포 (develop 브랜치만)
        //    reuseNode true 덕분에 Build 스테이지가 만든 JAR가
        //    Jenkins 에이전트 워크스페이스에 그대로 남아있음
        // ----------------------------------------------------------
        stage('Deploy to QA') {
            when {
                expression {
                    def b = (env.BRANCH_NAME ?: env.GIT_BRANCH ?: '').trim()
                    return b == 'develop' || b == 'origin/develop' || b == 'refs/heads/develop'
                }
            }
            steps {
                // Ensure artifacts exist even if this stage runs on a different workspace/node.
                unstash 'qa-artifacts'
                withCredentials([
                    string(credentialsId: 'qa-server-host', variable: 'QA_HOST'),
                    string(credentialsId: 'QA_WAS_SSH_PORT', variable: 'QA_SSH_PORT'),
                    sshUserPrivateKey(credentialsId: 'qa-ssh-key', keyFileVariable: 'SSH_KEY')
                ]) {
                    sh """
                        echo "Artifact check"
                        ls -la build/libs || true
                        ls -la Dockerfile || true

                        echo "[1/3] JAR + Dockerfile 전송"
                        scp -P \${QA_SSH_PORT} -i \${SSH_KEY} -o StrictHostKeyChecking=no \\
                            build/libs/${JAR_NAME} \\
                            ${QA_USER}@\${QA_HOST}:${DEPLOY_PATH}/app.jar
                        scp -P \${QA_SSH_PORT} -i \${SSH_KEY} -o StrictHostKeyChecking=no \\
                            Dockerfile \\
                            ${QA_USER}@\${QA_HOST}:${DEPLOY_PATH}/Dockerfile

                        echo "[2/3] Docker 이미지 빌드"
                        ssh -p \${QA_SSH_PORT} -i \${SSH_KEY} -o StrictHostKeyChecking=no \\
                            ${QA_USER}@\${QA_HOST} \\
                            "docker build -t ${SERVICE}:latest ${DEPLOY_PATH}"

                        echo "[3/3] 컨테이너 재시작"
                        ssh -p \${QA_SSH_PORT} -i \${SSH_KEY} -o StrictHostKeyChecking=no \\
                            ${QA_USER}@\${QA_HOST} \\
                            "docker stop ${SERVICE} 2>/dev/null || true && \\
                             docker rm ${SERVICE} 2>/dev/null || true && \\
                             docker run -d \\
                               --name ${SERVICE} \\
                               --restart unless-stopped \\
                               --network host \\
                               -e SPRING_PROFILES_ACTIVE=qa \\
                               -e SPRING_DATASOURCE_URL='jdbc:mysql://localhost:13307/porthos?characterEncoding=UTF-8&serverTimezone=Asia/Seoul&autoReconnect=true&rewriteBatchedStatements=true' \\
                               -e SPRING_DATASOURCE_USERNAME=porthos \\
                               -e SPRING_DATASOURCE_PASSWORD=porthos123! \\
                               -e SPRING_DATA_REDIS_HOST=localhost \\
                               -e SPRING_DATA_REDIS_PORT=16379 \\
                               -e SPRING_DATA_REDIS_USERNAME='' \\
                               -e SPRING_DATA_REDIS_SSL_ENABLED=false \\
                               ${SERVICE}:latest"
                    """
                }
            }
        }

        // ----------------------------------------------------------
        // 5. 헬스 체크 (develop 브랜치만)
        //    10초 간격으로 최대 6회(60초) 재시도
        //    /actuator/health 응답에 "UP" 포함 여부로 판단
        // ----------------------------------------------------------
        stage('Health Check') {
            when {
                expression {
                    def b = (env.BRANCH_NAME ?: env.GIT_BRANCH ?: '').trim()
                    return b == 'develop' || b == 'origin/develop' || b == 'refs/heads/develop'
                }
            }
            steps {
                withCredentials([
                    string(credentialsId: 'qa-server-host', variable: 'QA_HOST'),
                    string(credentialsId: 'QA_WAS_SSH_PORT', variable: 'QA_SSH_PORT'),
                    sshUserPrivateKey(credentialsId: 'qa-ssh-key', keyFileVariable: 'SSH_KEY')
                ]) {
                    retry(6) {
                        sleep time: 10, unit: 'SECONDS'
                        sh """
                            ssh -p \${QA_SSH_PORT} -i \${SSH_KEY} -o StrictHostKeyChecking=no \\
                                ${QA_USER}@\${QA_HOST} \\
                                "curl -sf http://localhost:${MGMT_PORT}/actuator/health | grep -q UP"
                        """
                    }
                }
            }
        }
    }

    // ----------------------------------------------------------
    // 알림 (develop 브랜치 배포 결과만 Slack 전송)
    // ----------------------------------------------------------
    post {
        success {
            script {
                if (env.GIT_BRANCH == 'develop') {
                    slackSend(
                        channel: '#deploy-log',
                        color: 'good',
                        tokenCredentialId: 'slack-token',
                        message: "✅ *${APP_NAME}* QA 배포 성공\n" +
                                 "Branch: `${env.GIT_BRANCH}` | Build: `#${env.BUILD_NUMBER}`\n" +
                                 "${env.BUILD_URL}"
                    )
                }
            }
        }
        failure {
            script {
                if (env.GIT_BRANCH == 'develop') {
                    slackSend(
                        channel: '#deploy-log',
                        color: 'danger',
                        tokenCredentialId: 'slack-token',
                        message: "❌ *${APP_NAME}* QA 배포 실패\n" +
                                 "Branch: `${env.GIT_BRANCH}` | Build: `#${env.BUILD_NUMBER}`\n" +
                                 "${env.BUILD_URL}"
                    )
                }
            }
        }
        always {
            cleanWs()
        }
    }
}
