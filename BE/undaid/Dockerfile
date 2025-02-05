# 빌드 스테이지
FROM gradle:8.12.1-jdk17 AS build

WORKDIR /home/gradle/src

COPY --chown=gradle:gradle . .

RUN gradle build -x test --no-daemon

# 실행 스테이지
FROM openjdk:17-jdk

WORKDIR /app

# 빌드 스테이지에서 생성된 JAR 파일을 복사
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
