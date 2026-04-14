# Fase 1: Immagine base con Java (JDK o JRE)
FROM eclipse-temurin:17-jre-alpine

# Imposta la directory di lavoro nel container
WORKDIR /app

# Valori di default per l'esecuzione in container; possono essere sovrascritti con -e.
ENV SERVER_PORT=8086 \
	SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/QTMDashboard?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC \
	SPRING_DATASOURCE_USERNAME=root \
	SPRING_DATASOURCE_PASSWORD=dago \
	APP_KEYCLOAK_SERVER_URL=http://host.docker.internal:8085 \
	SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://host.docker.internal:8085/realms/QTM \
	APP_KEYCLOAK_TOKEN_URL=http://host.docker.internal:8085/realms/QTM/protocol/openid-connect/token \
	APP_CORS_ALLOWED_ORIGINS=http://localhost:4200,http://127.0.0.1:4200


# Copia il file JAR dal tuo computer al container
# Sostituisci 'nome-app.jar' con il nome reale del tuo file
COPY target/qtm-dashboard-0.0.1-SNAPSHOT.jar app.jar

# Esponi la porta utilizzata da Spring Boot (solitamente 8080)
EXPOSE 8086

# Comando per avviare l'applicazione
ENTRYPOINT ["java", "-jar", "app.jar"]