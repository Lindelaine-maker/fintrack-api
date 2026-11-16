# FinTrack — API de Finanças Pessoais

Projeto acadêmico de uma API RESTful para gerenciamento de finanças pessoais, com Java 17 e Spring Boot.

## Tecnologias
- Spring Boot 3, Spring Web e Spring Data JPA/Hibernate
- Spring Security com autenticação JWT
- DTOs, validação e camada de serviços
- H2 para desenvolvimento e configuração alternativa para PostgreSQL
- Swagger/OpenAPI

## Funcionalidades
- Cadastro e login de usuários
- CRUD de categorias e transações
- Consultas por período, por categoria e resumo de saldo por tipo
- Proteção de recursos por usuário autenticado
- Tratamento global de erros e documentação dos endpoints

## Executar localmente
Requer Java 17 e Maven. Na raiz do projeto, execute:

```bash
mvn spring-boot:run
```

A documentação Swagger fica disponível em `http://localhost:8080/swagger-ui.html`.

## Código-fonte
O pacote completo `FinTrack-Projeto-Completo.zip` foi entregue nesta conversa. Ele contém o projeto Maven e os arquivos Java em `src/main/java`. O ZIP ainda não foi adicionado a este repositório; esta página documenta o projeto e orienta sua execução.

> Observação: a compilação e os testes automatizados ainda precisam ser executados em um ambiente com Maven instalado.