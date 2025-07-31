# Microservices with Spring Boot and Spring Cloud on Kubernetes

In this project I demonstrate an implementation of spring cloud microservices deployed on Kubernetes. 


## Getting Started 
Prerequisite - Knowledge of spring boot, docker and kubernetes.  
I have tested the solution on a local Kubernetes single node cluster using docker desktop on mac.   
I have also tested on a AWS EKS cluster
<!-- I have also tested on a multiple node private GKE cluster. -->

## Architecture

The example microservices consists of the following modules:
- **employee-service** - a module containing the first of our sample microservices that allows to perform CRUD operation on Mongo repository of employees
- **department-service** - a module containing the second of our sample microservices that allows to perform CRUD operation on Mongo repository of departments. It communicates with employee-service. 

## Usage

1. `kubectl create ns vik` - Create new kubernetes namespace 
2. `kubectl config set-context --current --namespace=vik` - use newly created namespace
3. `kubectl apply -f privileges.yaml` - Spring Cloud Kubernetes requires access to the Kubernetes API in order to be able to retrieve a list of addresses for pods running for a single service

### Database setup
Go to the `k8s` directory. Here several YAML scripts need to be applied before running the microservices.
1. `kubectl apply -f mongo-secret.yaml` - credentials for MongoDB
2. `kubectl apply -f mongo-configmap.yaml` - user for MongoDB
3. `kubectl apply -f mongo.yaml` - Deployment for MongoDB
4. `kubectl apply -f mongo-express.yaml` - Deployment for MongoExpress web console for interacting with mongo db.  For GKE change the type of the service in mongo-express.yaml to LoadBalancer.

#### Local docker-desktop
Mongo express is accessible at `http://localhost:30000`  (if login needed use admin/pass)<br/>
You can create more databases via mongo express but you will need to assign users to the newly created database - https://www.mongodb.com/docs/manual/tutorial/create-users/.  Open interactive terminal in the mongo pod and then you can execute the commands

#### AWS
For one of the EC2 instances add a security group inbound rule: Custom TCP, Port Range 30000, Source My IP <br/>
If you have multiple nodes you can use any node as services span nodes.  

<!-- 
#### GKE

With a private GKE cluster you expose mongo express via an external load balancer.  If you have setup a non private cluster (not advisable as your vms will have public ips) you can also expose mongo express via NodePort service type.

Do `kubectl get service` and see the mongo-express-service.  Mongo express is accessible at `http://{service_external_ip):8081` (where 8081 is defined as the service port)

If defining the type of the service in mongo-express.yaml as NodePort when using GKE ensure you have a firewall rule to allow ingress to the node on port 30000 - `gcloud compute firewall-rules create test-node-port --allow tcp:30000`  
Do `kubectl get node -o wide` and see the external ip address of the node - Mongo express is accessible at `http://{node_external_ip):30000`  If you have multiple nodes you can use any node as services span nodes.   
-->

### Build and deploy microservices
1. `mvn clean package` - In root folder this will build both modules employee and department 
2. Build Docker images for each module using command, for example: `docker build -t vik/employee:1.0 .`

#### AWS
1. Upload the docker images to docker hub:  
`docker tag vik/employee:1.0 vikrantardhawa/employee:1.0`  
`docker push vikrantardhawa/employee:1.0`
2. In AWS CLI clone this git repo and update the image in deployment.yaml to use the image uploaded to docker hub

<!--
#### GKE
1. Upload the docker images to google container registry:  
`docker tag vik/employee:1.0 gcr.io/${GOOGLE_CLOUD_PROJECT}/vik/employee:1.0`  
`docker push gcr.io/${GOOGLE_CLOUD_PROJECT}/vik/employee:1.0`
2. Update the image in deployment.yaml to use the image from container registry
-->

#### Local docker-desktop and AWS
1. `kubectl apply -f springboot-configmap.yaml` - for employee service   
2. `kubectl apply -f deployment.yaml` - for both services  
3. Check status with `kubectl get all` and `kubectl logs {pod-name}`

