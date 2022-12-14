# Microservices with Spring Boot and Spring Cloud on Kubernetes

In this project I demonstrate an implementation of spring cloud microservices deployed on Kubernetes. 


## Getting Started 
Prerequisite - Knowledge of spring boot, docker and kubernetes.  
I have tested the solution on a local Kubernetes single node cluster using docker desktop on mac.   
I have also tested on a multiple node cluster using GKE.

## Usage

1. `kubectl create ns vik` - Create new kubernetes namespace 
2. `kubens vik` - use newly created namespace
3. `kubectl apply -f privileges.yaml` - Spring Cloud Kubernetes requires access to the Kubernetes API in order to be able to retrieve a list of addresses for pods running for a single service

### Database setup
Go to the `k8s` directory. Here several YAML scripts need to be applied before running the microservices.
1. `kubectl apply -f mongo-secret.yaml` - credentials for MongoDB
2. `kubectl apply -f mongo-configmap.yaml` - user for MongoDB
3. `kubectl apply -f mongo.yaml` - Deployment for MongoDB
4. `kubectl apply -f mongo-express.yaml` - Deployment for MongoExpress web console for interacting with mongo db


When using docker-desktop locally mongo express is accessible at http://localhost:8081/.   
When using GKE do `kubectl get service` and see the mongo-express-service - use the external ip address instead of localhost

### Build and deploy microservices
1. `mvn clean package` - In root folder this will build both modules employee and department 
2. Build Docker images for each module using command, for example: `docker build -t vik/employee:1.0 .`
#### GKE
1. Upload the docker images to google container registry:  
`docker tag vik/employee:1.0 gcr.io/${GOOGLE_CLOUD_PROJECT}/vik/employee:1.0`  
`docker push gcr.io/${GOOGLE_CLOUD_PROJECT}/vik/employee:1.0`
2. Update the image in deployment.yaml to use the image from container registry
#### Local docker-desktop and GKE
1. Apply all templates using command: `kubectl apply -f deployment.yaml`
2. Check status with `kubectl get all` and `kubectl logs {pod-name}`

### Ingress
1. `kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.3.1/deploy/static/provider/cloud/deploy.yaml` - Install ingress
2. `kubens ingress-nginx` - Move to the newly created namespace ingress-nginx 
3. `kubectl get all` - Check the nginx-controller pod in this namespace is running

#### Local docker-desktop
1. Add to hosts file: 127.0.0.1 microservices.info (on mac run command sudo vi /private/etc/hosts)
2. `kubens vik`  
3. `kubectl apply -f ingress.yaml`

#### GKE
1. Browse to the external ip of the nginx service.  You should see a 404 nginx page
2. `kubens vik` 
3. In ingress.yaml remove host line and put hyphen before http on following line
4. `kubectl apply -f ingress.yaml`
5. `kubectl get ingress` - note the address (it will be the same as the address you browsed to where you got the 404 page)

### Invoke application

NOTE: When running on gke replace microservices.info in the following commands with the ingress address as noted above

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

## Architecture

The example microservices consists of the following modules:
- **employee-service** - a module containing the first of our sample microservices that allows to perform CRUD operation on Mongo repository of employees
- **department-service** - a module containing the second of our sample microservices that allows to perform CRUD operation on Mongo repository of departments. It communicates with employee-service. 

## Troubleshooting 

To debug do `kubectl port-forward <your pod name> 5005:5005` and then attach debugger in IDE - https://refactorfirst.com/how-to-remote-debug-java-application-on-kubernetes




