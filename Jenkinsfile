pipeline {
    agent any

    tools {
        jdk 'jdk17'
        gradle 'gradle'
    }

    environment {
        SCANNER_HOME = tool 'sonar-scanner'
    }

    stages {
        stage('Checkout') {
            steps {
               checkout scm
            }
        }
        stage('Dependencies') {
            steps {
                // Gradle 의존성 다운로드
                sh './gradlew dependencies'
            }
        }
        stage('Replace Secret Properties') {
            steps {
                sh 'chmod -R u+w ./src/main/resources'
                withCredentials([file(credentialsId: 'blogSecret', variable: 'blogSecret')]) {
                    script {
                        sh 'cp $blogSecret ./src/main/resources/application-secret.yml'
                    }
                }
            }
        }
        stage('Build & Test') {
            steps {
                // Gradle 컴파일과 테스트 수행
                sh './gradlew clean build'
            }
        }
        stage('File System Scan') {
            steps {
                sh 'trivy fs --format table -o trivy-fs-report.html .'
            }
        }
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonar') {
                    sh '''
                        $SCANNER_HOME/bin/sonar-scanner \
                        -Dsonar.projectName=OnGil-Blog \
                        -Dsonar.projectKey=OnGil-Blog \
                        -Dsonar.java.binaries=.
                    '''
                }
            }
        }
        stage('Quality Gate') {
            steps {
                script {
                    waitForQualityGate abortPipeline: false, credentialsId: 'sonar-token'
                }
            }
        }
        stage('Build & Tag Docker Image') {
            steps {
                script {
                    withDockerRegistry(credentialsId: 'docker-credential') {
                        sh 'docker build -t hojun8094/blog:latest .'

                    }
                }
            }
        }
        stage('Docker Image Scan') {
            steps {
                sh 'trivy image --format table -o trivy-image-report.html hojun8094/blog:latest'
            }
        }
        stage('Push Docker Image') {
            steps {
                script {
                    withDockerRegistry(credentialsId: 'docker-credential') {
                        sh 'docker push hojun8094/blog:latest'
                    }
                }
            }
        }
        stage('Deploy Docker Container') {
            steps {
                script {
                    // 기존 컨테이너가 있으면 중지 및 제거
                    sh '''
                    if [ $(docker ps -a -q -f name=blog) ]; then
                        docker stop blog
                        docker rm blog
                    fi
                    '''
                    // 새로운 컨테이너 실행
                    sh 'docker run -d --network msa-network --name blog -p 8761:8761 hojun8094/blog:latest'
                }
            }
        }
    }
}
