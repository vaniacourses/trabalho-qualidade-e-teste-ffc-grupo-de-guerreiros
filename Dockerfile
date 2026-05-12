FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml ./
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:17-jre
RUN groupadd -r app && useradd -r -g app app \
 && apt-get update && apt-get install -y --no-install-recommends curl locales \
 && sed -i '/pt_BR.UTF-8/s/^# //' /etc/locale.gen \
 && locale-gen pt_BR.UTF-8 \
 && rm -rf /var/lib/apt/lists/*
ENV LANG=pt_BR.UTF-8 \
    LANGUAGE=pt_BR:pt \
    LC_ALL=pt_BR.UTF-8 \
    TZ=America/Sao_Paulo
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
RUN chown -R app:app /app
USER app
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["java", "-Duser.language=pt", "-Duser.country=BR", "-Duser.timezone=America/Sao_Paulo", "-jar", "/app/app.jar"]
