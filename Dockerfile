# =============================================
# Etapa 1: compilar el JAR con Maven
# =============================================
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Descarga dependencias primero (se cachean si el pom no cambia)
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# =============================================
# Etapa 2: imagen liviana solo con el JRE
# =============================================
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/users-management-*.jar app.jar

# Render asigna el puerto en la variable PORT; localmente se usa 8080
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
