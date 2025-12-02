# 05 – Deployment Multi-Instance (Microservices + CQRS + Broker)

## 0. Objetivo do documento

Este documento descreve a estratégia de **deployment distribuído** para a Parte 2 do projeto SIDIS, cumprindo os requisitos:

- Cada microserviço tem **≥ 2 instâncias** em execução simultaneamente.
- Cada instância possui **o seu próprio read model** (database-per-microservice-instance).
- Todas as instâncias comunicam via **broker AMQP (RabbitMQ)** para propagação de eventos e Sagas.
- O API Gateway realiza **load balancing** entre instâncias.
- O sistema suporta **consistência eventual**, falhas parciais e reinícios transparentes.

---

# 1. Arquitectura geral do deployment

A arquitetura final do sistema com múltiplas instâncias é:

# 2. Estratégia de deployment

## 2.1 Tecnologias recomendadas

- **Docker Compose** para desenvolvimento e entrega da UC.
- Possível usar **Docker Swarm** para scaling automático, mas não é obrigatório.

---

# 3. Estrutura dos ficheiros de configuração

 ``` deploy/
├── docker-compose.yml
├── gateway/
│ └── application.yml
├── patient/
│ ├── application-instance-1.yml
│ ├── application-instance-2.yml
├── physician/
│ ├── application-instance-1.yml
│ ├── application-instance-2.yml
├── scheduling/
│ ├── application-instance-1.yml
│ ├── application-instance-2.yml
└── clinical-records/
├── application-instance-1.yml
├── application-instance-2.yml
```

Cada instância tem: 

- Porta distinta
- Base de dados de read model distinta
- Fila AMQP distinta

---

# 4. Configuração por instância

Exemplo real para **Patient Service**:

### Instância 1 (`application-instance-1.yml`)

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:postgresql://patient-read-db-1:5432/patient
    username: postgres
    password: pass

rabbitmq:
  queue: patient-service.instance-1.events
Instância 2 (application-instance-2.yml)
yaml
Copy code
server:
  port: 8082

spring:
  datasource:
    url: jdbc:postgresql://patient-read-db-2:5432/patient
    username: postgres
    password: pass

rabbitmq:
  queue: patient-service.instance-2.events

```


### Instância 2 (application-instance-2.yml)
 
```
server:
port: 8082

spring:
datasource:
url: jdbc:postgresql://patient-read-db-2:5432/patient
username: postgres
password: pass

