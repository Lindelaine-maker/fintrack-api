# FinTrack API — Projeto 2 | Módulo Avançado

**API RESTful para gerenciamento de finanças pessoais**, desenvolvida como entrega do Projeto 2 do módulo avançado.

## 1. Objetivo

Migrar a lógica de finanças pessoais para um back-end multiusuário, com persistência JPA/Hibernate, API REST com Spring Boot e proteção dos dados por Spring Security + JWT. Cada usuário acessa somente suas próprias categorias e transações.

## 2. Tecnologias

- Java 17 e Maven
- Spring Boot 3.3.5 / Spring Web
- Spring Data JPA e Hibernate
- H2 (desenvolvimento e testes) e PostgreSQL (perfil alternativo)
- Spring Security, JWT (JJWT) e BCrypt
- Bean Validation
- ModelMapper para conversão dos campos escalares do DTO de entrada para a entidade de transação; DTO de saída imutável construído explicitamente
- Swagger/OpenAPI (springdoc)
- JUnit 5, Spring Boot Test, MockMvc e Spring Security Test

## 3. Requisitos do enunciado

| Requisito | Implementação |
|---|---|
| Entidade Usuario | `Usuario` com persistência JPA |
| Entidade Categoria | `Categoria`, vinculada ao proprietário |
| Entidade Transacao | `Transacao`, vinculada ao usuário e à categoria |
| Relacionamentos | Usuario 1:N Categoria; Usuario 1:N Transacao; Categoria 1:N Transacao |
| Repositórios JPA | `UsuarioRepository`, `CategoriaRepository`, `TransacaoRepository` |
| Consultas JPQL | Filtros por datas/categoria e somatórios por tipo para relatório de saldo |
| API REST | `AuthController`, `CategoriaController`, `TransacaoController` |
| DTOs | `RegisterDTO`, `LoginDTO`, `TokenDTO`, `CategoriaDTO`, `TransacaoDTO`, `SaldoDTO` |
| Camada de serviço | Serviços de autenticação, usuário, categoria e transação |
| ModelMapper | Injeção no `TransacaoService` para request → entidade; resposta `record` construída explicitamente |
| Tratamento de erros | `GlobalExceptionHandler` com respostas 400, 404, 409 e demais erros |
| Segurança | JWT stateless, BCrypt, `SecurityFilterChain`, `@PreAuthorize` e verificação de propriedade |
| Swagger/OpenAPI | Tags, operações, DTO schemas, Bearer JWT e códigos de resposta |

## 4. Regras de negócio e segurança

1. O usuário é identificado pelo token JWT. O cliente não informa o proprietário da transação/categoria.
2. As consultas são filtradas pelo ID do usuário autenticado.
3. Categorias só podem ser consultadas, alteradas e excluídas pelo proprietário.
4. Transações só podem ser consultadas, alteradas e excluídas pelo proprietário.
5. A categoria usada em uma transação precisa pertencer ao usuário e ter o mesmo tipo (RECEITA/DESPESA).
6. O valor de uma transação deve ser positivo.
7. Uma despesa não pode ultrapassar o saldo disponível (HTTP 409).
8. Não se pode excluir categoria vinculada a transações nem alterar seu tipo enquanto houver lançamentos associados.
9. O cadastro não permite e-mail já utilizado.
10. Senhas são armazenadas com BCrypt; sessões são stateless.

**Semântica de autorização:** recurso inexistente retorna 404; recurso existente pertencente a outro usuário retorna 403.

## 5. Executar localmente

### Pré-requisitos

- JDK 17 ou superior
- Maven 3.8 ou superior

### Iniciar

Na pasta `FinTrack-Projeto-Completo/fintrack`:

```bash
mvn clean spring-boot:run
```

A API ficará disponível em `http://localhost:8080`.

### Banco H2 local

O perfil padrão utiliza H2 persistido em arquivo:

