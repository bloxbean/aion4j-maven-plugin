/*
 * MIT License
 *
 * Copyright (c) 2019 BloxBean Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

pipeline {
    agent any

    parameters {
        choice(
                choices: ['BUILD_ONLY' , 'SNAPSHOT_RELEASE', 'RELEASE'],
                description: '',
                name: 'BUILD_TYPE')
    }

    tools {
        maven 'M3'
        jdk 'jdk-11'
    }

    environment {
        gpg_passphrase = credentials("gpg_passphrase")
    }

    stages {

        stage('Build') {
            steps {
                 sh  'mvn initialize'
                 sh  'mvn clean install -DskipITs'
            }
        }

        stage('Unit Tests') {
            steps {

                sh 'mvn -B test'

            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Integration Tests') {
            steps {

                sh 'mvn -B integration-test'

            }
//            post {
//                always {
//                    junit '**/target/failsafe-reports/*.xml'
//                }
//            }
        }

        stage("Snapshot Release") {
            when {
                expression { params.BUILD_TYPE == 'SNAPSHOT_RELEASE' }
            }
            steps {
                configFileProvider(
                        [configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')]) {

                    sh 'mvn -s "$MAVEN_SETTINGS" clean deploy -Dgpg.passphrase=${gpg_passphrase} -DskipITs -Prelease'

                }
            }
        }

        stage("Release") {
            when {
                expression { params.BUILD_TYPE == 'RELEASE' }
            }
            steps {
                configFileProvider(
                        [configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')]) {

                    sh """
                        git config --global user.email satran004@gmail.com
                        git config --global user.name Jenkins
                       """

                    sh 'mvn -s "$MAVEN_SETTINGS" release:clean release:prepare release:perform -Darguments=-Dgpg.passphrase=${gpg_passphrase} -DskipITs -Prelease'
                }
            }
            post {
                success {
                    echo "Publish to Sonatype repository"
                    dir("target/checkout") {
                        sh 'mvn nexus-staging:release -Prelease'
                    }
                }
            }
        }

        stage('Results') {
            steps {
                archiveArtifacts 'target/*.jar'
            }
        }
    }
}
