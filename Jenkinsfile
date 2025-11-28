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
      - sleep 99d          # 그냥 살아만 있게
    tty: true
    volumeMounts:
      - name: docker-config
        mountPath: /kaniko/.docker    # DockerHub 인증
      
      - name: kaniko-build
        mountPath: /workspace         # build context
      
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
""")  {
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
        script {
          sh '''
            git config --global user.email "jenkins@ci.com"
            git config --global user.name "Jenkins CI"
          '''
          
          sh '''
            rm -rf DeploymentRepo
            git clone https://$GIT_USER:$GIT_PASS@github.com/AllStackProject/Deployment.git DeploymentRepo
          '''
        
          sh """
            cd DeploymentRepo/frontend/overlays/dev
            sed -i 's|newTag:.*|newTag: "${BUILD_NUMBER}"|' kustomization.yaml
          """
          
          sh '''
            cd DeploymentRepo
            git add frontend/overlays/dev/kustomization.yaml
            git commit -m "chore: update frontend image tag to ${BUILD_NUMBER}"
            git push origin main
          '''
        }
      }
    }
    stage('Post-Build') {
      echo "✅ Docker image pushed to DockerHub successfully!"
    }

    // -------------------------
    // 🎉 SUCCESS SLACK
    // -------------------------
    slackSend(
      channel: "C09FJ3HK7E1",
      color: "good",
      message: """
🎉 *SUCCESS* — Backend Build #${BUILD_NUMBER}
*Image:* dockdock150/backend:${BUILD_NUMBER}
*Project:* ${JOB_NAME}
*Time:* ${new Date()}
"""
      ,
      tokenCredentialId: "slack-webhook"
    )

  } catch (err) {

    // -------------------------
    // ❌ FAILURE SLACK
    // -------------------------
    slackSend(
      channel: "C09FJ3HK7E1",
      color: "danger",
      message: """
❌ *FAILURE* — Backend Build #${BUILD_NUMBER}
*Project:* ${JOB_NAME}
*Error:* ${err}
*Time:* ${new Date()}
"""
      ,
      tokenCredentialId: "slack-webhook"
    )

    throw err
  }

} // end node

}
