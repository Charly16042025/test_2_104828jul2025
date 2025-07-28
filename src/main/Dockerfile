# Usa una imagen con Java preinstalado
FROM eclipse-temurin:17-jdk

# Crea un directorio dentro del contenedor
WORKDIR /app

# Copia el JAR compilado desde tu máquina al contenedor
COPY target/*.jar app.jar

# Expone el puerto (Render usa el 10000 o detecta automáticamente)
EXPOSE 8080

# Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "app.jar"]
##fin del docker file
##Dockerfile v1 2772025 - 7:03