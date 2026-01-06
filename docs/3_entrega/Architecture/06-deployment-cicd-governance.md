# 06 – Deployment, CI/CD and Governance

## 0. Objetivo

Este documento descreve as decisões e práticas adotadas para **deployment**, **automação (CI/CD)** e **governance** do sistema SIDIS/HAP, conforme os requisitos da Parte 3 (P3).

O objetivo é demonstrar que o sistema:
- pode ser construído e executado de forma reprodutível,
- suporta deployments controlados e sem downtime,
- segue convenções e boas práticas de engenharia,
- é governado de forma consistente ao longo do tempo.

---

## 1. Dockerfiles

### 1.1 Objetivo

Garantir que todos os microserviços:
- são empacotados de forma consistente,
- produzem imagens leves e previsíveis,
- seguem boas práticas de segurança e performance.

---

### 1.2 Estratégia adotada

Foram **standardizados os Dockerfiles** de todos os serviços com as seguintes decisões:

- Uso de **multi-stage builds**:
    - stage de build (compilação/testes)
    - stage de runtime (imagem final)
- Base image comum (ex.: JDK/JRE slim)
- Execução da aplicação como utilizador não-root
- Exposição explícita da porta do serviço
- Externalização total de configuração (env vars / config files)

---

### 1.3 O que foi normalizado

- Estrutura comum do Dockerfile em todos os serviços
- Variáveis de ambiente para:
    - configuração de DB
    - RabbitMQ
    - observability
    - segurança
- Healthcheck (quando aplicável)
- Labels básicos (nome do serviço, versão)

---

### 1.4 Critério de aceitação (Docker)

- Todas as imagens são construídas com o mesmo padrão.
- Não existem secrets hardcoded nas imagens.
- As imagens arrancam corretamente em ambientes diferentes (dev/demo).

---

## 2. Kubernetes Manifests

### 2.1 Objetivo

Executar o sistema num ambiente orquestrado, suportando:
- múltiplas instâncias por serviço,
- gestão automática de falhas,
- configuração centralizada,
- escalabilidade controlada.

---

### 2.2 Recursos Kubernetes utilizados

Os manifests incluem, conforme aplicável:

- **Deployment**
    - definição de réplicas (≥ 2 por serviço)
    - estratégia de rollout (RollingUpdate / Blue-Green / Canary)
    - probes de liveness e readiness

- **Service**
    - exposição interna (ClusterIP)
    - load balancing entre pods

- **Ingress**
    - exposição externa via API Gateway
    - routing por path/host

- **ConfigMaps**
    - configuração não sensível
    - parâmetros de aplicação

- **Secrets**
    - credenciais de DB
    - certificados mTLS
    - chaves JWT
    - credenciais do broker

- **HPA (Horizontal Pod Autoscaler)** (se aplicável)
    - escalabilidade baseada em CPU/métricas

---

### 2.3 Configuração por ambiente

A separação por ambiente é garantida através de:
- namespaces distintos (ex.: dev, demo)
- ConfigMaps/Secrets específicos por ambiente
- override de variáveis de ambiente

---

### 2.4 Critério de aceitação (Kubernetes)

- Todos os serviços arrancam no cluster.
- Existem pelo menos 2 réplicas por serviço crítico.
- Pods não prontos são removidos do tráfego.
- Atualizações não causam indisponibilidade global.

---

## 3. CI/CD Pipeline

### 3.1 Objetivo

Automatizar o ciclo de vida do software:
- build
- testes
- empacotamento
- deployment

Reduzindo erros manuais e garantindo consistência.

---

### 3.2 Etapas do pipeline

Pipeline típico (ex.: GitHub Actions):

1. **Checkout**
    - obtenção do código fonte

2. **Build**
    - compilação dos serviços
    - verificação de dependências

3. **Test**
    - execução de testes unitários
    - falha imediata em caso de erro

4. **Package**
    - construção de imagens Docker
    - tagging com versão/commit

5. **Push**
    - envio das imagens para registry (ex.: Docker Hub)

6. **Deploy**
    - aplicação dos manifests Kubernetes
    - rollout controlado

---

### 3.3 Quality Gates no pipeline

- Build falha se:
    - testes falharem
    - linting básico falhar
- Apenas código válido chega à fase de deploy.

---

### 3.4 Critério de aceitação (CI/CD)

- Um commit dispara automaticamente o pipeline.
- Falhas são detetadas antes do deploy.
- O deploy é reproduzível e consistente.

---

## 4. Governance

### 4.1 Convenções e standards

Foram definidas convenções para garantir consistência:

- **Naming**
    - serviços: `*-service`
    - imagens: `<org>/<service>:<version>`
    - recursos k8s: `<service>-deployment`, `<service>-svc`

- **Configuração**
    - environment variables como fonte principal
    - separação clara config vs secret

- **Logs e métricas**
    - formato consistente
    - nomes de métricas padronizados

---

### 4.2 Versionamento

- Versionamento semântico (MAJOR.MINOR.PATCH)
- APIs versionadas (ex.: `/api/v1/...`)
- Mudanças incompatíveis implicam nova versão major

---

### 4.3 Políticas de qualidade

- Pull Requests obrigatórios
- Revisão mínima antes de merge
- Pipeline CI obrigatório para merge
- Testes automáticos como gate de qualidade

---

### 4.4 Políticas de evolução e depreciação

- Alterações de API documentadas previamente
- Período de coexistência entre versões
- Comunicação clara de endpoints deprecados
- Remoção faseada e controlada

---