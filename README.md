# QTMDashboard

Progetto completo con:

- Backend `Spring Boot` (Java 17)
- Autenticazione `Keycloak` via endpoint token
- Persistenza su `MySQL` locale
- Frontend `Angular` con pagina login e dashboard

## Configurazione richiesta

### Keycloak

- URL token: `http://localhost:8085/realms/QTM/protocol/openid-connect/token`
- Realm: `QTM`
- Client: `postman-client`
- Grant type: `password`

Le proprietà sono già valorizzate in `src/main/resources/application.yml`.

### MySQL

- Host: `localhost`
- Porta: `3306`
- Database: `QTMDashboard`
- Username: `root`
- Password: `dago`

Il backend usa `ddl-auto: update`, quindi le tabelle vengono create/aggiornate automaticamente.

## Struttura principale

- `src/main/java/.../auth` → login Keycloak e controller accesso
- `src/main/java/.../user` → entity `User/Role`, repository, service, DTO, mapper, controller
- `frontend` → app Angular con login e dashboard

## Avvio backend

Da root progetto:

```bash
mvn spring-boot:run
```

Backend disponibile su `http://localhost:8080`.

Endpoint principali:

- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/users` (protetto)
- `GET /api/users/dashboard` (protetto)

## Avvio frontend

In cartella `frontend`:

```bash
npm install
npm start
```

Frontend disponibile su `http://localhost:4200`.

## Flusso login

1. Apri `http://localhost:4200/login`
2. Inserisci credenziali utente Keycloak
3. Angular invia richiesta a `POST /api/auth/login`
4. Il backend chiama Keycloak e restituisce `accessToken`
5. Angular salva il token e apre la dashboard (`/dashboard`)
