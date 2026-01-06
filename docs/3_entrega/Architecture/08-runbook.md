# 08 – Runbook (Comandos Operacionais)
## 0. Objetivo
Este documento funciona como runbook operacional do sistema SIDIS/HAP. Contém apenas comandos prontos a copiar/colar para build, execução com Docker Compose, execução em Kubernetes, validações básicas (health, segurança, observability) e testes rápidos estilo Postman/curl. Serve para desenvolvimento, demonstração e defesa oral.

---

## 1. Build dos serviços (local)
### 1.1 Build Java
Executar em cada serviço ou num script agregado:
```bash
./mvnw clean package -DskipTests
```
Ou, sem wrapper:
```bash
mvn clean package -DskipTests
```
Critério de sucesso: JAR gerado em target/ sem erros.

---

## 2. Docker
### 2.1 Build das imagens Docker
Na raiz do projeto:
```bash
docker build -t sidis/api-gateway ./api-gateway
docker build -t sidis/identity-service ./identity-service
docker build -t sidis/patient-service ./patient-service
docker build -t sidis/physician-service ./physician-service
docker build -t sidis/scheduling-service ./scheduling-service
docker build -t sidis/clinical-records-service ./clinical-records-service
```
Ver imagens:
```bash
docker images | grep sidis
```
---
## 3. Docker Compose
### 3.1 Subir a stack
```bash
docker compose up -d
```
Ou:
```bash
docker-compose up -d
```
### 3.2 Verificar containers
```bash
docker ps
```
Confirmar UP: api-gateway, identity-service, patient-service, physician-service, scheduling-service, clinical-records-service, rabbitmq, prometheus, grafana, jaeger, logging stack (se aplicável).
### 3.3 Parar a stack
```bash
docker compose down
```
---
## 4. Kubernetes (se aplicável)
### 4.1 Criar namespace
```bash
kubectl create namespace sidis
```
### 4.2 Aplicar manifests
```bash
kubectl apply -n sidis -f k8s/configmaps/
kubectl apply -n sidis -f k8s/secrets/
kubectl apply -n sidis -f k8s/deployments/
kubectl apply -n sidis -f k8s/services/
kubectl apply -n sidis -f k8s/ingress/
```
### 4.3 Ver estado
```bash
kubectl get pods -n sidis
kubectl get svc -n sidis
kubectl get ingress -n sidis
```
### 4.4 Ver logs de um pod
```bash
kubectl logs -n sidis <pod-name>
```
### 4.5 Escalar serviço (demo)
```bash
kubectl scale deployment scheduling-service --replicas=3 -n sidis
```
---
## 5. Health Checks
```bash
curl http://localhost:8080/actuator/health
```
Resposta esperada:
```json
{"status":"UP"}
```
---
## 6. Segurança (curl / Postman)
### 6.1 Login
```bash
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"username":"patient1","password":"password"}'
```
Guardar access_token.
### 6.2 Request sem token (401)
```bash
curl http://localhost:8080/consultas
```
### 6.3 Token sem permissões (403)
```bash
curl http://localhost:8080/records -H "Authorization: Bearer <PATIENT_TOKEN>"
```
### 6.4 Token válido (sucesso)
```bash
curl -X POST http://localhost:8080/consultas -H "Authorization: Bearer <VALID_TOKEN>" -H "Content-Type: application/json" -d '{"patientId":"uuid-patient","physicianId":"uuid-physician","date":"2026-01-10T10:00:00"}'
```
---
## 7. Observability
### 7.1 Prometheus
http://localhost:9090
### 7.2 Grafana
http://localhost:3000
### 7.3 Jaeger
http://localhost:16686
### 7.4 Logs centralizados
http://localhost:5601

---
## 8. Resiliência
### 8.1 Desligar serviço
```bash
docker stop physician-service-1
```
Executar novamente:
```bash
curl -X POST http://localhost:8080/consultas -H "Authorization: Bearer <VALID_TOKEN>"
```
### 8.2 Recuperar serviço
```bash
docker start physician-service-1
```
Repetir request e confirmar recuperação.

---
## 9. CI/CD
GitHub → Actions → workflow → run
Mostrar build, test, docker build/push e deploy ou artefactos.