### Ingress
1. `kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.3.1/deploy/static/provider/cloud/deploy.yaml` - Install ingress
2. `kubens ingress-nginx` - Move to the newly created namespace ingress-nginx 
3. `kubectl get all` - Check the nginx-controller pod in this namespace is running

#### Local docker-desktop
1. Add to hosts file: 127.0.0.1 microservices.info (on mac run command sudo vi /private/etc/hosts)
2. `kubectl config set-context --current --namespace=vik`  
3. `kubectl apply -f ingress.yaml`

#### AWS
1. Browse to the external ip of the nginx-controller Loadbalancer service in the ingress-nginx namespace.  You should see a 404 nginx page
2. `kubectl config set-context --current --namespace=vik` 
3. `kubectl apply -f ingress-gke.yaml`
4. `kubectl get ingress` - note the address (it will be the same as the address you browsed to where you got the 404 page)

<!--   
#### GKE
1. Browse to the external ip of the nginx service.  You should see a 404 nginx page
2. `kubens vik` 
3. `kubectl apply -f ingress-gke.yaml`
4. `kubectl get ingress` - note the address (it will be the same as the address you browsed to where you got the 404 page)
-->

### Invoke application

NOTE: When running on AWS replace microservices.info in the following commands with the ingress address as noted above

Add data to employee table
`curl --location --request POST 'http://microservices.info/employee' \
--header 'Content-Type: application/json' \
--data-raw '{
    "id" : "1",
    "organizationId" : "1",
    "departmentId" : "1",
    "name" : "vik",
    "age" : "40",
    "position" : "software developer"
}'`

`curl --location --request GET 'http://microservices.info/employee'`

Add data to department table
`curl --location --request POST 'http://microservices.info/department' \
--header 'Content-Type: application/json' \
--data-raw '{
    "id" : "1",
    "organizationId" : "1",
    "name" : "dept1",
    "employees" : [
    {
        "id": "1",
        "organizationId": "1",
        "departmentId": "1",
        "name": "vik",
        "age": 40,
        "position": "software developer"
    }
]
}'`

`curl --location --request GET 'http://microservices.info/department/feign'`

The last curl demonstrates inter service communication where department-service calls employee-service

### Health URLS

For local docker-desktop replace {ingress_ip} with microservices.info.  For AWS replace with ip of ingress service - `kubectl get ingress`

http://{ingress_ip}/employee/actuator/health  
http://{ingress_ip}/employee/actuator/health/liveness  
http://{ingress_ip}/employee/actuator/health/readiness

http://{ingress_ip}/department/actuator/health  
http://{ingress_ip}/department/actuator/health/liveness  
http://{ingress_ip}/department/actuator/health/readiness

### Shell script

To automate running of above commands cd to root of project and run `. script.sh`  

### Helm

Delete any existing employee deployment and configmap springboot-configuration

`cd employee-service`  
`helm install dev-helm helm -f helm/values-dev.yaml` - note this also installs the configmap springboot-configuration (if its already installed this command will fail and you will need to delete the configmap)  
`helm ls - a` - shows our installed helm chart  
`helm upgrade dev-helm helm -f helm/values-dev.yaml` - after updating values-dev.yaml run this command. Note just changing databaseName will not have any affect.  The deployment need to be modified hence update containerName  
`helm rollback dev-helm 1` - rolls back to previous version  
`helm uninstall dev-helm` - uninstalls the helm chart, deployment and configmap

## Troubleshooting 

To debug do `kubectl port-forward <your pod name> 5005:5005` and then attach debugger in IDE - https://refactorfirst.com/how-to-remote-debug-java-application-on-kubernetes.  
  
<!-- On private GKE cluster when executing `kubectl apply -f ingress-gke.yaml` you may receive an error.  In which case delete ingress webhook - see second answer here https://stackoverflow.com/questions/61616203/nginx-ingress-controller-failed-calling-webhook -->




