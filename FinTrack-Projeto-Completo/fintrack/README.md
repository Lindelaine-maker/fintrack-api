# FinTrack API — Projeto 2 (Módulo Avançado)

API RESTful para controle de finanças pessoais, implementada com Java 17, Spring Boot, Spring Data JPA/Hibernate, Spring Security, JWT e Swagger/OpenAPI.

## Requisitos atendidos pelo roteiro

- Entidades `Usuario`, `Categoria` e `Transacao` com JPA.
- Relacionamentos: Usuario 1:N Categoria, Usuario 1:N Transacao e Categoria 1:N Transacao.
- Repositórios JPA e consultas JPQL para período, categoria e somatórios por tipo.
- Controllers REST para autenticação, categorias e transações.
- DTOs `TransacaoDTO`, `CategoriaDTO` e `SaldoDTO`, além dos DTOs de autenticação.
- Camada de serviço para regras financeiras e conversão DTO/entidade (ModelMapper configurado).
- JWT stateless, BCrypt e autorização baseada no usuário autenticado.
- Swagger/OpenAPI com esquema Bearer JWT.
- Tratamento centralizado de erros.

## Regras de negócio implementadas

1. Cada categoria e transação é vinculada ao usuário autenticado; não é aceito ID de usuário enviado pelo cliente.
2. Uma categoria só pode ser usada pelo seu proprietário.
3. O tipo da transação precisa corresponder ao tipo da categoria.
4. O valor deve ser maior que zero.
5. Uma despesa que deixe o saldo negativo retorna HTTP 409.
6. E-mail é único; categorias com o mesmo nome não podem ser repetidas para o mesmo usuário.

## Executar

Pré-requisitos: JDK 17+ e Maven 3.8+.

```bash
mvn spring-boot:run
```

A aplicação inicia em `http://localhost:8080`.

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Console H2: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:file:./data/fintrack`
  - usuário: `sa`
  - senha: (vazia)

O banco H2 é persistente em arquivo para facilitar a avaliação local. Para PostgreSQL, configure o banco `fintrack` e execute:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

Variáveis opcionais: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET`. Em produção, defina `JWT_SECRET` com uma chave Base64 aleatória de pelo menos 256 bits; não use o segredo de desenvolvimento incluído no properties.

## Fluxo rápido de teste

### 1. Cadastro
`POST /api/auth/register`

```json
{
  "nome": "Ana",
  "email": "ana@email.com",
  "senha": "Senha123"
}
```

A resposta contém `token`. Copie-o para o botão **Authorize** do Swagger como `Bearer <token>` (ou informe apenas o token, conforme a interface).

### 2. Criar categoria de receita
`POST /api/v1/categorias`

```json
{"nome":"Salário","descricao":"Renda mensal","tipo":"RECEITA"}
```

### 3. Criar categoria de despesa
`POST /api/v1/categorias`

```json
{"nome":"Alimentação","descricao":"Mercado e refeições","tipo":"DESPESA"}
```

### 4. Registrar receita
`POST /api/v1/transacoes`

```json
{
  "descricao":"Salário de novembro",
  "valor":3500.00,
  "data":"2026-11-05",
  "tipo":"RECEITA",
  "categoriaId":1
}
```

### 5. Registrar despesa
`POST /api/v1/transacoes`

```json
{
  "descricao":"Compras do mês",
  "valor":450.00,
  "data":"2026-11-06",
  "tipo":"DESPESA",
  "categoriaId":2
}
```

### 6. Consultar saldo
`GET /api/v1/transacoes/relatorios/saldo`

Resposta esperada (valores ilustrativos):

```json
{"totalReceitas":3500.00,"totalDespesas":450.00,"saldoAtual":3050.00}
```

## Testes automatizados

O projeto inclui testes de integração para confirmar que endpoints financeiros exigem JWT, que um usuário não consegue excluir categorias de outro usuário e que e-mails duplicados retornam HTTP 409. Execute com `mvn clean verify`.

## Endpoints

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/auth/register` | Público | Cadastro |
| POST | `/api/auth/login` | Público | Login e emissão JWT |
| GET | `/api/v1/categorias` | JWT | Listar categorias |
| POST | `/api/v1/categorias` | JWT | Criar categoria |
| PUT | `/api/v1/categorias/{id}` | JWT | Atualizar categoria |
| DELETE | `/api/v1/categorias/{id}` | JWT | Excluir categoria |
| GET | `/api/v1/transacoes` | JWT | Listar transações |
| POST | `/api/v1/transacoes` | JWT | Criar transação |
| PUT | `/api/v1/transacoes/{id}` | JWT | Atualizar transação |
| DELETE | `/api/v1/transacoes/{id}` | JWT | Excluir transação |
| GET | `/api/v1/transacoes/relatorios/saldo` | JWT | Relatório de saldo |

Filtros da listagem: `inicio`, `fim` (formato `AAAA-MM-DD`) e `categoriaId`.

## Códigos HTTP

- `201 Created`: cadastro de usuário, categoria ou transação.
- `200 OK`: consultas, login e atualizações.
- `204 No Content`: exclusões.
- `400 Bad Request`: validação ou regra de entrada inválida.
- `401 Unauthorized`: token ausente, inválido ou expirado.
- `403 Forbidden`: usuário sem permissão.
- `404 Not Found`: recurso inexistente ou que não pertence ao usuário.
- `409 Conflict`: saldo insuficiente ou conflito de integridade.

## Estrutura

```text
src/main/java/br/com/fintrack/
├── config/        # Segurança, ModelMapper e OpenAPI
├── controller/    # Auth, Categoria e Transacao
├── dto/           # Contratos da API
├── exception/     # Exceções e handler global
├── model/         # Entidades e enum
├── repository/    # Spring Data JPA e JPQL
├── security/      # JWT e UserDetailsService
└── service/       # Regras de negócio
```

## Observações para entrega acadêmica

- O roteiro pede migração do JDBC/JavaFX para uma API; este pacote entrega o back-end RESTful. Um cliente JavaFX/web/mobile é consumidor opcional da API e não é necessário para executar o servidor.
- O roteiro cita rotas hierárquicas `/api/v1/usuarios/{id}/transacoes`; para evitar confiar em IDs enviados pelo cliente, esta implementação identifica o usuário pelo JWT e utiliza `/api/v1/transacoes`. O isolamento por proprietário é aplicado em todas as consultas e operações.
- O ModelMapper fica configurado como componente; o mapeamento de transação é explícito para manter clara a associação segura entre usuário e categoria.
- Antes da entrega final, rode os testes manuais descritos acima e, se sua instituição exigir, inclua capturas de tela do Swagger e dos resultados no banco.
