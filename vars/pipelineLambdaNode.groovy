// vars/pipelineLambdaNode.groovy
def call(Map config = [:]) {
    pipeline {
        agent any
        environment {
            APP_NAME    = "${config.appName ?: 'AppSinNombre'}"
            DEPLOY_ENV  = "${env.BRANCH_NAME == 'main' ? 'production' : env.BRANCH_NAME == 'staging' ? 'staging' : 'dev'}"
        }
        stages {
            stage('Checkout') {
                steps {
                    echo "📥 Descargando: ${env.APP_NAME} | Ambiente: ${env.DEPLOY_ENV}"
                    checkout scm
                }
            }
            stage('Build') {
                when { expression { return config.runBuild != false } }
                steps { echo "🏗️ Compilando..." }
            }
            stage('Test') {
                when { expression { return config.runTest != false } }
                steps { echo "🧪 Ejecutando pruebas..." }
            }
            stage('Security Scan') {
                when { expression { return config.runSecurity != false } }
                steps { echo "🛡️ Escaneando vulnerabilidades..." }
            }

            stage('Aprobación Manual') {
                when {
                    allOf {
                        expression { return config.requireApproval == true }
                        branch 'main'
                    
                    }
                }
                steps {
                    script {
                        timeout(time: 2, unit: 'HOURS') {
                            input message: "🚀 ¿Autorizar despliegue de ${env.APP_NAME} en ${env.DEPLOY_ENV}?",
                                  ok: "¡Sí, desplegar!",
                                  submitter: "admin"
                        }
                    }
                }
            }

            stage('Deploy') {
                when { expression { return config.runDeploy != false } }
                steps {
                    echo "🚀 Desplegando ${env.APP_NAME} en ${env.DEPLOY_ENV}..."
                }
            }
        }
    }
}
