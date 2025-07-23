# API de Autenticação

[![Java](https://img.shields.io/badge/Java-17-red?logo=java&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15.x-blue?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.x-ff6600?logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)](https://www.docker.com/)
[![Maven](https://img.shields.io/badge/Maven-3.9.x-orange?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![JWT](https://img.shields.io/badge/JWT-io.jsonwebtoken-9900cc)](https://github.com/jwtk/jjwt)
[![Passay](https://img.shields.io/badge/Passay-password--validation-yellow)](https://www.passay.org/)
[![Bucket4j](https://img.shields.io/badge/Bucket4j-rate--limiting-blueviolet)](https://bucket4j.com/)

Uma aplicação Spring Boot que fornece autenticação baseada em JWT, incluindo registro de usuário, login, verificação de e-mail e recuperação de senha com notificações por e-mail.

## Funcionalidades

- Registro de Usuário com Verificação de E-mail
- Autenticação baseada em JWT
- Fluxo de Recuperação de Senha
- Notificações por E-mail
- Integração com Banco de Dados PostgreSQL
- Suporte a Docker
- Documentação da API

## Arquitetura do Sistema

![Relações do Banco de Dados](exemplos/sql-relations.png)

---

## Pré-requisitos

- Java 17
- Docker & Docker Compose
- PostgreSQL (se executando localmente)

---

## Executando com Docker Compose

1.  Clone o repositório:
    ```bash
    git clone https://github.com/your-username/auth-api.git
    ```
2.  Navegue até o diretório do projeto:
    ```bash
    cd auth-api
    ```
3.  Inicie os contêineres:
    ```bash
    docker-compose up -d
    ```

Isto iniciará os seguintes serviços:

-   **PostgreSQL** – acessível na porta `5432`
-   **pgAdmin** – acessível em `http://localhost:5050`
-   **AuthAPI** – disponível na porta `8080`

---

## Acesso ao Banco de Dados (pgAdmin)

**Credenciais de Login:**

-   **Email:** `admin@admin.com`
-   **Senha:** `admin`

**Para conectar ao PostgreSQL:**

-   **Host:** `postgres`
-   **Porta:** `5432`
-   **Banco de Dados:** `auth_db`
-   **Usuário:** `postgres`
-   **Senha:** `admin`

---

## Endpoints da API

### 1. Registro de Usuário

```http
POST /api/auth/register
```

Registra um novo usuário e envia um e-mail de verificação.

**Corpo da Requisição:**

```json
{
    "username": "guilherme",
    "password": "Guilherme@123!",
    "email": "guilherme@gmail.com"
}
```

### 2. Login de Usuário

```http
POST /api/auth/login
```

Autentica um usuário e retorna um token JWT.

**Corpo da Requisição:**

```json
{
    "email": "guilherme@gmail.com",
    "password": "Guilherme@123!"
}
```

### 3. Esqueceu a Senha

```http
POST /api/auth/forgot-password?email=guilherme@gmail.com
```

Solicita um link de redefinição de senha por e-mail.

### 4. Redefinir Senha

```http
POST /api/auth/reset-password
```

Redefine a senha usando o token recebido por e-mail.

**Corpo da Requisição:**

```json
{
    "email": "guilherme@gmail.com",
    "token": "seu-token-de-reset",
    "newPassword": "NovaSenha@2024!"
}
```

### 5. Verificação de E-mail

```http
GET /api/auth/verify-email?token=seu-token-de-verificacao
```

Verifica o endereço de e-mail do usuário usando o token recebido.

### 6. Reenviar E-mail de Verificação

```http
POST /api/auth/resend-verification?email=guilherme@gmail.com
```

Solicita um novo e-mail de verificação.

### 7. Verificação de Saúde (Health Check)

```http
GET /api/auth/health
```

Verifica o status da API.

---

## Exemplos de Notificação por E-mail

### E-mail de Confirmação de Registro

![E-mail de Registro](exemplos/img_1.png)

### Verificação de E-mail

![Verificação de E-mail](exemplos/img_2.png)

### Solicitação de Redefinição de Senha

![Redefinição de Senha](exemplos/img_3.png)

### Confirmação de Redefinição de Senha

![Confirmação de Redefinição](exemplos/img_4.png)

---

## Configuração de Ambiente

As variáveis de ambiente são definidas no arquivo `.env`, incluindo:

-   Configuração do banco de dados PostgreSQL
-   Segredo e tempo de expiração do JWT
-   Porta do servidor
-   Configuração do serviço de e-mail
-   Configurações do RabbitMQ

---

## Executando Localmente (sem Docker)

1.  Certifique-se de que o PostgreSQL está rodando localmente.
2.  Atualize o arquivo `src/main/resources/application.properties` com as credenciais do seu banco de dados local.
3.  Execute a aplicação:
    ```bash
    ./mvnw spring-boot:run
    ```

---

## Construindo a Aplicação

```bash
./mvnw clean package
```

Isso irá gerar um arquivo JAR no diretório `target/`.

---

## Funcionalidades de Segurança

-   Autenticação baseada em JWT
-   Criptografia de senha usando BCrypt
-   Verificação de e-mail
-   Redefinição de senha baseada em token
-   Proteção contra CORS e CSRF
-   Limitação de taxa de requisições (Rate Limiting)
-   Validação de senha segura

---

## Json das Requisições

Uma coleção completa do Postman está disponível no arquivo `exemplos/auth-collection.json`. Importe esta coleção no Postman para testar todos os endpoints disponíveis.

---
