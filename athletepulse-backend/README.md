# AthletePulse - Backend

API em Spring Boot para o app AthletePulse (controle de performance e saúde de atletas).

## Pré-requisitos
- JDK 17+
- MySQL rodando localmente (ou ajustar `application.properties` pra apontar pro seu banco)

## Como rodar

1. Crie o banco (opcional - a aplicação cria sozinha graças a `createDatabaseIfNotExist=true`,
   mas se preferir criar manualmente):
   ```sql
   CREATE DATABASE athletepulse;
   ```

2. Ajuste `src/main/resources/application.properties`:
   - `spring.datasource.username` / `spring.datasource.password` com suas credenciais do MySQL.

3. Abra o projeto no IntelliJ (`File > Open` na pasta `athletepulse-backend`) e rode a classe
   `AthletePulseApplication`. A API sobe em `http://localhost:8080`.

## Endpoints disponíveis

### `POST /api/auth/registrar`
```json
{
  "nome": "João Costa",
  "email": "joao@email.com",
  "tipo": "jogador",
  "senha": "123456"
}
```
Retorna `201 Created` sem corpo, ou `409` se o email já existir.

### `POST /api/auth/login`
```json
{
  "email": "joao@email.com",
  "senha": "123456",
  "perfil": "jogador"
}
```
Retorna:
```json
{ "token": "...", "nome": "João Costa", "tipo": "jogador" }
```

## Conectando com o front-end

O front (pasta `ProjetoFinal-developer`) já está configurado pra chamar
`http://localhost:8080/api` (ver `js/config.js`). Se você abrir o HTML direto pelo
navegador (`file://`), o CORS pode bloquear — rode o front por um servidor local
(ex: extensão "Live Server" do VSCode) na porta 5500, que já está liberada em
`app.cors.origens-permitidas` no `application.properties`.

## Segurança - pontos de atenção antes de produção
- Troque `app.jwt.secret` por um valor aleatório forte via variável de ambiente `JWT_SECRET`.
- `spring.jpa.hibernate.ddl-auto=update` é conveniente em desenvolvimento, mas em produção
  o ideal é migrations versionadas (Flyway/Liquibase).
- Senhas são armazenadas com hash BCrypt (nunca em texto puro) — não alterar isso.
