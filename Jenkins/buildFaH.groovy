node {
    stage('setup'){
        checkout scm
        currentBuild.description = "${Branch}"
        appName = "k8s-fah-richstokes"
        dockerRepo = "atriarchsystems"
        deploymentEnvironment = "Development"
        dockerCredId = "AtriarchDockerID"
        dockerfilePathFromRoot = "./Dockerfile"// this is the path from the base directory
    }
    stage('build'){
       def buildout = sh(returnStdout: true, script: "docker build -t ${appName} -f ${dockerfilePathFromRoot} .")
       println buildout
    }
    stage('push'){
        def tagout = sh(returnStdout: true, script: "docker tag ${appName} ${dockerRepo}/${appName}:latest")
        println tagout
        withCredentials([usernamePassword(usernameVariable: "DOCKER_USER",passwordVariable: "DOCKER_PASS", credentialsId: dockerCredId)]){
            def loginout = sh(returnStdout: true, script: "echo ${DOCKER_PASS} | docker login --username ${DOCKER_USER} --password-stdin")
            println loginout
            def pushout = sh(returnStdout: true, script: "docker push ${dockerRepo}/${appName}:latest")
            println pushout
        }
    }
    stage('deploy'){        
        def deployout = sh(returnStdout: true, script: '''kubectl apply -f ./folding-cpu.yaml -n k8s-fah''')
        def updateout = sh(returnStdout: true, script: '''kubectl patch deployment fah-cpu -n k8s-fah -p "{\\"spec\\":{\\"template\\":{\\"metadata\\":{\\"labels\\":{\\"date\\":\\"`date +'%s'`\\"}}}}}"''')
        println deployout        
    }                
}
