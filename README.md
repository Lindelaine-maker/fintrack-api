# FinTrack API

Projeto acadêmico de API RESTful para gerenciamento de finanças pessoais, desenvolvido em Java 17 e Spring Boot 3.

## Código-fonte

O projeto Maven completo está em [FinTrack-Projeto-Completo/fintrack](FinTrack-Projeto-Completo/fintrack/). Consulte o [README detalhado](FinTrack-Projeto-Completo/fintrack/README.md) para configuração, exemplos de requisições e execução.

## Tecnologias

- Spring Boot, Spring Web e Spring Data JPA/Hibernate
- Spring Security stateless com JWT e BCrypt
- DTOs, Bean Validation e camada de serviços
- H2 para desenvolvimento e PostgreSQL como perfil alternativo
- Swagger/OpenAPI

## Executar

Requer JDK 17+ e Maven 3.8+.

No terminal, entre na pasta do projeto:

```bash
cd FinTrack-Projeto-Completo/fintrack
mvn spring-boot:run
```

Swagger: http://localhost:8080/swagger-ui.html

## Requisitos do módulo avançado

A API contém autenticação/registro, CRUD de categorias e transações, relatório de saldo, isolamento dos dados por usuário autenticado, consultas JPQL, tratamento global de erros e documentação OpenAPI.

As correções de implementação foram integradas na [pull request #6](https://github.com/Lindelaine-maker/fintrack-api/pull/6). A validação automatizada de compilação e testes é executada pelo workflow **FinTrack CI** com JDK 17.
