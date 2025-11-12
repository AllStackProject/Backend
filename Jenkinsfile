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
            --cleanup
          """
        }
      }
    }

    stage('Post-Build') {
      echo "✅ Docker image pushed to DockerHub successfully!"
    }
  }
}
