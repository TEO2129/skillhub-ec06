FROM openjdk:17-jdk-slim

# Installer Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copier le code source
COPY pom.xml .
COPY src ./src

# Compiler et empaqueter
RUN mvn clean package -DskipTests

# Copier le jar
FROM openjdk:17-jdk-slim
COPY --from=0 /app/target/auth-server-1.0.0.jar app.jar

# Port exposé
EXPOSE 8080

# Variables d'environnement requises
ENV APP_MASTER_KEY=""
ENV JWT_SECRET="SkillHubMasterKey2026!SecretJWTKey256BitsMinimum"

# Lancer l'application
ENTRYPOINT ["java", "-jar", "/app.jar"]