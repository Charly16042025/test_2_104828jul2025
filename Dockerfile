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
#FROM eclipse-temurin:17-jdk AS builder

#WORKDIR /app
#COPY . .

# Da permisos de ejecución y construye el JAR
#RUN chmod +x mvnw && \
 #   ./mvnw clean package -DskipTests

# Fase de ejecución (solo el JRE + JAR)
#FROM eclipse-temurin:17-jre
#WORKDIR /app
#COPY --from=builder /app/target/*.jar app.jar
#EXPOSE 8080
#ENTRYPOINT ["java", "-jar", "app.jar"]
##v4-25072025 - 09:05 - fin del docker file

FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY . .

RUN ./mvnw package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/tucotizador-0.0.1-SNAPSHOT.jar"]
