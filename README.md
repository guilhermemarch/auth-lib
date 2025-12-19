# API de Autenticação com JWT

[![Java](https://img.shields.io/badge/Java-17-red?logo=java&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15.x-blue?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.x-ff6600?logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)](https://www.docker.com/)
[![Maven](https://img.shields.io/badge/Maven-3.9.x-orange?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![JWT](https://img.shields.io/badge/JWT-io.jsonwebtoken-9900cc)](https://github.com/jwtk/jjwt)

API RESTful robusta para autenticação de usuários, construída com Spring Boot 3.4.4. Oferece autenticação baseada em JWT, verificação de email, recuperação de senha, rate limiting e notificações assíncronas via RabbitMQ.

---

A API estará disponível em `http://localhost:8080` com PostgreSQL e RabbitMQ configurados automaticamente.

---

## Funcionalidades

### Autenticação e Autorização
- Registro de usuário com validação de senha forte
- Verificação de email obrigatória com tokens seguros
- Login baseado em JWT com tokens de acesso
- Recuperação de senha via email com tokens temporários
- Reenvio de email de verificação

### Segurança
- Criptografia de senha com BCrypt
- Autenticação JWT com tokens assinados
- Rate limiting usando Bucket4j (proteção contra brute force)
- Validação de senha com Passay (regras de complexidade)
- Configuração de CORS e CSRF

### Infraestrutura
- Notificações assíncronas via RabbitMQ
- Templates de email profissionais em HTML
- Docker Compose para deploy simplificado
- PostgreSQL com persistência de dados
- pgAdmin para gerenciamento do banco

---

## Arquitetura

### Arquitetura do Sistema

```mermaid
graph TB
    subgraph "Client Layer"
        Client[Cliente/Frontend]
    end
    
    subgraph "Application Layer"
        API[Auth API<br/>Spring Boot 3.4.4<br/>Port 8080]
    end
    
    subgraph "Data Layer"
        DB[(PostgreSQL<br/>Port 5432)]
        pgAdmin[pgAdmin<br/>Port 5050]
    end
    
    subgraph "Messaging Layer"
        MQ[RabbitMQ<br/>Port 5672/15672]
        Worker[Email Worker]
    end
    
    subgraph "External Services"
        SMTP[SMTP Server<br/>Email Service]
    end
    
    Client -->|HTTP/REST| API
    API -->|JDBC/JPA| DB
    pgAdmin -.->|Manage| DB
    API -->|Publish Events| MQ
    MQ -->|Consume Events| Worker
    Worker -->|Send Email| SMTP
    SMTP -.->|Email| Client
```

### Fluxo de Autenticação (Registro e Login)

```mermaid
sequenceDiagram
    actor User
    participant API
    participant DB
    participant Queue as RabbitMQ
    participant Worker
    participant Email
    
    Note over User,Email: Registro de Usuário
    User->>+API: POST /api/auth/register<br/>{username, email, password}
    API->>API: Validar senha (Passay)
    API->>API: Hash senha (BCrypt)
    API->>DB: Salvar usuário
    DB-->>API: Usuário criado
    API->>API: Gerar token verificação
    API->>DB: Salvar token
    API->>Queue: Publicar evento email_verification
    API-->>-User: 201 Created
    
    Queue->>Worker: Consumir evento
    Worker->>Email: Enviar email HTML
    Email-->>User: Email de verificação
    
    Note over User,Email: Verificação de Email
    User->>+API: GET /api/auth/verify-email?token=xxx
    API->>DB: Validar token
    API->>DB: Marcar email como verificado
    API-->>-User: Email verificado com sucesso
    
    Note over User,Email: Login
    User->>+API: POST /api/auth/login<br/>{email, password}
    API->>DB: Buscar usuário
    API->>API: Verificar senha (BCrypt)
    API->>API: Verificar email verificado
    API->>API: Gerar JWT token
    API-->>-User: 200 OK {token, expiresIn}
```

### Fluxo de Recuperação de Senha

```mermaid
sequenceDiagram
    actor User
    participant API
    participant DB
    participant Queue as RabbitMQ
    participant Worker
    participant Email
    
    User->>+API: POST /api/auth/forgot-password?email=user@example.com
    API->>DB: Buscar usuário por email
    alt Usuário encontrado
        API->>API: Gerar token reset (UUID)
        API->>DB: Salvar token com expiração (1h)
        API->>Queue: Publicar evento password_reset
        Queue->>Worker: Consumir evento
        Worker->>Email: Enviar email com link
        Email-->>User: Email com token de reset
        API-->>-User: 200 OK
    else Usuário não encontrado
        API-->>User: 200 OK (não revela existência)
    end
    
    User->>+API: POST /api/auth/reset-password<br/>{email, token, newPassword}
    API->>DB: Validar token e expiração
    alt Token válido
        API->>API: Validar nova senha (Passay)
        API->>API: Hash nova senha (BCrypt)
        API->>DB: Atualizar senha
        API->>DB: Invalidar token
        API->>Queue: Publicar evento password_changed
        Queue->>Worker: Consumir evento
        Worker->>Email: Enviar confirmação
        Email-->>User: Email de confirmação
        API-->>-User: 200 OK
    else Token inválido/expirado
        API-->>User: 400 Bad Request
    end
```

---

## Tecnologias

### Backend
| Tecnologia | Versão | Propósito |
|------------|--------|-----------|
| Java | 17 | Linguagem principal |
| Spring Boot | 3.4.4 | Framework web |
| Spring Security | 6.x | Autenticação e autorização |
| Spring Data JPA | 3.x | Persistência de dados |
| Spring AMQP | 3.x | Integração RabbitMQ |
| Hibernate | 6.x | ORM |

### Segurança
| Biblioteca | Versão | Propósito |
|------------|--------|-----------|
| JJWT | 0.11.5 | Geração e validação de JWT |
| BCrypt | - | Hash de senhas |
| Passay | 1.6.4 | Validação de senha forte |
| Bucket4j | 7.6.0 | Rate limiting |

### Infraestrutura
| Serviço | Versão | Propósito |
|---------|--------|-----------|
| PostgreSQL | 15.x | Banco de dados |
| RabbitMQ | 3.x | Message broker |
| Docker | - | Containerização |
| pgAdmin | 4.x | Gerenciamento de BD |
| Maven | 3.9.x | Build tool |

---

## Uso

### Exemplo Completo de Fluxo de Registro e Login

#### 1. Registrar novo usuário

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "guilherme",
    "email": "guilherme@gmail.com",
    "password": "Guilherme@123!"
  }'
```

**Resposta:**
```json
{
  "message": "Usuário registrado com sucesso. Verifique seu email.",
  "userId": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
}
```

#### 2. Verificar email

Após receber o email, clicar no link ou fazer uma requisição:

```bash
curl -X GET "http://localhost:8080/api/auth/verify-email?token=abc123xyz"
```

#### 3. Fazer login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "guilherme@gmail.com",
    "password": "Guilherme@123!"
  }'
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "username": "guilherme",
    "email": "guilherme@gmail.com",
    "verified": true
  }
}
```

#### 4. Usar token JWT em requisições autenticadas

```bash
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## API Endpoints

### Autenticação

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `POST` | `/api/auth/register` | Registrar novo usuário |
| `POST` | `/api/auth/login` | Autenticar usuário e retornar JWT |
| `GET` | `/api/auth/verify-email?token={token}` | Verificar endereço de email |
| `POST` | `/api/auth/forgot-password?email={email}` | Solicitar reset de senha |
| `POST` | `/api/auth/reset-password` | Redefinir senha usando token |
| `POST` | `/api/auth/resend-verification?email={email}` | Reenviar email de verificação |
| `GET` | `/api/auth/health` | Health check da API |

---

## Segurança

### Recursos de Segurança Implementados

#### 1. Rate Limiting (Bucket4j)
Protege contra ataques de força bruta:
- Login: 10 tentativas por minuto por IP
- Registro: 5 registros por minuto por IP
- Recuperação de senha: 3 solicitações por hora por IP

#### 2. Validação de Senha (Passay)
Garante senhas fortes com regras customizáveis

#### 3. Hash de Senha (BCrypt)
- Fator de custo: 12
- Salt automático
- Proteção contra rainbow tables

#### 4. JWT Tokens
- Assinatura HMAC SHA-256
- Expiração configurável (padrão 1h)
- Claims customizados (userId, email, roles)

#### 5. CORS e CSRF
- CORS configurado para origens permitidas
- CSRF desabilitado para API stateless (JWT)

#### 6. Tokens de Verificação
- UUIDs aleatórios seguros
- Expiração de 24 horas (verificação email)
- Expiração de 1 hora (reset senha)
- Uso único (invalidados após uso)

---

## Exemplos de Email

### Email de Confirmação de Registro

![Email de Registro](exemplos/img_1.png)

### Email de Verificação

![Verificação de Email](exemplos/img_2.png)

### Email de Redefinição de Senha

![Redefinição de Senha](exemplos/img_3.png)

### Email de Confirmação de Redefinição

![Confirmação de Redefinição](exemplos/img_4.png)

---

## Recursos Adicionais

- [Coleção do Postman](exemplos/auth-collection.json) - Importe para testar todos os endpoints
- [Diagrama de Banco de Dados](exemplos/sql-relations.png) - Esquema ER completo
