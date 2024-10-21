pipeline {

    agent any

    tools {
        jdk 'jdk17'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Replace Application Properties') {
            steps {
                sh 'chmod -R u+w ./src/main/resources'
                withCredentials([file(credentialsId: 'blog-service-application-yml', variable: 'application')]) {
                    script {
                        sh 'cp ${application} ./src/main/resources/application.yml'
                    }
                }
            }
        }


        stage('Build & Test') {
            steps {
                script {
                    sh './gradlew clean build --no-daemon'
                }
            }
        }



        stage('Build & Tag Docker Image') {
            steps {
                script {
                    withDockerRegistry(credentialsId: 'dockerhub-credential-alphaka') {
                        sh 'docker build -t alphaka/blog-service:latest .'

                    }
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    withDockerRegistry(credentialsId: 'dockerhub-credential-alphaka') {
                        sh 'docker push alphaka/blog-service:latest'

                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    withCredentials([
                            sshUserPrivateKey(credentialsId: 'jenkins-ssh', keyFileVariable: 'SSH_KEY'),
                            string(credentialsId: 'vm-app1-address', variable: 'VM_ADDRESS')
                    ]) {

                        // 미리 작성해둔 deploy.sh 실행
                        sh '''
                            /var/jenkins_home/scripts/deploy.sh \\
                            "$VM_ADDRESS" \\
                            "$SSH_KEY" \\
                            "alphaka/blog-service" \\
                            "blog-service" \\
                            "8003"
                        '''
                    }
                }
            }
        }

    }
}
