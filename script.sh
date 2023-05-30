NAMESPACE='vik'
INGRESS_NAMESPACE='ingress-nginx'
SOURCE_DIR=$HOME/'spring-cloud-kubernetes'

# create ns
kubectl delete ns $NAMESPACE
kubectl create namespace $NAMESPACE
kubectl delete ns $INGRESS_NAMESPACE
kubens $NAMESPACE

# db setup
cd $SOURCE_DIR/k8s
kubectl apply -f privileges.yaml
kubectl apply -f mongo-secret.yaml
kubectl apply -f mongo-configmap.yaml
kubectl apply -f mongo.yaml
kubectl apply -f mongo-express.yaml

# build and upload docker images
cd $SOURCE_DIR
mvn clean package

cd $SOURCE_DIR/employee-service
docker build -t vik/employee:1.0 .
docker tag vik/employee:1.0 gcr.io/$DEVSHELL_PROJECT_ID/vik/employee:1.0
docker push gcr.io/$DEVSHELL_PROJECT_ID/vik/employee:1.0

cd $SOURCE_DIR/department-service
docker build -t vik/department:1.0 .
docker tag vik/department:1.0 gcr.io/$DEVSHELL_PROJECT_ID/vik/department:1.0
docker push gcr.io/$DEVSHELL_PROJECT_ID/vik/department:1.0

# deploy
cd $SOURCE_DIR/employee-service/k8s
kubectl apply -f springboot-configmap.yaml
kubectl apply -f deployment.yaml

cd $SOURCE_DIR/department-service/k8s
kubectl apply -f deployment.yaml

# ingress setup
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.3.1/deploy/static/provider/cloud/deploy.yaml
kubectl delete validatingwebhookconfigurations ingress-nginx-admission
cd $SOURCE_DIR/k8s
kubectl apply -f ingress-gke.yaml
