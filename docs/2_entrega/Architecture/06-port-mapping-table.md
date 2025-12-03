# 06 Port Mapping Table

Portas definidas para todas as instâncias.  
Cada serviço mantém a sua porta original, e as instâncias seguintes aumentam 6 e 12 unidades, evitando conflitos e garantindo consistência no esquema de deployment.

| Microserviço           | Instância 1 | Instância 2 | Instância 3 |
|------------------------|-------------|-------------|-------------|
| **Physician Service**  | 8081        | 8087        | 8093        |
| **Clinical Records**   | 8082        | 8088        | 8094        |
| **Patient Service**    | 8083        | 8089        | 8095        |
| **Scheduling Service** | 8084        | 8090        | 8096        |
| **Identity Service**   | 8085        | 8091        | 8097        |
| **API Gateway**        | 8086        | 8092        | 8098        |

## Regra usada

- **Instância 1** → porta original do microserviço
- **Instância 2** → porta original + **6**
- **Instância 3** → porta original + **12**