rabbitmq:
queue: patient-service.instance-2.events
```

# 5. Docker Compose (multi-instância com read DB por instância)

version: "3.9"

services:

# ===========================
# RabbitMQ Broker
# ===========================
rabbitmq:
image: rabbitmq:3-management
ports:
- "5672:5672"
- "15672:15672"
environment:
RABBITMQ_DEFAULT_USER: guest
RABBITMQ_DEFAULT_PASS: guest

# ===========================
# Patient Service – 2 instâncias
# ===========================
patient-service-1:
image: patient-service:latest
container_name: patient-service-1
environment:
SPRING_CONFIG_LOCATION: classpath:/application-instance-1.yml
ports:
- "8081:8081"
depends_on:
- rabbitmq
- patient-read-db-1

patient-service-2:
image: patient-service:latest
container_name: patient-service-2
environment:
SPRING_CONFIG_LOCATION: classpath:/application-instance-2.yml
ports:
- "8082:8082"
depends_on:
- rabbitmq
- patient-read-db-2

patient-read-db-1:
image: postgres:15
container_name: patient-read-db-1
environment:
POSTGRES_PASSWORD: pass
POSTGRES_DB: patient
ports:
- "5431:5432"

patient-read-db-2:
image: postgres:15
container_name: patient-read-db-2
environment:
POSTGRES_PASSWORD: pass
POSTGRES_DB: patient
ports:
- "5432:5432"

# ===========================
# Physician Service
# ===========================
physician-service-1:
image: physician-service:latest
container_name: physician-service-1
environment:
SPRING_CONFIG_LOCATION: classpath:/application-instance-1.yml
ports:
- "8091:8091"
depends_on:
- rabbitmq
- physician-read-db-1

physician-service-2:
image: physician-service:latest
container_name: physician-service-2
environment:
SPRING_CONFIG_LOCATION: classpath:/application-instance-2.yml
ports:
- "8092:8092"
depends_on:
- rabbitmq
- physician-read-db-2

physician-read-db-1:
image: postgres:15
container_name: physician-read-db-1
environment:
POSTGRES_PASSWORD: pass
POSTGRES_DB: physician

physician-read-db-2:
image: postgres:15
container_name: physician-read-db-2
environment:
POSTGRES_PASSWORD: pass
POSTGRES_DB: physician

# ===========================
# Scheduling Service
# ===========================
scheduling-service-1:
image: scheduling-service:latest
container_name: scheduling-service-1
environment:
SPRING_CONFIG_LOCATION: classpath:/application-instance-1.yml
ports:
- "8101:8101"
depends_on:
- rabbitmq
- scheduling-read-db-1

scheduling-service-2:
image: scheduling-service:latest
container_name: scheduling-service-2
environment:
SPRING_CONFIG_LOCATION: classpath:/application-instance-2.yml
ports:
- "8102:8102"
depends_on:
- rabbitmq
- scheduling-read-db-2

scheduling-read-db-1:
image: postgres:15
container_name: scheduling-read-db-1
environment:
POSTGRES_PASSWORD: pass
POSTGRES_DB: scheduling

scheduling-read-db-2:
image: postgres:15
container_name: scheduling-read-db-2
environment:
POSTGRES_PASSWORD: pass
POSTGRES_DB: scheduling

# ===========================
# Clinical Records Service
# ===========================
clinical-records-1:
image: clinical-records-service:latest
container_name: clinical-records-1
environment:
SPRING_CONFIG_LOCATION: classpath:/application-instance-1.yml
ports:
- "8111:8111"
depends_on:
- rabbitmq
- records-read-db-1

clinical-records-2:
image: clinical-records-service:latest
container_name: clinical-records-2
environment:
SPRING_CONFIG_LOCATION: classpath:/application-instance-2.yml
ports:
- "8112:8112"
depends_on:
- rabbitmq
- records-read-db-2

records-read-db-1:
image: postgres:15
container_name: records-read-db-1
environment:
POSTGRES_PASSWORD: pass
POSTGRES_DB: records

records-read-db-2:
image: postgres:15
container_name: records-read-db-2
environment:
POSTGRES_PASSWORD: pass
POSTGRES_DB: records

# ===========================
# API Gateway
# ===========================
api-gateway:
image: api-gateway:latest
container_name: api-gateway
ports:
- "80:8080"
depends_on:
- patient-service-1
- patient-service-2
- physician-service-1
- physician-service-2
- scheduling-service-1
- scheduling-service-2
- clinical-records-1
- clinical-records-2

# 6. Load Balancing (API Gateway)

O gateway deve ter:

patient-service:
instances:
- http://patient-service-1:8081
- http://patient-service-2:8082

Estratégias aceitáveis:

- Round-robin
- Random
- Least-connections

Caso uma instância falhe > gateway redireciona automaticamente para as restantes.

# 7. Arranque e shutdown

Ordem recomendada de arranque:

1. RabbitMQ
2. Read DBs
3. Instâncias dos microserviços
4. API Gateway

### Shutdown seguro

- Cada instância deve terminar normalmente: o próximo arranque sincroniza com novos eventos e reconstrói o read model.


# 8. Testes distribuídos obrigatórios
## 8.1 Load balancing

Fazer 20 GET ao mesmo endpoint > verificar que respostas alternam entre instâncias.

## 8.2 Eventos

Criar uma consulta > verificar no RabbitMQ que:

- O evento foi publicado
- Ambas as instâncias consumiram

## 8.3 Consistência eventual

Criar consulta e imediatamente fazer GET nas duas instâncias > pode haver atraso > esperado.

## 8.4 Falha de instância

Derrubar scheduling-service-1 > scheduling-service-2 continua funcional.

## 8.5 Sagas

Testar agendamento e cancelamento com:

- Paciente inválido
- Médico indisponível
- Records existentes
- Records inexistentes


# 9. Resumo

Este documento define:

- Deployment com múltiplas instâncias por serviço
- Read models por instância
- Ligação central ao RabbitMQ
- Load balancing no API Gateway
- Testes distribuídos essenciais
- 