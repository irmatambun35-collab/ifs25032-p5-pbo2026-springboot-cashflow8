pipeline {
    agent any

    environment {
        APP_NAME = 'ifs25032-p5-pbo2026-springboot-cashflow'
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Build & Test') {
            steps {
                // H2 dipakai lewat profil test; verify juga menjalankan cek Jacoco
                sh './mvnw -B clean verify'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Jacoco 100%') {
            steps {
                sh 'test -f target/site/jacoco/index.html'
                archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: false
            }
        }

        stage('Trivy Scan') {
            steps {
                sh 'trivy fs --exit-code 1 --severity HIGH,CRITICAL --skip-dirs node_modules,target .'
            }
        }

        stage('Package') {
            steps {
                sh './mvnw -B clean package -Pproduction -DskipTests'
            }
        }

        stage('Publish') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Deploy') {
            when { branch 'main' }
            steps {
                // Sesuaikan dengan server tujuan; kredensial DB_*/ROOT_* diset di environment server
                sh '''
                    echo "Deploy ${APP_NAME}"
                    # scp target/${APP_NAME}-0.0.1-SNAPSHOT.jar user@server:/opt/app/
                    # ssh user@server "systemctl restart cashflow"
                '''
            }
        }
    }
}
