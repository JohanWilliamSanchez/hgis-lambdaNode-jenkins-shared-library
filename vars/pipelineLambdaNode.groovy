// vars/pipelineLambdaNode.groovy
def call(Map config = [:]) {
    pipeline {
        agent any
        environment {
            APP_NAME    = "${config.appName ?: 'AppSinNombre'}"
            DEPLOY_ENV  = "${env.BRANCH_NAME == 'main' ? 'production' : env.BRANCH_NAME == 'staging' ? 'staging' : 'dev'}"
        }
        stages {
            
            stage('Build') {
                when { expression { return config.runBuild != false } }
                steps { echo "🏗️ Compilando..." }
            }
            stage('Test') {
                when { expression { return config.runTest != false } }
                steps { echo "🧪 Ejecutando pruebas..." }
            }
            stage('Static Code Analysis') {
                when { expression { return config.runStaticAnalysis != false } }
                steps {
                    sh '''
                        pip install semgrep -q --break-system-packages
                        semgrep scan \
                            --config auto \
                            --json \
                            --output semgrep-report.json \
                            --severity ERROR \
                            . || true
                    '''
                }
                post {
                    always {
                        archiveArtifacts artifacts: 'semgrep-report.json',
                                         allowEmptyArchive: true
                    }
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
