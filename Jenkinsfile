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

    environment {
        APP_NAME    = 'noticore'
        JAR_NAME    = 'porthos-0.0.1-SNAPSHOT.jar'
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
                echo "Branch: ${env.BRANCH_NAME} | Build: #${env.BUILD_NUMBER}"
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
                sh './gradlew test --no-daemon'
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
            when { branch 'develop' }
            steps {
                withCredentials([
                    string(credentialsId: 'qa-server-host', variable: 'QA_HOST'),
                    sshUserPrivateKey(credentialsId: 'qa-ssh-key', keyFileVariable: 'SSH_KEY')
                ]) {
                    sh """
                        echo "[1/3] JAR + Dockerfile 전송"
                        scp -i \${SSH_KEY} -o StrictHostKeyChecking=no \\
                            build/libs/${JAR_NAME} \\
                            ${QA_USER}@\${QA_HOST}:${DEPLOY_PATH}/app.jar
                        scp -i \${SSH_KEY} -o StrictHostKeyChecking=no \\
                            Dockerfile \\
                            ${QA_USER}@\${QA_HOST}:${DEPLOY_PATH}/Dockerfile

                        echo "[2/3] Docker 이미지 빌드"
                        ssh -i \${SSH_KEY} -o StrictHostKeyChecking=no \\
                            ${QA_USER}@\${QA_HOST} \\
                            "docker build -t ${SERVICE}:latest ${DEPLOY_PATH}"

                        echo "[3/3] 컨테이너 재시작"
                        ssh -i \${SSH_KEY} -o StrictHostKeyChecking=no \\
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
                               -e SPRING_DATA_REDIS_PASSWORD='noticore1!' \\
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
            when { branch 'develop' }
            steps {
                withCredentials([
                    string(credentialsId: 'qa-server-host', variable: 'QA_HOST'),
                    sshUserPrivateKey(credentialsId: 'qa-ssh-key', keyFileVariable: 'SSH_KEY')
                ]) {
                    retry(6) {
                        sleep time: 10, unit: 'SECONDS'
                        sh """
                            ssh -i \${SSH_KEY} -o StrictHostKeyChecking=no \\
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
                if (env.BRANCH_NAME == 'develop') {
                    slackSend(
                        channel: '#deploy-log',
                        color: 'good',
                        message: "✅ *${APP_NAME}* QA 배포 성공\n" +
                                 "Branch: `${env.BRANCH_NAME}` | Build: `#${env.BUILD_NUMBER}`\n" +
                                 "${env.BUILD_URL}"
                    )
                }
            }
        }
        failure {
            script {
                if (env.BRANCH_NAME == 'develop') {
                    slackSend(
                        channel: '#deploy-log',
                        color: 'danger',
                        message: "❌ *${APP_NAME}* QA 배포 실패\n" +
                                 "Branch: `${env.BRANCH_NAME}` | Build: `#${env.BUILD_NUMBER}`\n" +
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
