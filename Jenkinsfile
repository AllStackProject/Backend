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
      
      - name: kaniko-cache
        mountPath: /kaniko/.cache

      - name: kaniko-snap
        mountPath: /kaniko/snapshots
      
      - name: kaniko-build
        mountPath: /workspace         # build context
      
      - name: kaniko-tmp
        mountPath: /tmp               # snapshot, layer temp files
    
    resources:
      requests:
        ephemeral-storage: "2Gi"
      limits:
        ephemeral-storage: "4Gi"

  volumes:
  - name: docker-config
    secret:
      secretName: docker-config-dockerhub
      items:
      - key: .dockerconfigjson
        path: config.json
  
  - name: kaniko-snap
    persistentVolumeClaim:
      claimName: pvc-kaniko-root-60
  
  - name: kaniko-cache
    persistentVolumeClaim:
      claimName: pvc-kaniko-root-60    
  
  - name: kaniko-build
    persistentVolumeClaim:
      claimName: pvc-kaniko-build-20
      
  - name: kaniko-tmp
    persistentVolumeClaim:
      claimName: pvc-kaniko-tmp-20
""")  {

  node(POD_LABEL) {

    stage('Checkout') {
      // Webhook으로 받은 SCM 정보로 자동 checkout
      checkout scm
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

          // 빌드 및 DockerHub 푸시
          sh """
          /kaniko/executor \
            --context ${WORKSPACE} \
            --dockerfile ${WORKSPACE}/Dockerfile \
            --destination ${IMAGE} \
            --cache=true \
            --cache-dir=/tmp \
            --cleanup \
            --force
          """
        }
      }
    }
    
    stage('Update Kustomize for ArgoCD') {
  withCredentials([usernamePassword(credentialsId: 'git-clone', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_PASS')]) {
    script {
      // ✅ Git 설정
      sh '''
        git config --global user.email "jenkins@ci.com"
        git config --global user.name "Jenkins CI"
      '''

      // ✅ Deployment repo clone
      sh '''
        rm -rf DeploymentRepo
        git clone https://$GIT_USER:$GIT_PASS@github.com/AllStackProject/Deployment.git DeploymentRepo
      '''

      // ✅ kustomization.yaml 수정
      sh """
        cd DeploymentRepo/backend/overlays/dev
        sed -i 's|newTag:.*|newTag: "${BUILD_NUMBER}"|' kustomization.yaml
      """

      // ✅ 변경사항 커밋 및 푸시
      sh '''
        cd DeploymentRepo
        git add backend/overlays/dev/kustomization.yaml
        git commit -m "chore: update image tag to ${BUILD_NUMBER}"
        git push origin main
      '''
    }
  }
}

    stage('Post-Build') {
      echo "✅ Docker image pushed to DockerHub successfully!"
    }
  }
}
