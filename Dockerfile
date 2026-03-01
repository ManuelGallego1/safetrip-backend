### ---------- Build Stage ----------
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copiamos primero los archivos del wrapper y pom.xml para aprovechar cache
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Descargamos dependencias en cache
RUN ./mvnw dependency:go-offline -B || true

# Copiamos el código fuente después (para no invalidar cache en cada cambio)
COPY src ./src

# Compilamos la aplicación sin tests
RUN ./mvnw clean package -DskipTests -B

### ---------- Runtime Stage ----------
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Instalar tzdata para zona horaria
RUN apk add --no-cache tzdata

# Configuramos zona horaria3
ENV TZ=America/Bogota

# Copiar el JAR compilado desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Crear usuario sin privilegios
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Exponer el puerto del backend
EXPOSE 8080

# Flags JVM recomendados para entornos en contenedor
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Xms256m -Xmx512m"

# Usamos ENTRYPOINT directo (más eficiente que CMD)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]