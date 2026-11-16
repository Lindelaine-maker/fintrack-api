# Relatório técnico de revisão e adequação — FinTrack API

**Entrega:** Projeto 2 — Finanças Pessoais (FinTrack), Módulo Avançado de Java  
**Data da revisão:** 16/11/2026  
**Repositório:** https://github.com/Lindelaine-maker/fintrack-api  
**Branch de trabalho:** `review/fintrack-entrega-professor`  
**Pull request:** https://github.com/Lindelaine-maker/fintrack-api/pull/5

---

## 1. Objetivo e escopo

Este relatório registra a análise do projeto FinTrack em relação às instruções do arquivo **Java - módulo avançado.pdf**, especialmente o Projeto 2 (páginas 5 e 6), e as correções realizadas na branch de revisão.

O enunciado solicita a migração da aplicação de finanças pessoais para uma API RESTful multiusuário com Spring Boot, Spring Data JPA/Hibernate, entidades e relacionamentos, consultas JPQL, DTOs, camada de serviço, regras de negócio, Spring Security com JWT, autorização por proprietário e documentação Swagger/OpenAPI.

Este documento diferencia:
- **Implementado/ajustado:** alteração feita no código da branch indicada.
- **Existente:** funcionalidade identificada na revisão do projeto.
- **Pendente de validação:** depende de execução automatizada ou teste manual.
- **Limitação conhecida:** melhoria que não deve ser considerada concluída.

## 2. Requisitos do enunciado e situação

| Requisito do professor | Evidência/implementação | Situação |
|---|---|---|
| Entidade `Transacao` com JPA | `model/Transacao.java`, `@Entity` | Implementado |
| Entidade `Usuario` | `model/Usuario.java`, persistência JPA | Implementado |
| Entidade `Categoria` | `model/Categoria.java`, persistência JPA | Implementado |
| Relacionamento Usuario 1:N Transacao | `Usuario.transacoes` e `Transacao.usuario` | Implementado |
| Relacionamento Usuario 1:N Categoria | `Usuario.categorias` e `Categoria.usuario` | Implementado |
| Relacionamento Categoria 1:N Transacao | `Categoria.transacoes` e `Transacao.categoria` | Implementado |
| Substituição do DAO JDBC por Spring Data JPA | Repositórios `UsuarioRepository`, `CategoriaRepository`, `TransacaoRepository` | Implementado |
| Consultas JPQL para filtros e relatórios | `TransacaoRepository`: filtro por período/categoria, somatórios e totais consolidados | Implementado |
| Controllers REST | `AuthController`, `CategoriaController`, `TransacaoController` | Implementado |
| Versionamento da API | Prefixo `/api/v1` nos recursos financeiros | Implementado |
| DTOs | `TransacaoDTO`, `CategoriaDTO`, `SaldoDTO`, DTOs de autenticação | Implementado |
| Camada de serviço | Serviços de autenticação, usuário, categoria e transação | Implementado |
| ModelMapper DTO de entrada → entidade | Configuração e injeção no `TransacaoService`; campos de controle são ignorados e preenchidos pelo serviço | Implementado; aguarda CI |
| Cálculo de saldo e validação de despesa | Regra em `TransacaoService`; despesa acima do saldo gera conflito | Implementado; precisa de execução de testes |
| Spring Security e JWT | `SecurityFilterChain`, filtro JWT, autenticação stateless | Implementado |
| Endpoints públicos de cadastro/login | `/api/auth/register` e `/api/auth/login` | Implementado |
| Isolamento dos dados por usuário | Consultas filtradas pelo usuário autenticado e verificação de proprietário | Implementado |
| `@PreAuthorize` | Proteção por papel e proprietário nos endpoints de alteração/exclusão | Implementado |
| Swagger/OpenAPI | springdoc, tags, operações, schemas e Bearer JWT | Implementado; revisão de cobertura em andamento |
| Respostas HTTP 400, 401, 403, 404 e 409 | Handler global e documentação dos endpoints | Implementado |
| Testes automatizados isolados | Testes de integração com perfil `test` e H2 em memória | Ajustado; aguarda CI |

