pipeline {
    agent any
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    credentialsId: 'github-token',
                    url: 'https://github.com/thermoCat/jenkins-test'
            }
        }
        
        stage('Build') {
            steps {
                echo 'Building...'
                // 여기에 빌드 명령어 추가
            }
        }
        
        stage('Deploy') {
            steps {
                echo 'Deploying...'
                script {
                    // 예시: docker-compose로 배포
                    sh 'docker-compose down || true'
                    sh 'docker-compose up -d'
                }
            }
        }
    }
}