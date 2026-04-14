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

## Avvio con Docker

Il `docker-compose.yml` imposta gia `QTMDB_ENV=collaudo` per il backend, mentre all'interno della rete Docker il datasource viene forzato correttamente su `mysql:3306`.

Build immagine:

```bash
mvn clean package -DskipTests
docker build -t qtm-dashboard .
```

Run container usando i servizi esposti sulla macchina host:

```bash
docker run --rm -p 8086:8086 qtm-dashboard
```

Se MySQL o Keycloak non sono raggiungibili tramite `host.docker.internal`, sovrascrivere le variabili d'ambiente al run:

```bash
docker run --rm -p 8086:8086 \
	-e SPRING_DATASOURCE_URL="jdbc:mysql://<db-host>:3306/QTMDashboard?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
	-e SPRING_DATASOURCE_USERNAME=root \
	-e SPRING_DATASOURCE_PASSWORD=dago \
	-e APP_KEYCLOAK_SERVER_URL="http://<keycloak-host>:8085" \
	-e SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI="http://<keycloak-host>:8085/realms/QTM" \
	-e APP_KEYCLOAK_TOKEN_URL="http://<keycloak-host>:8085/realms/QTM/protocol/openid-connect/token" \
	-e APP_CORS_ALLOWED_ORIGINS="http://localhost:4200,http://127.0.0.1:4200" \
	qtm-dashboard
```

Nota: su Linux `host.docker.internal` potrebbe non essere risolto automaticamente. Se Keycloak gira in un altro container Docker, collega QTMDB alla stessa rete e usa come host il nome del container Keycloak nelle variabili `APP_KEYCLOAK_SERVER_URL`, `APP_KEYCLOAK_TOKEN_URL` e `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`.

Nota: se MySQL gira sulla macchina host ma accetta connessioni solo da `localhost`, il container non potra collegarsi finche il server MySQL non viene configurato per ascoltare anche su un indirizzo raggiungibile dal container.

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
