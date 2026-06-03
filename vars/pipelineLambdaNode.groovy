// vars/pipelineEstandar.groovy
def call(Map config = [:]) {
    pipeline {
        agent any

        environment {
            APP_NAME = "${config.appName ?: 'AppSinNombre'}"
        }

        stages {
            stage('Checkout') {
                steps {
                    echo "📥 [Checkout] Descargando código de: ${env.APP_NAME}"
                    checkout scm
                }
            }

            stage('Build') {
                when {
                    expression { return config.runBuild != false }
                }
                steps {
                    echo "🏗️ [Build] Compilando la aplicación..."
                }
            }

            stage('Test') {
                when {
                    expression { return config.runTest != false }
                }
                steps {
                    echo "🧪 [Test] Ejecutando pruebas..."
                }
            }

            stage('Security Scan') {
                when {
                    expression { return config.runSecurity != false }
                }
                steps {
                    echo "🛡️ [Security Scan] Escaneando vulnerabilidades..."
                }
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
                         input message: "🚀 ¿Deseas autorizar el despliegue de ${env.APP_NAME}?",
                                      ok: "¡Sí, desplegar!",
                                      submitter: "admin"
                    }
                       
                    }
                }
            }

            stage('Deploy') {
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
