podTemplate(yaml: """
apiVersion: v1
kind: Pod
metadata:
  labels:
    jenkins/kaniko: "true"
spec:
  containers:
    - name: kaniko
      image: gcr.io/kaniko-project/executor:v1.6.0-debug
      imagePullPolicy: Always
      command:
        - cat
      tty: true
      volumeMounts:
        - name: docker-config
          mountPath: /kaniko/.docker
  volumes:
    - name: docker-config
      secret:
        secretName: docker-config-dockerhub
        items:
          - key: .dockerconfigjson
            path: config.json
""") {

  node(POD_LABEL) {

    stage('Checkout') {
      // Webhook으로 받은 SCM 정보로 자동 checkout
      checkout scm
    }

    stage('Build & Push with Kaniko') {
      container('kaniko') {
        script {
          def IMAGE = "docker.io/dockdock150/backend:${BUILD_NUMBER}"

          // 빌드 및 DockerHub 푸시
          sh """
          /kaniko/executor \
            --context ${WORKSPACE} \
            --dockerfile ${WORKSPACE}/Dockerfile \
            --destination ${IMAGE} \
            --cleanup \
            --force
          """
        }
      }
    }
    
   stage('Update Kustomize for ArgoCD') {
      steps {
        script {
          // ✅ Kustomize 파일 경로
            def KUSTOMIZE_FILE = "kustomization.yaml"

          // ✅ Git 설정
          sh """
            git config --global user.email "jenkins@ci.com"
            git config --global user.name "Jenkins CI"
          """

      // ✅ Git 클론 (Deployment repo)
      sh """
        rm -rf DeploymentRepo
        git clone https://$GIT_USER:$GIT_PASS@github.com/AllStackProject/Deployment.git DeploymentRepo
      """

      // ✅ Kustomization 파일 수정
      sh """
        cd DeploymentRepo/overlays/dev
        sed -i 's|newTag:.*|newTag: "${BUILD_NUMBER}"|' kustomization.yaml
      """

      // ✅ 변경사항 커밋 및 푸시
      sh """
        cd DeploymentRepo
        git add overlays/dev/kustomization.yaml
        git commit -m "update image tag to ${BUILD_NUMBER}"
        git push origin main
      """
    }
  }
}

    stage('Post-Build') {
      echo "✅ Docker image pushed to DockerHub successfully!"
    }
  }
}
