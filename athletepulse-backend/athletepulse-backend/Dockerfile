# Estágio 1: Build do projeto Java com Maven e JDK 17
FROM maven:3.8.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia o código do projeto para dentro do container
COPY . .

# Compila o projeto e gera o arquivo .jar (pula os testes para o build ser mais rápido)
RUN mvn clean package -DskipTests

# Estágio 2: Execução com uma imagem leve do Java JRE 17
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copia o .jar gerado no primeiro estágio para a imagem final
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta padrão do Spring Boot
EXPOSE 8080

# Comando para iniciar a aplicação no Render
ENTRYPOINT ["java", "-jar", "app.jar"]