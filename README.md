# SAD Evolui

Sistema de Apoio à Decisão para organizar avaliações externas e manuais, analisar feedbacks e priorizar melhorias em aplicativos. Contas possuem projetos e dados isolados; análises heurísticas podem ser revisadas e cada decisão SAW registra os pesos e valores usados.

## Tecnologias

- Backend: Java 17, Spring Boot 3.5, Maven, Spring Web, Spring Data JPA, Spring Security, JWT, Flyway, Jakarta Validation, Lombok, DevTools, Springdoc OpenAPI, JUnit 5 e Mockito.
- Frontend: React, TypeScript, Vite, Axios, React Router, TanStack Query e ESLint.
- Banco: PostgreSQL 17 com Docker Compose.

## Estrutura

```text
sad-evolui/
├── backend/     API Spring organizada em módulos
├── frontend/    Aplicação React organizada por funcionalidades
├── docker-compose.yml
├── .env.example
└── README.md
```

## Pré-requisitos

- Java 21
- Maven 3.6.3 ou superior
- Node.js 20.19+ ou 22.12+
- npm
- Docker com Docker Compose
- Java 17

## Configurar ambiente

Na raiz `sad-evolui`, copie `.env.example` para `.env` e ajuste as credenciais locais. O `.env` é ignorado pelo Git. O Compose carrega esse arquivo automaticamente. Para executar o backend fora do Docker, carregue as variáveis no shell.

```sh
cp .env.example .env
```

## PostgreSQL

Na raiz do projeto:

```sh
docker compose up -d postgres
docker compose ps
```

Os dados persistem no volume Docker `postgres_data`. Para encerrar o serviço, use `docker compose down`.

## Backend

```sh
cd backend
set -a
source ../.env
set +a
mvn spring-boot:run
```

O perfil padrão local é `dev`. O Flyway aplica as migrations ao iniciar. A API estará em `http://localhost:8080`; a documentação Swagger fica em `http://localhost:8080/swagger-ui.html` e o OpenAPI em `http://localhost:8080/v3/api-docs`. Crie uma conta em `/register` antes de usar os módulos protegidos.

## Frontend

```sh
cd frontend
npm install
npm run dev
```

O endereço padrão é `http://localhost:5173`. Para apontar para outra API, defina `VITE_API_URL` no arquivo `frontend/.env.local` (por exemplo, `VITE_API_URL=http://localhost:8080/api`).

Comandos úteis: `npm run lint` e `npm run build`.

## Funcionalidades disponíveis

- Cadastro e login com senha BCrypt e token JWT. Projetos, aplicativos, feedbacks, categorias personalizadas, análises, melhorias e decisões são isolados por conta.
- Análises reproduzíveis: sentimento pela nota (1–2 negativo, 3 neutro, 4–5 positivo), categoria por regras de palavras-chave e grupos candidatos por similaridade Jaccard. A API identifica a versão do método; essas regras não são um modelo treinado. A classificação pode ser corrigida manualmente, mantendo o valor automático original.
- Estatísticas descritivas: média, mediana, moda, mínimo, máximo, desvio padrão populacional, contagens por sentimento e categoria, evolução mensal e problemas agrupados. O dashboard aceita filtros `projectId`, `applicationId`, `from` e `to` em `/api/dashboard/summary`.
- Melhorias com avaliações de impacto, urgência, frequência e esforço de 1 a 5 e origem/justificativa registrada.
- Rodadas SAW com pesos informados pelo gestor que devem totalizar 1. Benefícios usam valor dividido pelo maior; esforço usa menor dividido pelo valor. Histórico guarda pesos, dados originais, normalizações e pontuação.
- Categorias padrão: desempenho, autenticação, interface, navegação, estabilidade, funcionalidades e outros. Contas podem criar categorias próprias.

Execute os testes do backend com `cd backend && mvn test`.

## Configuração de segurança

A API exige JWT em todas as rotas de gestão, exceto cadastro/login, Swagger e recebimento externo de feedbacks. Os recursos de domínio são filtrados pelo usuário do token.

## Receber avaliações por API

Sistemas externos podem registrar avaliações pelo endpoint `POST /api/integrations/feedbacks`. Configure `INTEGRATION_API_KEY` no `.env` com uma chave aleatória longa e compartilhe-a somente com a integração autorizada. O endpoint exige essa chave no header `X-API-Key`; sem ela, retorna `401`.

O corpo usa o identificador do aplicativo já cadastrado, nota de 1 a 5, comentário e data em ISO 8601 UTC. A resposta `201` retorna o feedback criado. Reenvios exatos para o mesmo aplicativo são recusados como duplicidade (`409`). A chave é compartilhada entre as integrações deste ambiente; ela não substitui autenticação de usuário ou isolamento de acesso por cliente.

```sh
curl -X POST http://localhost:8080/api/integrations/feedbacks \
  -H 'Content-Type: application/json' \
  -H 'X-API-Key: SUA_CHAVE_DE_INTEGRACAO' \
  -d '{
    "applicationId": "UUID_DO_APLICATIVO",
    "rating": 5,
    "comment": "A experiência ficou mais rápida.",
    "submittedAt": "2026-11-13T12:00:00Z"
  }'
```