## 3. Correções realizadas nesta revisão

### 3.1 ModelMapper e DTO imutável

O pipeline anterior falhava ao tentar criar um mapeamento convencional para `TransacaoDTO`, que é um Java record (tipo final e imutável).

Correção:
- Mantida a conversão request DTO → entidade `Transacao`.
- O ModelMapper é usado no mapeamento de entrada (`TransacaoDTO` → `Transacao`).
- Campos de controle (ID, usuário e categoria) continuam sob responsabilidade do servidor.
- A resposta `TransacaoDTO` é construída explicitamente pelo construtor canônico do record, evitando a tentativa de instanciação reflexiva que falha para tipos finais.

### 3.2 Concorrência no saldo

A validação anterior podia permitir que duas despesas simultâneas fossem aprovadas com base no mesmo saldo ainda disponível.

Correção:
- Criada consulta de usuário com bloqueio pessimista (`PESSIMISTIC_WRITE`).
- As operações de criação/alteração/exclusão de transação e de manutenção de categoria passam a adquirir o bloqueio do usuário dentro da transação.
- Isso serializa operações concorrentes do mesmo usuário nas rotinas que alteram saldo e categorias.

### 3.3 Consistência entre categoria e transação

A criação de transação e a alteração de tipo da categoria podiam competir entre si.

Correção:
- As operações de categoria e transação compartilham o bloqueio do registro do usuário.
- A verificação do tipo da categoria e a persistência passam a ocorrer dentro da transação protegida.

### 3.4 Nomes de categorias

Foi adicionado o campo `nome_normalizado` e uma restrição única composta por usuário e nome normalizado, além da verificação de duplicidade sem distinção de maiúsculas/minúsculas e do bloqueio por usuário. Isso reduz o risco de duplicatas como “Casa” e “casa” em criações concorrentes.

**Nota de migração:** o novo campo foi deixado nullable para não impedir a atualização de bancos já existentes que contenham categorias antigas. Em bancos legados, os valores antigos podem permanecer nulos até serem normalizados por migração de dados; novos cadastros e alterações passam a preenchê-lo.

### 3.5 Relatório de saldo

O relatório passa a obter receitas e despesas em uma única consulta agregada, evitando combinar resultados de duas consultas executadas em momentos diferentes.

### 3.6 Atualização e descrição de transação

As descrições são removidas de espaços excedentes nas operações de criação e atualização. A entidade recebeu `@Version` para habilitar controle otimista no nível JPA e o handler global traduz conflitos de concorrência de persistência para HTTP 409.

### 3.7 Isolamento dos testes

A classe `FinTrackEndpointsIntegrationTest` foi configurada com `@ActiveProfiles("test")`, assim como a suíte de segurança, para utilizar H2 em memória e não gravar dados no banco H2 local da aplicação.

### 3.8 Autorização e semântica 403/404

As verificações de propriedade usam consultas únicas que retornam o vínculo do recurso com o usuário. IDs inexistentes são deixados chegar à camada de serviço para resultar em 404; recursos existentes de outro usuário resultam em 403.

## 4. Segurança

Pontos observados na implementação:
- Cadastro e login públicos; demais endpoints financeiros protegidos.
- JWT enviado no cabeçalho `Authorization: Bearer`.
- Senhas armazenadas com BCrypt.
- Aplicação stateless.
- O ID do proprietário é obtido a partir do usuário autenticado, não de um campo enviado pelo cliente.
- Consultas e operações de categoria/transação consideram o usuário autenticado.
- A categoria escolhida para uma transação precisa pertencer ao usuário.
- A alteração/exclusão de recursos verifica a propriedade.
- O segredo JWT deve ser fornecido por variável de ambiente em produção. O valor padrão existente é exclusivamente para desenvolvimento e não deve ser usado em produção.

## 5. Testes e validação

