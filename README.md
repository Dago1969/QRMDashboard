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

Backend disponibile su `http://localhost:8086`.

## Avvio con Docker

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
	-e SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI="http://<keycloak-host>:8085/realms/QTM" \
	-e APP_KEYCLOAK_TOKEN_URL="http://<keycloak-host>:8085/realms/QTM/protocol/openid-connect/token" \
	qtm-dashboard
```

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
