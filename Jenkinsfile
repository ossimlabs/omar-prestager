properties([
  parameters ([
    string(name: 'DOCKER_REGISTRY_DOWNLOAD_URL',
           defaultValue: 'nexus-docker-private-group.ossim.io',
           description: 'Repository of docker images')
  ]),
  pipelineTriggers([
    [$class: "GitHubPushTrigger"]
  ]),
  [$class: 'GithubProjectProperty', displayName: '', projectUrlStr: 'https://github.com/ossimlabs/omar-prestager']
])

podTemplate(
  containers: [
    containerTemplate(
        name: 'docker',
        image: 'docker:19.03.11',
        ttyEnabled: true,
        command: 'cat',
        privileged: true
    ),
    containerTemplate(
        image: "${DOCKER_REGISTRY_DOWNLOAD_URL}/alpine/helm:3.2.3",
        name: 'helm',
        command: 'cat',
        ttyEnabled: true
    ),
    containerTemplate(
        name: 'git',
        image: 'alpine/git:latest',
        ttyEnabled: true,
        command: 'cat',
        envVars: [
            envVar(key: 'HOME', value: '/root')
        ]
    )
  ],
  volumes: [
    hostPathVolume(
      hostPath: '/var/run/docker.sock',
      mountPath: '/var/run/docker.sock'
    ),
  ]
)

{
  node(POD_LABEL){
    stage("Checkout branch"){
        scmVars = checkout(scm)
        GIT_BRANCH_NAME = scmVars.GIT_BRANCH
        BRANCH_NAME = """${sh(returnStdout: true, script: "echo ${GIT_BRANCH_NAME} | awk -F'/' '{print \$2}'").trim()}"""
        VERSION = '0.8'
        ARTIFACT_NAME = 'omar-prestager'
        GIT_TAG_NAME = ARTIFACT_NAME + "-" + VERSION
        script {            
            if (BRANCH_NAME != 'master') {
                buildName "${VERSION}-SNAPSHOT - ${BRANCH_NAME}"
            } else {
                buildName "${VERSION} - ${BRANCH_NAME}"
            }
        }
    }

    stage("Load Variables")
          {
            withCredentials([string(credentialsId: 'o2-artifact-project', variable: 'o2ArtifactProject')]) {
              step ([$class: "CopyArtifact",
                projectName: o2ArtifactProject,
                filter: "common-variables.groovy",
                flatten: true])
              }
              load "common-variables.groovy"

            switch (BRANCH_NAME) {
            case "master":
              TAG_NAME = VERSION
              break

            case "dev":
              TAG_NAME = "latest"
              break

            default:
              TAG_NAME = BRANCH_NAME
              break
          }

        DOCKER_IMAGE_PATH = "${DOCKER_REGISTRY_PRIVATE_UPLOAD_URL}/omar-prestager"

        }
        stage('SonarQube Analysis') {
            nodejs(nodeJSInstallationName: "${NODEJS_VERSION}") {
                def scannerHome = tool "${SONARQUBE_SCANNER_VERSION}"

                withSonarQubeEnv('sonarqube'){
                    sh """
                        ${scannerHome}/bin/sonar-scanner \
                        -Dsonar.projectKey=omar-prestager \
                        -Dsonar.login=${SONARQUBE_TOKEN}
                    """
                }
            }
        }

    stage("Build & Deploy") {
      container('docker'){
        withGradle {
          withDockerRegistry(credentialsId: 'dockerCredentials', url: "https://${DOCKER_REGISTRY_PRIVATE_UPLOAD_URL}") {
          script {
            sh 'apk add gradle'
            sh 'gradle jDB'
            }
          }
        }
      }
    }
    
    stage("Push Docker Image") {
      container('docker'){
        withDockerRegistry(credentialsId: 'dockerCredentials', url: "https://${DOCKER_REGISTRY_PRIVATE_UPLOAD_URL}") {
          script {
            sh "docker tag nexus-docker-private-hosted.ossim.io/omar-prestager:latest ${DOCKER_IMAGE_PATH}:${TAG_NAME}"
            sh "docker push ${DOCKER_IMAGE_PATH}:${TAG_NAME}"

            if (BRANCH_NAME == "master") {
              sh  "docker tag ${DOCKER_IMAGE_PATH}:${TAG_NAME} ${DOCKER_IMAGE_PATH}:release"
              sh  "docker push ${DOCKER_IMAGE_PATH}:release"
            }
          }
        }
      }
    }

    stage('Package Chart'){
      container('helm') {
        script {
          sh 'helm package chart'
        }
      }
    }

    stage('Upload Chart'){
      container('helm') {
        withCredentials([usernameColonPassword(credentialsId: 'helmCredentials', variable: 'HELM_CREDENTIALS')]) {
          script {
            sh 'apk add curl'
            sh 'curl -u ${HELM_CREDENTIALS} ${HELM_UPLOAD_URL} --upload-file *.tgz -v'
          }
        }
      }
    }

    stage('Tag Repo') {
      when (BRANCH_NAME == 'master') {
        container('git') {
          withCredentials([sshUserPrivateKey(
          credentialsId: env.GIT_SSH_CREDENTIALS_ID,
          keyFileVariable: 'SSH_KEY_FILE',
          passphraseVariable: '',
          usernameVariable: 'SSH_USERNAME')]) {
            script {
                sh """
                  mkdir ~/.ssh
                  echo -e "StrictHostKeyChecking=no\nIdentityFile ${SSH_KEY_FILE}" >> ~/.ssh/config
                  git config user.email "radiantcibot@gmail.com"
                  git config user.name "Jenkins"
                  git tag -a "${GIT_TAG_NAME}" \
                    -m "Generated by: ${env.JENKINS_URL}" \
                    -m "Job: ${env.JOB_NAME}" \
                    -m "Build: ${env.BUILD_NUMBER}"
                  git push -v origin "${GIT_TAG_NAME}"
                """
            }
          }
        }
      }
    }
  }
}
