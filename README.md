# QTMDashboard

Progetto completo con:

- Backend `Spring Boot` (Java 17)
- Autenticazione `Keycloak` via endpoint token
- Persistenza su `MySQL` locale
- Frontend `Angular` con pagina login e dashboard

## Configurazione richiesta

### Keycloak

- URL token: `http://localhost:8085/realms/<realmCode>/protocol/openid-connect/token` (il nome del realm è ora parametrico tramite la variabile d'ambiente APP_KEYCLOAK_REALM_CODE, default realmCode)
- Realm: `QTM`
- Client: `postman-client`
- Grant type: `password`

Per il provisioning utenti da backend serve anche una configurazione admin Keycloak. Sono supportate due modalita:

- `APP_KEYCLOAK_ADMIN_CLIENT_ID` + `APP_KEYCLOAK_ADMIN_CLIENT_SECRET` per `client_credentials`
- `APP_KEYCLOAK_ADMIN_USERNAME` + `APP_KEYCLOAK_ADMIN_PASSWORD` come fallback, opzionalmente con `APP_KEYCLOAK_ADMIN_USER_REALM` (default `master`)

Per l'avvio locale vengono accettate anche le variabili standard di Keycloak `KEYCLOAK_ADMIN` / `KEYCLOAK_ADMIN_PASSWORD` e `KC_BOOTSTRAP_ADMIN_USERNAME` / `KC_BOOTSTRAP_ADMIN_PASSWORD`, usate automaticamente come fallback per il provisioning admin su `admin-cli`.

Le proprietà sono già valorizzate in `src/main/resources/application.yml`.

### MySQL

- Host: `localhost`
- Porta sviluppo: `3306`
- Porta collaudo: `3307`
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

Per selezionare l'ambiente usare la variabile `QTMDB_ENV`:

- `QTMDB_ENV=sviluppo` → usa MySQL locale su `localhost:3306`
- `QTMDB_ENV=collaudo` → usa MySQL esposto da Docker su `localhost:3307`

Esempi:

```bash
# Sviluppo locale
mvn spring-boot:run -Dspring-boot.run.profiles=sviluppo

# Collaudo locale contro DB Docker su 3307
mvn spring-boot:run -Dspring-boot.run.profiles=collaudo
```

Oppure via variabile d'ambiente:

```bash
# PowerShell
$env:QTMDB_ENV = "sviluppo"
mvn spring-boot:run

$env:QTMDB_ENV = "collaudo"
mvn spring-boot:run
```

Backend disponibile su `http://localhost:8086`.

docker build -t qtm-dashboard .
docker run --rm -p 8086:8086 qtm-dashboard
docker run --rm -p 8086:8086 \
	-e SPRING_DATASOURCE_URL="jdbc:mysql://<db-host>:3306/QTMDashboard?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
	-e SPRING_DATASOURCE_USERNAME=root \
	-e SPRING_DATASOURCE_PASSWORD=dago \
	-e APP_KEYCLOAK_SERVER_URL="http://<keycloak-host>:8085" \
	-e APP_KEYCLOAK_REALM_CODE="realmCode" \
	-e SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI="http://<keycloak-host>:8085/realms/$APP_KEYCLOAK_REALM_CODE" \
	-e APP_KEYCLOAK_TOKEN_URL="http://<keycloak-host>:8085/realms/$APP_KEYCLOAK_REALM_CODE/protocol/openid-connect/token" \
	-e APP_CORS_ALLOWED_ORIGINS="http://localhost:4200,http://127.0.0.1:4200" \
	qtm-dashboard

## Avvio con Docker Compose (FE/BE separati)

Dal 2026-04-24 il progetto è suddiviso in compose separati per backend e frontend:

- `docker-compose.backend.yml` → backend Spring Boot + MySQL
- `docker-compose.frontend.yml` → solo frontend Angular

### Build e pulizia immagini

Per eliminare tutte le vecchie immagini Docker di QRMDashboard:

```bash
docker image rm qtmdashboard-backend qtmdashboard-frontend qtmdashboard-mysql || true
```

Per buildare e pubblicare le nuove immagini:

```bash
# Backend (da QRMDashboard)
docker compose -f docker-compose.backend.yml build
docker compose -f docker-compose.backend.yml up -d

# Frontend (da QRMDashboard)
docker compose -f docker-compose.frontend.yml build
docker compose -f docker-compose.frontend.yml up -d
```

### Avvio backend standalone

```bash
docker compose -f docker-compose.backend.yml up -d
```
Backend disponibile su `http://localhost:8086`

### Avvio frontend standalone

```bash
docker compose -f docker-compose.frontend.yml up -d
```
Frontend disponibile su `http://localhost:4200`

### Note reti

Entrambi i compose usano la rete `qtm-network` e, per il backend, anche la rete esterna `keycloak-net`.
Se usi Keycloak in Docker, assicurati che la rete `keycloak-docker_default` sia presente e collegata.

### Esempio di flusso

1. Avvia backend: `docker compose -f docker-compose.backend.yml up -d`
2. Avvia frontend: `docker compose -f docker-compose.frontend.yml up -d`
3. Accedi a `http://localhost:4200` per la dashboard

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
