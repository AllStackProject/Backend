podTemplate(yaml: """
apiVersion: v1
kind: Pod
metadata:
  labels:
    jenkins/kaniko: "true"
spec:
  nodeSelector:
    jenkins-node: "true"

  tolerations:
  - key: "dedicated"
    operator: "Equal"
    value: "cicd"
    effect: "NoSchedule"

  containers:
  - name: kaniko
    image: gcr.io/kaniko-project/executor:v1.6.0-debug
    imagePullPolicy: Always
    command:
      - /busybox/sh
    args:
      - -c
      - sleep 99d
    tty: true
    volumeMounts:
      - name: docker-config
        mountPath: /kaniko/.docker
      - name: kaniko-build
        mountPath: /workspace
      - name: kaniko-tmp
        mountPath: /tmp
    resources:
      requests:
        cpu: "500m"
        memory: "1Gi"

  volumes:
  - name: docker-config
    secret:
      secretName: docker-config-dockerhub
      items:
        - key: .dockerconfigjson
          path: config.json

  - name: kaniko-build
    persistentVolumeClaim:
      claimName: pvc-kaniko-build-60

  - name: kaniko-tmp
    persistentVolumeClaim:
      claimName: pvc-kaniko-tmp-30
""") {

  node(POD_LABEL) {

    try {

      stage('Checkout') {
        checkout scm
      }

      stage('Copy to Kaniko Context') {
        container('kaniko') {
          sh """
            rm -rf /workspace/*
            cp -r ${WORKSPACE}/* /workspace/
          """
        }
      }

      stage('SonarQube Analysis') {
        withSonarQubeEnv('sonarQube') {
          withCredentials([string(credentialsId: 'sonarQubeToken', variable: 'SONAR_TOKEN')]) {
            sh """
              ./gradlew sonarqube \
                -Dsonar.projectKey=backend \
                -Dsonar.host.url=$SONAR_HOST_URL \
                -Dsonar.login=$SONAR_TOKEN
            """
          }
        }
      }

      stage('Build & Push with Kaniko') {
        container('kaniko') {
          script {
            def IMAGE = "docker.io/dockdock150/backend:${BUILD_NUMBER}"
            sh """
              /kaniko/executor \
                --context /workspace \
                --dockerfile /workspace/Dockerfile \
                --destination ${IMAGE} \
                --cache=true \
                --cache-repo=docker.io/dockdock150/backend-cache \
                --cleanup \
                --force
            """
          }
        }
      }

      stage('Update Kustomize for ArgoCD') {
        withCredentials([usernamePassword(credentialsId: 'git-clone', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_PASS')]) {

          sh '''
            git config --global user.email "jenkins@ci.com"
            git config --global user.name "Jenkins CI"
          '''

          sh '''
            rm -rf DeploymentRepo
            git clone https://$GIT_USER:$GIT_PASS@github.com/AllStackProject/Deployment.git DeploymentRepo
          '''

          sh """
            cd DeploymentRepo/backend/overlays/dev
            sed -i 's|newTag:.*|newTag: "${BUILD_NUMBER}"|' kustomization.yaml
          """

          sh '''
            cd DeploymentRepo
            git add backend/overlays/dev/kustomization.yaml
            git commit -m "chore: update image tag to '"${BUILD_NUMBER}"'"
            git push origin main
          '''
        }
      }

      stage('Post-Build') {
        echo "✅ Docker image pushed to DockerHub successfully!"
      }

      currentBuild.result = 'SUCCESS'

    } catch (e) {

      currentBuild.result = 'FAILURE'
      throw e

    } finally {

      if (currentBuild.result == 'SUCCESS') {
        slackSend(
          channel: 'C09FJ3HK7E1',
          color: 'good',
          message: "🎉 *Backend Build 성공 (#${BUILD_NUMBER})*\n이미지: `dockdock150/backend:${BUILD_NUMBER}`",
          tokenCredentialId: 'slack-webhook'
        )
      } else {
        slackSend(
          channel: 'C09FJ3HK7E1',
          color: 'danger',
          message: "🔥 *Backend Build 실패 (#${BUILD_NUMBER})*",
          tokenCredentialId: 'slack-webhook'
        )
      }
    }

  }
}