- Console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:file:./data/fintrack`
- Usuário: `sa`
- Senha: vazia

### PostgreSQL (opcional)

Configure as variáveis `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` e ative o perfil PostgreSQL conforme `application-postgres.properties`:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

### Segredo JWT

A aplicação aceita a variável de ambiente `JWT_SECRET` (chave Base64 de pelo menos 256 bits). O segredo padrão é somente para desenvolvimento e **não deve ser utilizado em produção**.

## 6. Swagger / OpenAPI

- Swagger UI: http://localhost:8080/swagger-ui.html
- Documento OpenAPI JSON: http://localhost:8080/v3/api-docs

Para testar endpoints protegidos:
1. Faça o cadastro ou login.
2. Copie o campo `token` da resposta.
3. Clique em **Authorize** no Swagger e informe o token Bearer.
4. Execute as operações de categorias e transações.

## 7. Fluxo de demonstração

### 7.1 Cadastrar usuário

`POST /api/auth/register`

```json
{
  "nome": "Ana",
  "email": "ana@email.com",
  "senha": "Senha123"
}
```

A resposta retorna o JWT e os dados básicos do usuário.

### 7.2 Login

`POST /api/auth/login`

```json
{
  "email": "ana@email.com",
  "senha": "Senha123"
}
```

### 7.3 Criar categoria de receita

`POST /api/v1/categorias`

```json
{"nome":"Salário","descricao":"Renda mensal","tipo":"RECEITA"}
```

### 7.4 Criar categoria de despesa

`POST /api/v1/categorias`

```json
{"nome":"Alimentação","descricao":"Mercado e refeições","tipo":"DESPESA"}
```

### 7.5 Criar receita

`POST /api/v1/transacoes`

```json
{
  "descricao": "Salário de novembro",
  "valor": 3500.00,
  "data": "2026-11-05",
  "tipo": "RECEITA",
  "categoriaId": 1
}
```

### 7.6 Criar despesa

`POST /api/v1/transacoes`

```json
{
  "descricao": "Compras do mês",
  "valor": 450.00,
  "data": "2026-11-06",
  "tipo": "DESPESA",
  "categoriaId": 2
}
```

### 7.7 Consultar saldo

`GET /api/v1/transacoes/relatorios/saldo`

Exemplo ilustrativo:

```json
{"totalReceitas":3500.00,"totalDespesas":450.00,"saldoAtual":3050.00}
```

## 8. Endpoints

| Método | Rota | Acesso | Resultado principal |
|---|---|---|---|
| POST | `/api/auth/register` | Público | Cadastro (201) |
| POST | `/api/auth/login` | Público | Login e JWT (200) |
| GET | `/api/v1/categorias` | JWT | Listar categorias do usuário (200) |
| POST | `/api/v1/categorias` | JWT | Criar categoria (201) |
| PUT | `/api/v1/categorias/{id}` | JWT/proprietário | Atualizar categoria (200) |
| DELETE | `/api/v1/categorias/{id}` | JWT/proprietário | Excluir categoria (204) |
| GET | `/api/v1/transacoes` | JWT | Listar transações (200) |
| POST | `/api/v1/transacoes` | JWT | Criar transação (201) |
| PUT | `/api/v1/transacoes/{id}` | JWT/proprietário | Atualizar transação (200) |
| DELETE | `/api/v1/transacoes/{id}` | JWT/proprietário | Excluir transação (204) |
| GET | `/api/v1/transacoes/relatorios/saldo` | JWT | Resumo financeiro (200) |

Filtros opcionais de transações: `inicio`, `fim` (formato ISO `AAAA-MM-DD`) e `categoriaId`.

## 9. Códigos HTTP

- **200** — consulta, login ou atualização concluída.
- **201** — usuário, categoria ou transação criada.
- **204** — exclusão concluída sem corpo de resposta.
- **400** — dados inválidos, validação ou parâmetros incorretos.
- **401** — token ausente, inválido/expirado ou credenciais incorretas.
- **403** — recurso pertence a outro usuário ou acesso não permitido.
- **404** — recurso não encontrado.
- **409** — e-mail/categoria duplicada, conflito de integridade ou saldo insuficiente.

## 10. Testes automatizados

Os testes de integração usam o perfil `test`, com banco H2 **em memória** e estratégia `create-drop`. Assim, não leem nem alteram o arquivo H2 usado na execução normal.

Cobertura de segurança existente:
- endpoints financeiros sem JWT retornam 401;
- usuário não lista nem exclui categoria de outro usuário;
- proprietário consegue excluir seu próprio recurso;
- ID inexistente retorna 404 e recurso de terceiro retorna 403;
- e-mail duplicado retorna 409.

Executar todos os testes e verificações Maven:

```bash
mvn clean verify
```

## 11. Organização do código

```text
src/
├── main/
│   ├── java/br/com/fintrack/
│   │   ├── config/       # Security, ModelMapper e OpenAPI
│   │   ├── controller/   # Controllers REST
│   │   ├── dto/          # Contratos e schemas da API
│   │   ├── exception/    # Exceções e handler global
│   │   ├── model/        # Entidades JPA e enumerações
│   │   ├── repository/   # Spring Data JPA e JPQL
│   │   ├── security/     # Filtro e serviço JWT
│   │   └── service/      # Regras de negócio
│   └── resources/        # Configuração da aplicação
└── test/
    ├── java/             # Testes de integração
    └── resources/        # Perfil isolado de testes
```

## 12. Observação sobre o escopo

O enunciado cita como exemplo rotas hierárquicas `/api/v1/usuarios/{id}/transacoes`. Esta API utiliza `/api/v1/transacoes` e identifica o usuário pelo JWT, sem confiar em um ID de proprietário enviado pelo cliente. A regra de isolamento por usuário permanece aplicada na camada de serviço e nas consultas.

O artefato desta entrega é o back-end RESTful. Um cliente JavaFX, web ou mobile pode consumir a API, mas não é necessário para iniciar e avaliar o servidor.
