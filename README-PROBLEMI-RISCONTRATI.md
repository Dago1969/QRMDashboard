# Problemi riscontrati

## Interventi fatti

- Disabilitata l'esecuzione automatica di `data.sql` nel profilo DEV con `spring.sql.init.mode: never`, lasciando l'inizializzazione a Liquibase.
- Resa componibile la URL del database tramite `DB_HOST`, `DB_PORT`, `DB_NAME_PREFIX` e `DB_NAME`, con fallback di `DB_NAME` su `APP_KEYCLOAK_REALM_CODE`.
- Centralizzata la configurazione Keycloak partendo da `server-url` e `realm-code`; da questi vengono composti `realm-url`, token endpoint, issuer e JWK endpoint.
- Rimosso il valore di default cablato del secret del client amministrativo Keycloak.
- Centralizzata la URL della dashboard tenant tramite `APP_TENANTS_BASE_URL` e riutilizzata nei mapping dei client.
- Aggiunti nel solo profilo DEV i logger di autenticazione, Spring Security, client HTTP e Keycloak.
- Separati i seed Liquibase MySQL e MariaDB per ruoli, template email e ruolo infermiere, evitando la sintassi di upsert MySQL non supportata da MariaDB.
- Resi relativi gli endpoint API del frontend tramite `environment.apiBaseUrl` per supportare sia localhost sia deploy sotto path prefix.
- Corrette le chiamate ASL che usavano direttamente il path assoluto `/api`.
- Reso relativo il caricamento delle risorse i18n per rispettare il base path del frontend.
- Gestito il caso in cui il tenant punta alla dashboard stessa, evitando redirect e loop tra login e dashboard.
- Disabilitato il riferimento a `texture.png`, assente dal repository e richiamato con un path assoluto.
- Configurato `proxy.conf.json` per eseguire il frontend in locale contro il backend DEV; lasciata nello stesso file anche la configurazione localhost commentata.
- Impostato `secure: false` nel proxy DEV locale per il certificato non riconosciuto dalla CA usata da Node.

## Nota redirect tenant

- La gestione del redirect e sparsa tra migration, tabella `tenant_app_pointer`, `TenantAppPointerDataInitializer` e frontend.
- Liquibase inserisce `tenant_app_url` con `http://localhost:4201/dashboard`.
- `APP_TENANTS_BASE_URL` non aggiorna quel record: l'initializer modifica soltanto il vecchio valore `http://localhost:8087`.
- Dopo il login il frontend chiede al backend la `tenant_app_url` associata al `client_code` selezionato.
- Se la URL corrisponde alla dashboard corrente, ora naviga internamente senza redirect e senza aggiungere parametri alla URL.
- Se invece punta a un'applicazione tenant esterna, il frontend esegue un redirect verso quella URL aggiungendo in query string il JWT (`token`) e il contesto selezionato (`client`, `role`, `project`).
- La login doveva essere implementata con il flusso OIDC nativo di Keycloak (`Authorization Code` con `PKCE` e redirect), non con una login custom basata sul password grant. In questo modo frontend locale e DEV potrebbero usare lo stesso realm, backend e database configurando soltanto le redirect URI autorizzate, senza far transitare le credenziali dal backend applicativo e senza passare il token nella query string.

## Note

Le scelte temporanee, i workaround e i punti da rivedere sono indicati direttamente nei file interessati con commenti `FIXME Francesco`.