A suíte existente contempla:
- endpoints protegidos sem token;
- token inválido;
- isolamento entre dois usuários;
- proibição de exclusão de categoria de outro usuário;
- distinção entre recurso inexistente (404) e recurso de terceiro (403);
- cadastro duplicado (409);
- fluxo de cadastro/login, categorias, receitas, despesas, filtros, atualização, saldo e exclusão;
- validação de dados inválidos (400);
- verificação de resposta DTO e cálculo do saldo.

Comando recomendado na pasta `FinTrack-Projeto-Completo/fintrack`:

```bash
mvn clean verify
```

**Status de execução:** a execução de GitHub Actions da branch falhou na inicialização do contexto Spring. A causa foi a tentativa de registrar `Converter<Transacao, TransacaoDTO>` no ModelMapper, que ainda dispara erro para o destino record final. A implementação foi simplificada: ModelMapper permanece no mapeamento de entrada DTO → entidade e a resposta record é criada pelo construtor. A correção precisa ser confirmada por uma nova execução de CI. Não há neste relatório uma alegação de testes verdes sem essa confirmação.

## 6. Documentação e demonstração

O README do projeto descreve:
- objetivo e tecnologias;
- matriz de requisitos;
- regras de negócio e segurança;
- execução local e configuração de banco;
- segredo JWT;
- Swagger UI e OpenAPI JSON;
- fluxo de demonstração (cadastro, login, categorias, receita, despesa e saldo);
- endpoints e códigos HTTP;
- execução dos testes;
- organização de pacotes.

URLs locais quando a aplicação estiver em execução:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Console H2: `http://localhost:8080/h2-console`

## 7. Limitações conhecidas e itens que não devem ser declarados como concluídos

1. **Idempotência de POST:** não foi implementado armazenamento de chave de idempotência para que uma repetição de POST após timeout devolva a transação original. Clientes podem duplicar lançamentos se repetirem manualmente a mesma solicitação.
2. **Controle otimista ponta a ponta:** a entidade tem `@Version`, mas o contrato atual de `TransacaoDTO` ainda não exige que o cliente envie a versão lida anteriormente. Portanto, o controle de versão JPA não substitui uma estratégia completa de detecção de atualização obsoleta na API.
3. **Migração de dados existentes:** a coluna `nome_normalizado` é compatível com a atualização de schema, mas registros legados precisam ser preenchidos para que a restrição de unicidade normalizada os cubra integralmente.
4. **CI após a última alteração:** precisa ser confirmado no GitHub Actions. Caso falhe, os logs devem ser tratados antes da entrega final.
5. **Produção:** antes de publicar, configurar segredo JWT forte via ambiente, credenciais reais do PostgreSQL e desabilitar configurações de desenvolvimento, como console H2.

## 8. Como apresentar ao professor

Sugestão de sequência objetiva:
1. Mostrar as entidades e seus relacionamentos JPA.
2. Mostrar os repositórios e as consultas JPQL.
3. Abrir os controllers e DTOs para demonstrar o contrato REST.
4. Cadastrar um usuário e autenticar para obter o JWT.
5. Criar uma categoria de receita e outra de despesa.
6. Registrar uma receita e uma despesa; consultar o saldo.
7. Tentar registrar despesa superior ao saldo e demonstrar o conflito.
8. Criar um segundo usuário e demonstrar que ele não acessa recursos do primeiro.
9. Mostrar o Swagger e executar `mvn clean verify`.

## 9. Conclusão

A branch de revisão contém ajustes importantes para alinhar o FinTrack aos requisitos centrais do Projeto 2 do módulo avançado: JPA e relacionamentos, API REST versionada, DTOs e serviços, ModelMapper efetivo, validação de saldo, segurança JWT multiusuário, documentação OpenAPI e testes isolados.

**A entrega permanece condicionada à aprovação do CI após as últimas alterações e à decisão consciente sobre as limitações registradas na seção 7.** Não se deve apresentar este relatório como certificação externa ou como prova de que todos os cenários de concorrência e repetição de requisições foram cobertos.
