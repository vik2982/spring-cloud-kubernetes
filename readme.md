# Microservices with Spring Boot and Spring Cloud on Kubernetes

In this project I demonstrate an implementation of spring cloud microservices deployed on Kubernetes. 


## Getting Started 
Prerequisite - Knowledge of spring boot, docker and kubernetes
The services can deployed on a local Kubernetes single-node cluster (I have tested on docker desktop on mac and GKE).

## Before you start
Go to the `k8s` directory. Here several YAML scripts need to be applied before running the microservices.
1. `privileges.yaml` - Spring Cloud Kubernetes requires access to the Kubernetes API in order to be able to retrieve a list of addresses for pods running for a single service
2. `mongo-secret.yaml` - credentials for MongoDB
3. `mongo-configmap.yaml` - user for MongoDB
4. `mongo.yaml` - `Deployment` for MongoDB
5. `mongo-express.yaml` - `Deployment` for MongoExpress web console for interacting with mongo db
Just apply these scripts using `kubectl apply`.

Mongo express is accessible at http://localhost:8081/

### Usage
1. Build Maven projects : `mvn clean package`
2. Build Docker images for each module using command, for example: `docker build -t vik/employee:1.0 .`
3. Create new kubernetes namespace for the microservices to run in - `kubectl create ns vik`
4. Apply all templates using command: `kubectl apply -f deployment.yaml`
5. Check status with `kubectl get all` and `kubectl logs {pod-name}`
6. Install ingress - `kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.3.1/deploy/static/provider/cloud/deploy.yaml`
7. Check the namespace ingress-nginx has been created and the nginx-controller pod in this namespace is running - install kubens to list namespaces
8. Add to hosts file: 127.0.0.1 microservices.info (on mac run command sudo vi /private/etc/hosts)
9. `kubectl apply -f ingress.yaml`

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




