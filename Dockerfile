# Usa una imagen con Java preinstalado
#FROM eclipse-temurin:17-jdk

# Crea un directorio dentro del contenedor
#WORKDIR /app

# Copia el JAR compilado desde tu máquina al contenedor
#COPY target/*.jar app.jar

# Expone el puerto (Render usa el 10000 o detecta automáticamente)
#EXPOSE 8080

# Comando para ejecutar la app
#ENTRYPOINT ["java", "-jar", "app.jar"]

# Fase de construcción (genera el JAR)
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app
COPY . .

# Da permisos de ejecución y construye el JAR
RUN chmod +x mvnw && \
    ./mvnw clean package -DskipTests

# Fase de ejecución (solo el JRE + JAR)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]