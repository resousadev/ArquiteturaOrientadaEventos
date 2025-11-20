# ms-checkout

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Gradle](https://img.shields.io/badge/Gradle-8.x-blue.svg)](https://gradle.org/)
[![AWS SDK](https://img.shields.io/badge/AWS%20SDK-2.38.5-yellow.svg)](https://aws.amazon.com/sdk-for-java/)

Microserviço de checkout desenvolvido com Spring Boot 3.5.7, parte de uma arquitetura orientada a eventos utilizando serviços nativos da AWS.

## 📋 Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Tecnologias](#tecnologias)
- [Pré-requisitos](#pré-requisitos)
- [Instalação](#instalação)
- [Como Usar](#como-usar)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Arquitetura](#arquitetura)
- [Desenvolvimento](#desenvolvimento)
- [Testes](#testes)
- [Contribuindo](#contribuindo)

## 🎯 Sobre o Projeto

O **ms-checkout** é um microserviço responsável por operações de checkout em uma arquitetura orientada a eventos. O projeto está em fase inicial de desenvolvimento e utiliza serviços nativos da AWS para comunicação assíncrona e processamento de eventos.

Este microserviço faz parte do projeto **ArquiteturaOrientadaEventos**, que demonstra padrões modernos de desenvolvimento com mensageria e eventos.

## 🚀 Tecnologias

- **Java 21** - Última versão LTS do Java
- **Spring Boot 3.5.7** - Framework para desenvolvimento de aplicações Java
- **Gradle 8.x** - Ferramenta de build e gerenciamento de dependências
- **AWS SDK for Java 2.x** - Integração com serviços AWS
  - Amazon EventBridge - Bus de eventos
  - Amazon SQS - Fila de mensagens (planejado)
- **JUnit 5** - Framework de testes

## 📦 Pré-requisitos

Antes de começar, você precisará ter instalado em sua máquina:

- [Java 21](https://adoptium.net/) ou superior
- [Gradle 8.x](https://gradle.org/install/) (opcional, pois o projeto usa Gradle Wrapper)
- [Git](https://git-scm.com)
- Conta AWS configurada (para recursos de EventBridge e SQS)

## 🔧 Instalação

1. Clone o repositório:
```powershell
git clone git@gh-resousadev:resousadev/ArquiteturaOrientadaEventos.git
cd ms-checkout
```

2. Compile o projeto:
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
```

### Executar testes

```powershell
.\gradlew.bat test
```

## 📁 Estrutura do Projeto

```
ms-checkout/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── io/
│   │   │       └── resousadev/
│   │   │           └── linuxtips/
│   │   │               └── mscheckout/
│   │   │                   └── MsCheckoutApplication.java
│   │   └── resources/
│   │       ├── application.yaml
│   │       ├── static/
│   │       └── templates/
│   └── test/
│       └── java/
│           └── io/
│               └── resousadev/
│                   └── linuxtips/
│                       └── mscheckout/
│                           └── MsCheckoutApplicationTests.java
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
└── README.md
```

### ⚠️ Convenção de Nomenclatura

O pacote Java utiliza `mscheckout` (sem separadores), pois hífens não são permitidos em nomes de pacotes Java. Ao criar novas classes, sempre use:

```java
package io.resousadev.linuxtips.mscheckout;
```

## 🏗️ Arquitetura

### Arquitetura Orientada a Eventos com AWS

O microserviço utiliza serviços nativos da AWS para implementar uma arquitetura orientada a eventos:

```
┌─────────────────┐
│  ms-checkout    │
│                 │
│  ┌───────────┐  │      ┌──────────────────┐
│  │ REST API  │──┼─────→│ Amazon           │
│  └───────────┘  │      │ EventBridge      │
│                 │      └──────────────────┘
│  ┌───────────┐  │              │
│  │ Event     │←─┼──────────────┘
│  │ Consumer  │  │      ┌──────────────────┐
│  └───────────┘  │←─────│ Amazon SQS       │
└─────────────────┘      └──────────────────┘
```

**Componentes Principais:**

- **Amazon EventBridge**: Bus de eventos para publicação e roteamento de eventos de domínio
- **Amazon SQS**: Filas de mensagens para processamento assíncrono
- **AWS SDK for Java 2.x**: Integração com serviços AWS

### Próximos Passos da Arquitetura

- [ ] Implementar publishers de eventos do EventBridge para eventos do domínio de checkout
- [ ] Configurar consumers SQS para processamento assíncrono
- [ ] Adicionar endpoints REST para operações de checkout
- [ ] Configurar credenciais AWS e região no `application.yaml`
- [ ] Implementar camadas de serviço e repositório

## 🛠️ Desenvolvimento

### Estrutura de Pacotes Recomendada

```
io.resousadev.linuxtips.mscheckout/
├── config/          # Configurações Spring e AWS
├── controller/      # Endpoints REST
├── service/         # Lógica de negócio
├── repository/      # Acesso a dados
├── domain/          # Entidades e modelos
├── events/          # Publishers e Consumers de eventos
│   ├── publisher/
│   └── consumer/
└── dto/            # Data Transfer Objects
```

### Configuração AWS

Adicione as seguintes propriedades ao `application.yaml`:

```yaml
aws:
  region: us-east-1
  eventbridge:
    bus-name: checkout-events
  sqs:
    queue-url: https://sqs.us-east-1.amazonaws.com/your-queue
```

### Variáveis de Ambiente

Configure as credenciais AWS usando variáveis de ambiente ou AWS CLI:

```powershell
$env:AWS_ACCESS_KEY_ID="your-access-key"
$env:AWS_SECRET_ACCESS_KEY="your-secret-key"
$env:AWS_REGION="us-east-1"
```

## ✅ Testes

O projeto utiliza JUnit 5 para testes unitários e de integração.

```powershell
# Executar todos os testes
.\gradlew.bat test

# Executar com relatório detalhado
.\gradlew.bat test --info

# Executar testes específicos
.\gradlew.bat test --tests "MsCheckoutApplicationTests"
```

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

## 📝 Licença

Este projeto está em desenvolvimento como parte do projeto ArquiteturaOrientadaEventos.

## 👤 Autor

**resousadev**
- Email: resousadev@gmail.com
- GitHub: [@resousadev](https://github.com/resousadev)

## 🔗 Links Úteis

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [AWS SDK for Java 2.x](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/)
- [Amazon EventBridge](https://docs.aws.amazon.com/eventbridge/)
- [Amazon SQS](https://docs.aws.amazon.com/sqs/)
- [Gradle Documentation](https://docs.gradle.org/)

---

⭐ Se este projeto foi útil para você, considere dar uma estrela no repositório!
