# ms-checkout

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Gradle](https://img.shields.io/badge/Gradle-8.x-blue.svg)](https://gradle.org/)
[![AWS SDK](https://img.shields.io/badge/AWS%20SDK-2.38.5-yellow.svg)](https://aws.amazon.com/sdk-for-java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)](https://docs.docker.com/compose/)

Microserviço de checkout desenvolvido com Spring Boot 3.5.7, parte de uma arquitetura orientada a eventos utilizando serviços nativos da AWS.

## 📋 Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Tecnologias](#tecnologias)
- [Pré-requisitos](#pré-requisitos)
- [Instalação](#instalação)
- [Como Usar](#como-usar)
- [API Endpoints](#api-endpoints)
- [Segurança](#segurança)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Arquitetura](#arquitetura)
- [Banco de Dados](#banco-de-dados)
- [Testes](#testes)
- [Contribuindo](#contribuindo)

## 🎯 Sobre o Projeto

O **ms-checkout** é um microserviço responsável por operações de checkout em uma arquitetura orientada a eventos. O projeto utiliza serviços nativos da AWS para comunicação assíncrona e processamento de eventos.

Este microserviço faz parte do projeto **ArquiteturaOrientadaEventos**, que demonstra padrões modernos de desenvolvimento com mensageria e eventos.

### Funcionalidades Implementadas

- ✅ Autenticação e autorização com Spring Security
- ✅ Publicação de eventos de pagamento no Amazon EventBridge
- ✅ Gerenciamento de usuários com roles (ADMIN/USER)
- ✅ Persistência com PostgreSQL e migrações Flyway
- ✅ Interface web com Thymeleaf (login/home)
- ✅ Logging estruturado em JSON com Logstash
- ✅ Testes de integração com Testcontainers
- ✅ Consumo de eventos do Amazon SQS com long polling

## 🚀 Tecnologias

### Core
| Tecnologia | Versão | Descrição |
|------------|--------|-----------|
| Java | 21 | Última versão LTS |
| Spring Boot | 3.5.7 | Framework principal |
| Gradle | 8.x | Build e gerenciamento de dependências |

### Web & Segurança
| Tecnologia | Descrição |
|------------|-----------|
| Spring Web | REST API |
| Spring Security | Autenticação e autorização |
| Spring Validation | Validação de dados |
| Thymeleaf | Templates HTML |

### Persistência
| Tecnologia | Versão | Descrição |
|------------|--------|-----------|
| Spring Data JPA | - | Acesso a dados |
| PostgreSQL | 15 | Banco de dados relacional |
| Flyway | - | Migrações de banco de dados |

### AWS
| Tecnologia | Versão | Descrição |
|------------|--------|-----------|
| AWS SDK EventBridge | 2.38.5 | Publicação de eventos |
| AWS SDK SQS | 2.38.5 | Consumo de mensagens |

### Utilitários
| Tecnologia | Versão | Descrição |
|------------|--------|-----------|
| MapStruct | 1.6.3 | Mapeamento de objetos |
| Lombok | - | Redução de boilerplate |
| Logstash Logback Encoder | 8.0 | Logging JSON estruturado |
| spring-dotenv | 4.0.0 | Variáveis de ambiente |

### Qualidade & Testes
| Tecnologia | Versão | Descrição |
|------------|--------|-----------|
| JUnit 5 | - | Framework de testes |
| Testcontainers | - | Testes de integração com Docker |
| Checkstyle | 10.12.5 | Análise estática de código |

## 📦 Pré-requisitos

Antes de começar, você precisará ter instalado em sua máquina:

- [Java 21](https://adoptium.net/) ou superior
- [Docker](https://www.docker.com/) e Docker Compose
- [Git](https://git-scm.com)
- Conta AWS configurada (para recursos de EventBridge)

> **Nota:** Gradle 8.x é opcional, pois o projeto usa Gradle Wrapper.

## 🔧 Instalação

### 1. Clone o repositório

```powershell
git clone git@gh-resousadev:resousadev/ArquiteturaOrientadaEventos.git
cd ms-checkout
```

### 2. Configure as variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto:

```env
# Banco de Dados
POSTGRES_USER=checkout_user
POSTGRES_PASSWORD=checkout_pass
POSTGRES_DB=checkout_db

# AWS
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
AWS_REGION=us-east-1

# Servidor (opcional)
SERVER_PORT=8080
```

### 3. Inicie o banco de dados

```powershell
docker-compose up -d
```

### 4. Compile o projeto

```powershell
.\gradlew.bat build
```

## 💻 Como Usar

### Executar a aplicação

```powershell
.\gradlew.bat bootRun
```

A aplicação estará disponível em `http://localhost:8080`

### Build do projeto

```powershell
# Build completo
.\gradlew.bat build

# Clean build
.\gradlew.bat clean build

# Build sem testes
.\gradlew.bat build -x test
```

### Verificação de código (Checkstyle)

```powershell
.\gradlew.bat checkstyleMain checkstyleTest
```

## 🔌 API Endpoints

### Checkout

| Método | Endpoint | Descrição | Autenticação |
|--------|----------|-----------|--------------|
| POST | `/v1/mscheckout/orders` | Cria um novo pedido e publica evento | Sim |

**Exemplo de Request:**
```json
{
  "origem": "web",
  "valor": 150.00,
  "status": "PENDING"
}
```

### Usuários

| Método | Endpoint | Descrição | Role Necessária |
|--------|----------|-----------|-----------------|
| GET | `/usuarios` | Lista todos os usuários | ADMIN |
| POST | `/usuarios` | Cria novo usuário | ADMIN |
| DELETE | `/usuarios/{id}` | Remove usuário | ADMIN |

**Exemplo de Request (POST /usuarios):**
```json
{
  "login": "novouser",
  "senha": "senha123",
  "roles": ["USER"]
}
```

### Interface Web

| Endpoint | Descrição |
|----------|-----------|
| `/login` | Página de login |
| `/home` | Página inicial (requer autenticação) |

## 🔐 Segurança

O projeto utiliza Spring Security com autenticação baseada em formulário.

### Configuração de Autenticação

- **Método**: Form Login
- **Encoding de Senha**: BCrypt
- **Sessão**: Baseada em cookie

### Usuários de Desenvolvimento

> ⚠️ **Atenção**: Estes usuários são apenas para desenvolvimento local.

| Usuário | Senha | Roles |
|---------|-------|-------|
| admin | admin123 | ADMIN, USER |
| user | user123 | USER |

### Roles e Permissões

| Role | Permissões |
|------|------------|
| ADMIN | Acesso total, gerenciamento de usuários |
| USER | Acesso às funcionalidades básicas de checkout |

## 📁 Estrutura do Projeto

```
ms-checkout/
├── src/
│   ├── main/
│   │   ├── java/io/resousadev/linuxtips/mscheckout/
│   │   │   ├── MsCheckoutApplication.java
│   │   │   ├── config/
│   │   │   │   ├── AwsConfig.java            # Cliente EventBridge
│   │   │   │   ├── SecurityConfig.java       # Spring Security
│   │   │   │   ├── CorrelationIdFilter.java  # Rastreamento de requests
│   │   │   │   └── WebConfig.java            # Configuração MVC
│   │   │   ├── controller/
│   │   │   │   ├── CheckoutController.java   # API de checkout
│   │   │   │   ├── UsuarioController.java    # CRUD de usuários
│   │   │   │   └── WebController.java        # Views Thymeleaf
│   │   │   ├── service/
│   │   │   │   └── UsuarioService.java       # Lógica de usuários
│   │   │   ├── repository/
│   │   │   │   └── UsuarioRepository.java    # JPA Repository
│   │   │   ├── domain/
│   │   │   │   ├── Usuario.java              # Entidade JPA
│   │   │   │   └── PagamentoEvent.java       # Evento de pagamento
│   │   │   ├── dto/
│   │   │   │   └── UsuarioDTO.java           # DTO de usuário
│   │   │   ├── mapper/
│   │   │   │   └── UsuarioMapper.java        # MapStruct mapper
│   │   │   ├── producer/
│   │   │   │   └── PagamentoEventProducer.java  # Publisher EventBridge
│   │   │   └── consumer/
│   │   │       └── SqsMessageConsumer.java   # SQS Consumer
│   │   └── resources/
│   │       ├── application.yaml
│   │       ├── logback-spring.xml
│   │       ├── db/migration/
│   │       │   ├── V1__create_schema.sql
│   │       │   └── V2__create_usuarios_table.sql
│   │       └── templates/
│   │           ├── login.html
│   │           └── home.html
│   └── test/
│       ├── java/io/resousadev/linuxtips/mscheckout/
│       │   ├── MsCheckoutApplicationTests.java
│       │   └── config/
│       │       └── DockerAvailableCondition.java
│       └── resources/
│           ├── application-test.yaml
│           └── logback-test.xml
├── config/checkstyle/
│   ├── checkstyle.xml
│   └── suppressions.xml
├── docker-compose.yml
├── build.gradle
├── settings.gradle
└── README.md
```

### ⚠️ Convenção de Nomenclatura

O pacote Java utiliza `mscheckout` (sem separadores), pois hífens não são permitidos em nomes de pacotes Java:

```java
package io.resousadev.linuxtips.mscheckout;
```

## 🏗️ Arquitetura

### Arquitetura Orientada a Eventos com AWS

```
┌─────────────────────────────────────────────────────────────┐
│                      ms-checkout                             │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────┐  │
│  │  Controller │───→│   Service   │───→│   Repository    │  │
│  └─────────────┘    └─────────────┘    └─────────────────┘  │
│         │                                       │            │
│         │                                       ▼            │
│         │                              ┌─────────────────┐  │
│         │                              │   PostgreSQL    │  │
│         │                              └─────────────────┘  │
│         ▼                                                    │
│  ┌─────────────────┐                                        │
│  │ PagamentoEvent  │                                        │
│  │    Producer     │                                        │
│  └────────┬────────┘                                        │
│           │                                                  │
│           │         ┌─────────────────┐                     │
│           │         │ SqsMessage      │                     │
│           │         │   Consumer      │◄────────────────┐   │
│           │         └─────────────────┘                 │   │
└───────────┼─────────────────────────────────────────────┼───┘
            │                                             │
            ▼                                             │
   ┌──────────────────┐      ┌──────────────────┐        │
   │ Amazon           │      │ Amazon SQS       │        │
   │ EventBridge      │─────►│ (checkout-events │────────┘
   │ (status-pedido)  │      │     -queue)      │
   └──────────────────┘      └──────────────────┘
```

**Componentes Implementados:**

- **Controllers**: REST API e views Thymeleaf
- **Services**: Lógica de negócio com validação
- **Repositories**: Persistência com Spring Data JPA
- **Event Producers**: Publicação de eventos no EventBridge
- **Event Consumers**: Consumo de mensagens SQS com long polling
- **Security**: Autenticação e autorização com Spring Security

### Próximos Passos

- [x] ~~Implementar consumers SQS para processamento assíncrono~~
- [ ] Implementar lógica de negócio no SQS consumer
- [ ] Adicionar mais eventos de domínio (OrderCreated, OrderCompleted)
- [ ] Implementar circuit breaker com Resilience4j
- [ ] Adicionar métricas com Micrometer

## 🗄️ Banco de Dados

### Configuração

O projeto utiliza PostgreSQL 15 com Flyway para migrações.

**Docker Compose:**
```powershell
# Iniciar banco de dados
docker-compose up -d

# Parar banco de dados
docker-compose down

# Parar e remover volumes
docker-compose down -v
```

### Migrações

| Versão | Arquivo | Descrição |
|--------|---------|-----------|
| V1 | `V1__create_schema.sql` | Cria schema `checkout` e extensão UUID |
| V2 | `V2__create_usuarios_table.sql` | Cria tabelas `usuarios` e `usuario_roles` |

### Modelo de Dados

```sql
-- Schema: checkout

usuarios
├── id (UUID, PK)
├── login (VARCHAR, UNIQUE)
└── senha (VARCHAR)

usuario_roles
├── usuario_id (UUID, FK)
└── role (VARCHAR)
```

## ✅ Testes

O projeto utiliza JUnit 5 e Testcontainers para testes de integração.

### Executar testes

```powershell
# Executar todos os testes
.\gradlew.bat test

# Executar com relatório detalhado
.\gradlew.bat test --info

# Executar testes específicos
.\gradlew.bat test --tests "MsCheckoutApplicationTests"
```

### Testcontainers

Os testes de integração utilizam Testcontainers com PostgreSQL. **Docker deve estar em execução** para rodar os testes.

Se Docker não estiver disponível, os testes serão automaticamente ignorados (via `DockerAvailableCondition`).

### Relatórios

Após a execução dos testes, os relatórios estarão disponíveis em:
- `build/reports/tests/test/index.html`

## 🤝 Contribuindo

Contribuições são sempre bem-vindas! Para contribuir:

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

### Padrões de Commit

- `feat:` nova funcionalidade
- `fix:` correção de bug
- `docs:` documentação
- `test:` testes
- `refactor:` refatoração de código
- `chore:` tarefas de manutenção

## ⚙️ Variáveis de Ambiente

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `SERVER_PORT` | Porta do servidor | 8080 |
| `POSTGRES_USER` | Usuário do PostgreSQL | - |
| `POSTGRES_PASSWORD` | Senha do PostgreSQL | - |
| `POSTGRES_DB` | Nome do banco | - |
| `AWS_ACCESS_KEY_ID` | Chave de acesso AWS | - |
| `AWS_SECRET_ACCESS_KEY` | Chave secreta AWS | - |
| `AWS_REGION` | Região AWS | us-east-1 |
| `AWS_ENDPOINT` | Endpoint AWS (LocalStack) | - |
| `SQS_QUEUE_URL` | URL da fila SQS | - |
| `SQS_QUEUE_NAME` | Nome da fila SQS | checkout-events-queue |

## 📝 Licença

Este projeto está em desenvolvimento como parte do projeto ArquiteturaOrientadaEventos.

## 👤 Autor

**resousadev**
- Email: resousadev@gmail.com
- GitHub: [@resousadev](https://github.com/resousadev)

## 🔗 Links Úteis

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [AWS SDK for Java 2.x](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/)
- [Amazon EventBridge](https://docs.aws.amazon.com/eventbridge/)
- [Flyway Documentation](https://documentation.red-gate.com/fd)
- [Testcontainers](https://testcontainers.com/)
- [Gradle Documentation](https://docs.gradle.org/)

---

⭐ Se este projeto foi útil para você, considere dar uma estrela no repositório!
