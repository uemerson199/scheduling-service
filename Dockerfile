FROM eclipse-temurin:21-jdk-jammy as build

WORKDIR /workspace/app

# 1. Copia os arquivos de configuração do Gradle
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 2. A CORREÇÃO: Remove quebras de linha do Windows (\r) e dá permissão de execução
# O 'sed' apaga o caractere invisível que causa o erro "not found"
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# 3. Copia o código fonte (só agora, para aproveitar o cache das camadas anteriores)
COPY src src

# 4. Executa o build
RUN ./gradlew build -x test --no-daemon

# --- Estágio Final (Runtime) ---
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copia o JAR gerado no estágio de build
# O *.jar garante que pegue o arquivo independente da versão no nome
COPY --from=build /workspace/app/build/libs/*.jar app.jar

# Expõe a porta (A porta real é definida pela variável de ambiente SERVER_PORT no docker-compose)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]