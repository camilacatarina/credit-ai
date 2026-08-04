# Credit AI - Motor de Decisão de Crédito Bancário

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.3-green)
![Spring Security](https://img.shields.io/badge/Spring_Security-JWT-blue)
![Postgres](https://img.shields.io/badge/Database-H2/PostgreSQL-important)
![Maven](https://img.shields.io/badge/Build-Maven-orange)

Sistema backend desenvolvido em Spring Boot para simular a concessão de crédito, aplicando regras financeiras reais. O sistema possui autenticação robusta via JWT, cálculo de score de crédito (0 a 1000) baseado em renda, idade e tempo de emprego, além de validações rigorosas de comprometimento de renda.

---

## Tecnologias e Arquitetura

| Categoria | Tecnologias |
| :--- | :--- |
| **Linguagem** | Java 17 |
| **Framework** | Spring Boot 3.4.3 (Web, Data JPA, Security, Cache) |
| **Segurança** | Spring Security, JWT (JSON Web Token) |
| **Banco de Dados** | H2 (Desenvolvimento) / PostgreSQL (Produção) |
| **Boas Práticas** | Clean Architecture (DTOs, Enums, Service, Controller), Lombok, Padrão REST |
| **Documentação** | Swagger (OpenAPI) |
| **Build** | Maven |

---

## Funcionalidades Principais

*   🔐 **Autenticação e Autorização:** Sistema seguro com geração de Tokens JWT e login de usuários.
*   💰 **Regra de Comprometimento de Renda:** Utiliza a premissa bancária clássica para análise de capacidade de pagamento.
*   📊 **Score Híbrido de Crédito:** Cálculo de uma pontuação de 0 a 1000 baseada em pesos estratégicos:
    *   **Renda Mensal** (Máximo 400 pontos)
    *   **Endividamento** (Máximo 300 pontos)
    *   **Estabilidade Profissional** (Máximo 200 pontos)
    *   **Faixa Etária** (Máximo 100 pontos)
*   📝 **Análise Detalhada:** Além do status de aprovação (`APROVADO`, `REPROVADO`, `ANALISE_MANUAL`), a API retorna motivos claros e recomendações financeiras personalizadas.
*   ⚙️ **Tratamento Global de Erros:** Arquitetura `@RestControllerAdvice` para devolver mensagens amigáveis em caso de requisições inválidas ou credenciais erradas.

---

## Pré-requisitos

*   Java 17 ou superior
*   Maven instalado
*   IntelliJ IDEA ou VS Code (recomendado)

---

## Como executar o projeto localmente

1.  **Clone o repositório:**
    ```bash
    git clone git@github.com:camilacatarina/credit-ai.git