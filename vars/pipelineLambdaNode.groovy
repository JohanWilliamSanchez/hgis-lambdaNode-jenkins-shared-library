// vars/pipelineEstandar.groovy
def call(Map config = [:]) {
    pipeline {
        agent any

        environment {
            APP_NAME = "${config.appName ?: 'AppSinNombre'}"
        }

        stages {
            stage('Checkout') {
                agent any
                steps {
                    echo "📥 [Checkout] Descargando código de: ${env.APP_NAME}"
                    checkout scm
                }
            }

            stage('Build') {
                agent any
                when {
                    expression { return config.runBuild != false }
                }
                steps {
                    echo "🏗️ [Build] Compilando la aplicación..."
                }
            }

            stage('Test') {
                agent any
                when {
                    expression { return config.runTest != false }
                }
                steps {
                    echo "🧪 [Test] Ejecutando pruebas..."
                }
            }

            stage('Security Scan') {
                agent any
                when {
                    expression { return config.runSecurity != false }
                }
                steps {
                    echo "🛡️ [Security Scan] Escaneando vulnerabilidades..."
                }
            }

           
            stage('Aprobación Manual') {
                when {
                    expression { return config.requireApproval == true }
                }
                steps {
                    script {
                        timeout(time: 2, unit: 'HOURS') {
                         input message: "🚀 ¿Deseas autorizar el despliegue de ${env.APP_NAME}?",
                                      ok: "¡Sí, desplegar!",
                                      submitter: "admin"
                    }
                       
                    }
                }
            }

            stage('Deploy') {
                agent any
                when {
                    expression { return config.runDeploy != false }
                }
                steps {
                    echo "🚀 [Deploy] Desplegando..."
                }
            }
        }
    }
}
