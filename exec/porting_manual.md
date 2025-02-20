# 프로젝트 포팅 매뉴얼

## 목차
1. [개요](#1-개요)
2. [시스템 환경](#2-시스템-환경)
3. [빌드 및 배포 가이드](#3-빌드-및-배포-가이드)
4. [리버스 프록시 설정 가이드](#4-리버스-프록시-설정-가이드)
5. [데이터베이스 설정 가이드](#5-데이터베이스-설정-가이드)
6. [서버 가동 및 종료](#6-서버-가동-및-종료)


## 1. 개요
### 1.1 문서 개요
- 작성일: 2025-02-19
- 작성자: [권동환]

### 1.2 프로젝트 개요
- 프로젝트명: [undaied]
- GitLab 저장소 URL: [[GitLab URL](https://lab.ssafy.com/s12-webmobile2-sub1/S12P11B212)]

## 2. 시스템 환경
### 2.1 개발 환경
#### 2.1.1 IDE
- IntelliJ IDEA 2024.3.1.1
- Visual Studio Code 1.97.2

#### 2.1.2 런타임 환경
- JDK 17
- Python 3.12.8
- Node.js 22.12
  - npm 11

#### 2.1.3 빌드 도구
- Gradle 8.12.1
- Vite 6.0.5

### 2.2 서버 환경
#### 2.2.1 인스턴스 및 운영체제 
- AWS EC2
- Ubuntu 20.04 LTS

#### 2.2.2 서버 기술 스택
- 프론트엔드 서버 : Vite
- 백엔드 서버 : SpringBoot
- AI 서버: FastAPI
- 프록시 서버: Nginx
- 컨테이너: Docker
- 데이터베이스: MySQL
- 캐시 서버: Redis

#### 2.2.3 서비스 포트 구성
| 서비스     | 포트 | 기술 스택   |
| ---------- | ---- | ----------- |
| 프론트엔드 | 5173 | Vite        |
| 백엔드     | 8080 | Spring Boot |
| 소켓       | 9090 | Netty       |
| DB(SQL)    | 3306 | MySQL       |
| DB(NOSQL)  | 6379 | Redis       |
| AI         | 8000 | FastAPI     |
| Proxy      | 443  | Nginx       |

#### 2.2.4 MySQL 데이터베이스 접속 정보
> 주의사항 : DB접속 ID, PW는 유추하기 어려운 것으로 설정할 것
- 데이터베이스명: undaied
- 접속 정보
  ```properties
  spring.datasource.url=jdbc:mysql://mysql:3306/${MYSQL_DATABASE}?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
  spring.datasource.username=YOUR_DB_ID
  spring.datasource.password=YOUR_DB_PW
  ```

#### 2.2.5 Redis 데이터베이스 접속 정보
- 데이터베이스명: 0
- 접속 정보
  ```properties
  spring.data.redis.host=redis
  spring.data.redis.port=6379
  spring.data.redis.password=YOUR_DB_ROOT_PW
  ```

#### 2.2.6 환경변수 정보
> 주의사항 : 지정된 디렉토리에 환경변수가 올바르게 존재하지 않을 경우 실행되지 않으며, URL을 호스팅할 서버의 도메인으로 대체할 것
- 프로젝트 루트 디렉토리 : `S12P11B212` 
- 환경변수 명세
  - 프론트엔드 서버 환경변수 : `S12P11B212/FE/.env`
     ```env
    VITE_API_URL=https://i12b212.p.ssafy.io
    VITE_SOCKET_URL=https://i12b212.p.ssafy.io
    VITE_HMR_HOST=i12b212.p.ssafy.io
    VITE_HMR_ENABLED=false  
     ```
  - 백엔드 서버 환경변수 : `S12P11B212/BE/undaid/src/main/resources/application.properties`
     ```properties
    # Application
    spring.application.name=undaid
    spring.config.import=optional:file:.env[.properties],application-secrets.properties

    # Database Configuration
    spring.datasource.url=jdbc:mysql://mysql:3306/${MYSQL_DATABASE}?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
    spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
    spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
    spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

    # JPA Configuration
    spring.jpa.show-sql=true
    spring.jpa.hibernate.ddl-auto=none
    spring.jpa.properties.hibernate.format_sql=true
    spring.jpa.properties.hibernate.highlight_sql=true
    spring.jpa.open-in-view=false

    # Redis Configuration
    spring.data.redis.host=redis
    spring.data.redis.port=6379
    spring.data.redis.password=${SPRING_REDIS_PASSWORD}
    spring.data.redis.database=0
    spring.data.redis.timeout=5000
    spring.data.redis.lettuce.pool.max-active=8
    spring.data.redis.lettuce.pool.max-idle=8
    spring.data.redis.lettuce.pool.max-wait=-1ms

    # Server Configuration
    server.servlet.encoding.charset=UTF-8
    server.servlet.encoding.force=true

    # Logging Configuration
    logging.level.root=INFO
    logging.level.org.springframework.security=DEBUG

    # Socket.IO Debug Logging
    logging.level.com.ssafy.undaied.socket=TRACE
    logging.level.com.corundumstudio.socketio=TRACE
    logging.level.com.ssafy.undaied.socket.config=TRACE
    logging.level.com.ssafy.undaied.socket.handler=TRACE

    # Redis & WebSocket Debug Logging
    logging.level.org.springframework.data.redis=TRACE
    logging.level.org.springframework.web.socket=TRACE
    logging.level.io.netty=TRACE

    # hikari Setting & Logging
    spring.datasource.hikari.maximum-pool-size=10
    spring.datasource.hikari.minimum-idle=5
    spring.datasource.hikari.idle-timeout=300000
    spring.datasource.hikari.connection-timeout=20000
    spring.datasource.hikari.max-lifetime=1200000
    logging.level.com.zaxxer.hikari=WARN

    # 소켓 관련 추가 설정
    spring.main.allow-bean-definition-overriding=true
    netty.leak-detection.level=disabled
    spring.netty.connection-timeout=30000

    # Netty 관련 설정 추가
    spring.netty.worker-count=8
    spring.netty.boss-count=1

    # Socket.IO 최적화
    socketio.server.ping-interval=25000
    socketio.server.ping-timeout=60000
    socketio.server.upgrade-timeout=10000

    # Socket.IO 클라이언트 설정
    socketio.client.max-http-content-length=1048576
    socketio.client.max-frame-payload-length=1048576
    socketio.client.compression-enabled=true

    # Redis connection pool 최적화
    spring.data.redis.lettuce.pool.min-idle=5
    spring.data.redis.lettuce.pool.time-between-eviction-runs=60000
    spring.data.redis.lettuce.shutdown-timeout=1000ms

    # Spring에서 AI서버 연동용 주소
    ai.server.url=https://i12b212.p.ssafy.io
     ```
    `S12P11B212/BE/undaid/src/main/resources/application-secrets.properties`
     ```properties
    # Database Credentials
    spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
    spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

    # JWT Configuration
    jwt.secret=${JWT_SECRET}

    # Socket.IO Configuration
    socketio.server.hostname=0.0.0.0
    socketio.server.port=9090
     ``` 
  - AI 서버 환경변수 : `S12P11B212/AI/.env`
    > 주의 사항 : 아래 사이트에 접속하여 API KEY를 발급받아서 사용할 것  
    > [Gemini](https://aistudio.google.com/apikey)  
    > [ChatGPT](https://platform.openai.com/api-keys)
    > ChatGPT의 경우, 결제가 필요함
     ```env
    OPENAI_API_KEY=<YOUR OPENAI API KEY>
    GEMINI_API_KEY=<YOUR GEMINI API KEY>
     ```
  - 프로젝트 환경변수 : `S12P11B212/.env`
     ```env
    MYSQL_ROOT_PASSWORD=YOUR_DB_ROOT_PW
    MYSQL_DATABASE=undaied
    MYSQL_USER=YOUR_DB_ID
    MYSQL_PASSWORD=YOUR_DB_PW

    FRONTEND_PORTS=5173:5173

    JWT_SECRET=your_JWT_SWCRET

    SPRING_DATA_REDIS_HOST=redis
    SPRING_DATA_REDIS_PORT=6379

    REDIS_PASSWORD=YOUR_DB_ROOT_PW
     ```


## 3. 빌드 및 배포 가이드
> 주의사항 : 프로젝트를 새로 클론받았을 경우, docker-compose.override.yml은 아래 내용을 참고하여 직접 수동으로 작성해야하며, docker network 역시 수동으로 생성해야한다. 그 외의 내용들은 이미 작성되어있다.
### 3.1 업데이트 패키지 확인 및 설치
```bash
$ sudo apt update && sudo apt upgrade -y
```

### 3.2 도커 설치
#### 3.2.1 필요한 패키지 설치
```bash
sudo apt install -y apt-transport-https ca-certificates curl software-properties-common
```

#### 3.2.2 Docker 공식 GPG 키를 추가
```bash
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
```

#### 3.2.3 Docker 저장소 추가
```bash
$ sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
```

#### 3.2.4 패키지 목록 재업데이트 
```bash
sudo apt update && sudo apt upgrade -y
```

#### 3.2.5 Docker 설치
```bash
sudo apt install -y docker-ce docker-ce-cli containerd.io
```

#### 3.2.6 도커 설치 확인
```bash
$ docker --version 
# 설치 성공시 "Docker version 27.5.1, build 9f9e405"등이 출력됨
```

#### 3.2.7 현재 사용자를 docker 그룹에 추가하여 sudo 없이 Docker를 실행할 수 있게 설정
> 주의사항 : 이 설정을 적용하려면 로그아웃했다가, 다시 로그인해야함
```bash
sudo usermod -aG docker $USER
```

#### 3.2.8 Docker Compose 설치
```bash
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
```

#### 3.2.9 Docker Compose에 실행 권한 부여
```bash
sudo chmod +x /usr/local/bin/docker-compose
```

#### 3.2.10 Docker Compose 설치 확인
```bash
sudo chmod +x /usr/local/bin/docker-compose
# 설치 성공시 "Docker Compose version v2.32.4" 등이 출력됨
```

#### 3.2.11 Docker Compose에 실행권한 부여
```bash
sudo chmod +x /usr/local/bin/docker-compose
```


### 3.3 docker-compose 작성
#### 프로젝트 루트 디렉토리에 다음 파일들을 만들고 아래 내용을 작성한다
- docker-compose.yml
```yml
services:
 # 프론트엔드 서비스 설정
 frontend:
   build: ./FE                     # FE 디렉토리의 Dockerfile로 빌드
   container_name: FE
   expose:
     - "5173"                      # Vite 개발 서버 포트
   volumes:
     - ./FE:/FE                    # 소스 코드 마운트
     - /FE/node_modules            # node_modules 볼륨 처리
   networks:
     - app_network
   restart: always
   environment:
     - TZ=Asia/Seoul              # 타임존 설정

 # 백엔드 서비스 설정
 backend:
   build: ./BE/undaid             # BE 디렉토리의 Dockerfile로 빌드
   container_name: BE
   expose:
     - "8080"                     # 스프링부트 포트
     - "9090"                     # Netty 포트
   depends_on:                    # MySQL과 Redis 시작 후 실행
     mysql:
       condition: service_started
     redis:
       condition: service_started
   environment:
     SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/undaied?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul
     SPRING_DATASOURCE_USERNAME: ${MYSQL_USER}
     SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD}
     JWT_SECRET: ${JWT_SECRET}
     SOCKETIO_SERVER_HOSTNAME: "0.0.0.0"
     SOCKETIO_SERVER_PORT: 9090
     SPRING_REDIS_HOST: redis
     SPRING_REDIS_PORT: 6379
     SPRING_REDIS_PASSWORD: ${REDIS_PASSWORD}
     TZ: Asia/Seoul
     JAVA_SECURITY_PROPERTIES: |   # Java 보안 설정
       jdk.internal.reflect.permitAll=true
       jdk.reflect.allowNativeAccess=true
   ulimits:                       # 파일 디스크립터 제한 설정
     nofile:
       soft: 65536
       hard: 65536
   networks:
     - app_network
   restart: always
   deploy:                        # 메모리 제한 설정
     resources:
       limits:
         memory: 1G
       reservations:
         memory: 512M

 # AI 서비스 설정
 ai:
   build: ./AI                    # AI 디렉토리의 Dockerfile로 빌드
   container_name: AI
   expose:
     - "8000"                     # FastAPI 포트
   volumes:
     - ./AI:/AI                   # 소스 코드 마운트
   environment:
     - PYTHONPATH=/AI
     - TZ=Asia/Seoul
   networks:
     - app_network
   restart: always

 # MySQL 서비스 설정
 mysql:
   build: 
     context: ./mysql             # MySQL 디렉토리의 Dockerfile로 빌드
   container_name: mysql
   expose:
     - "3306"                     # MySQL 포트
   environment:                   # MySQL 환경 변수 설정
     MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
     MYSQL_DATABASE: ${MYSQL_DATABASE}
     MYSQL_USER: ${MYSQL_USER}
     MYSQL_PASSWORD: ${MYSQL_PASSWORD}
     TZ: Asia/Seoul
     MYSQL_INIT_COMMAND: "SET GLOBAL host_cache_size=0"
   volumes:
     - mysql_data:/var/lib/mysql  # MySQL 데이터 저장
     - ./mysql/pid:/pid           # PID 파일 저장
   networks:
     - app_network
   restart: always
   deploy:                        # 메모리 제한 설정
     resources:
       limits:
         memory: 1G
       reservations:
         memory: 512M

 # Redis 서비스 설정
 redis:
   build: ./redis                 # Redis 디렉토리의 Dockerfile로 빌드
   container_name: redis
   expose:
     - "6379"                     # Redis 포트
   volumes:                       # Redis 설정 및 데이터 마운트
     - ./redis/redis.conf:/usr/local/etc/redis/redis.conf
     - ./redis/var/run/redis:/var/run/redis
     - redis_data:/data
   networks:
     - app_network

# 도커 볼륨 정의
volumes:
 mysql_data:                      # MySQL 데이터 영구 저장
 mysql_pid:                       # MySQL PID 파일 저장
 redis_data:                      # Redis 데이터 영구 저장
 redis_pid:                       # Redis PID 파일 저장

# 도커 네트워크 정의
networks:
 app_network:
   external: true                 # 외부에서 생성된 네트워크 사용
```
- docker-compose.override.yml
```yml
services:
 # MySQL 서비스 설정
 mysql:
   ports:
     - "3306:3306"  # MySQL 기본 포트 매핑 (호스트:컨테이너)
     
 # Redis 서비스 설정  
 redis:
   ports:
     - "6379:6379"  # Redis 기본 포트 매핑 (호스트:컨테이너)
```
- docker-compose.nginx.yml
```yml
# 서비스 정의 시작
services:
 # Nginx 서비스 설정
 nginx:
   image: nginx:alpine    # Alpine 기반 Nginx 이미지 사용
   container_name: nginx  # 컨테이너 이름 지정
   
   # 호스트와 컨테이너 간의 포트 매핑
   ports:
     - "80:80"     # HTTP 포트
     - "443:443"   # HTTPS 포트
   
   # 호스트와 컨테이너 간의 볼륨 마운트
   volumes:
     - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro                # Nginx 메인 설정 파일 (읽기 전용)
     - ./nginx/conf.d:/etc/nginx/conf.d:rw                       # Nginx 추가 설정 파일 (읽기/쓰기)
     - /etc/letsencrypt:/etc/letsencrypt:ro                      # SSL 인증서 파일 (읽기 전용)
     - /etc/ssl/certs:/etc/ssl/certs:ro                         # SSL 인증서 파일 (읽기 전용)
     - /etc/nginx/sites-available:/etc/nginx/sites-available:ro  # 사이트 설정 파일 (읽기 전용)
     - /var/log/nginx:/var/log/nginx                            # Nginx 로그 파일
     - ./nginx/html:/usr/share/nginx/html:ro                     # 정적 파일 디렉토리 (읽기 전용)
   
   networks:
     - app_network    # 사용할 네트워크 지정
   restart: always    # 컨테이너 항상 재시작
   environment:
     TZ: Asia/Seoul  # 타임존 설정

# 네트워크 설정
networks:
 app_network:
   external: true    # 외부에서 생성된 네트워크 사용
```

### 3.4 docker network 생성
#### 아래 명령어를 실행하여 컨테이너간 통신을 위한 네트워크를 생성한다
```bash
$ docker network create app_network
```

#### network 생성 확인
```bash
$ docker network ls
# NETWORK ID     NAME          DRIVER    SCOPE
# 0903bef71aca   app_network   bridge    local
# ^ 이렇게 지정된 해시값과 app_network가 뜨면 잘 생성된 것이다.
```


### 3.5 Dockerfile 작성
#### 3.5.1 프론트엔드 도커 파일
```Dockerfile
# Node.js 22 버전을 기반 이미지로 사용
FROM node:22

# 작업 디렉토리를 /FE로 설정
WORKDIR /FE

# package.json과 package-lock.json 파일을 복사
COPY package*.json ./

# npm 캐시 제거, node_modules 삭제, 패키지 새로 설치
RUN npm cache clean --force && \
   rm -rf node_modules package-lock.json && \
   npm install && \
   npm install sonner && \
   npm install socket.io-client

# 현재 디렉토리의 모든 파일을 컨테이너로 복사
COPY . .

# Vite 개발 서버 포트 5173을 외부에 노출
EXPOSE 5173

# Vite 개발 서버를 실행하고 모든 IP에서 접근 가능하도록 설정
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]
```
#### 3.5.2 백엔드 도커 파일
```Dockerfile
# === 빌드 스테이지 ===
# Gradle 8.12.1과 JDK 17을 포함한 이미지를 빌드 스테이지의 베이스로 사용
FROM gradle:8.12.1-jdk17 AS build

# Gradle 작업 디렉토리 설정
WORKDIR /home/gradle/src

# 현재 디렉토리의 모든 파일을 gradle:gradle 권한으로 복사
COPY --chown=gradle:gradle . .

# 테스트 제외하고 데몬 없이 빌드 실행
RUN gradle build -x test --no-daemon

# === 실행 스테이지 ===
# JDK 17을 포함한 이미지를 실행 스테이지의 베이스로 사용
FROM openjdk:17-jdk

# 애플리케이션 디렉토리 설정
WORKDIR /app

# 빌드 스테이지에서 생성된 JAR 파일을 app.jar로 복사
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

# 스프링부트 기본 포트 노출
EXPOSE 8080

# Java 모듈 시스템의 접근 제한을 해제하기 위한 옵션들 설정
ENV JAVA_OPTS="\
   --add-opens=java.base/sun.nio.ch=ALL-UNNAMED \
   --add-opens=java.base/java.lang=ALL-UNNAMED \
   --add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
   --add-opens=java.base/java.io=ALL-UNNAMED \
   --add-opens=java.base/java.nio=ALL-UNNAMED \
   --add-opens=java.base/java.util=ALL-UNNAMED \
   --add-opens=java.base/java.util.concurrent=ALL-UNNAMED \
   --add-opens=java.base/java.net=ALL-UNNAMED \
   --add-opens=java.base/jdk.internal.misc=ALL-UNNAMED \
   -Dio.netty.tryReflectionSetAccessible=true \
   -Djava.security.egd=file:/dev/./urandom"

# JAR 파일 실행 (Java 옵션 포함)
CMD java $JAVA_OPTS -jar app.jar
```
#### 3.5.3 AI서버 도커 파일
```Dockerfile
# Python 4.12.8 버전을 기반 이미지로 사용
FROM python:3.12.8

# 작업 디렉토리를 /AI로 설정
WORKDIR /AI

# requirements.txt 파일만 먼저 복사
COPY ./requirements.txt /AI/requirements.txt

# pip 패키지 설치 (캐시 없이 최신 버전으로)
RUN pip install --no-cache-dir --upgrade -r /AI/requirements.txt

# 현재 디렉토리의 모든 파일을 컨테이너로 복사
COPY . /AI

# FastAPI 서버 포트 8000을 외부에 노출 
EXPOSE 8000

# uvicorn을 사용해 FastAPI 서버 실행 (모든 IP에서 접근 가능하도록)
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000"]
```
#### 3.5.4 MySQL 도커 파일
```Dockerfile
# MySQL 8.0.41 버전을 기반 이미지로 사용
FROM mysql:8.0.41

# 컨테이너의 타임존을 서울로 설정
ENV TZ=Asia/Seoul

# 커스텀 MySQL 설정 파일을 컨테이너에 복사
COPY mysql.cnf /etc/mysql/conf.d/custom.cnf

# 초기화 SQL 스크립트를 도커 엔트리포인트 초기화 디렉토리에 복사
COPY init/ /docker-entrypoint-initdb.d/

# MySQL 기본 포트 3306을 외부에 노출
EXPOSE 3306
```
#### 3.5.5 Redis 도커 파일
```Dockerfile
FROM redis:alpine

# 타임존 설정
ENV TZ=Asia/Seoul

# Redis 설정 파일 복사 : 프젝 디렉토리를 통짜로 복사 안해도 됨
COPY redis.conf /usr/local/etc/redis/redis.conf

# 메모리 제한
LABEL org.opencontainers.image.resources.memory.limit=256M

# 컨테이너 실행 시 설정 파일 사용
CMD ["redis-server", "/usr/local/etc/redis/redis.conf"]
```
#### 3.5.6 Nginx 도커 파일
```Dockerfile
# Alpine 기반의 경량화된 Nginx 이미지 사용
FROM nginx:alpine

# Nginx 설정 파일이 위치한 디렉토리로 작업 디렉토리 변경
WORKDIR /etc/nginx

# 현재 디렉토리의 모든 파일을 Nginx 설정 디렉토리로 복사
COPY . /etc/nginx/
```


### 3.6 방화벽 포트 허용
#### 3.6.1 아래 명령어를 실행하여, 도커 컨테이너 통신을 위한 포트를 방화벽에서 허용한다
```bash
sudo ufw allow 80,443,8000,8080,5173,9090,3306,6379/tcp
``` 

#### 3.6.2 방화벽에서 허용된 포트들 확인하기
```bash
$ sudo ufw status numbered
```

#### 3.6.3 허용된 포트들을 적용하기(방화벽 활성화)
```bash
$ sudo ufw enable
```

### 3.7 도커 컨테이너 빌드
> 주의사항 : https 통신을 위해 반드시 nginx 컨테이너를 먼저 실행한다
#### 리버스 프록시 서버로 세팅된 nginx 컨테이너는 docker-compose.nginx.yml로 빌드된다. 아래 명령어를 실행하여 nginx 컨테이너를 빌드, 실행한다.
```bash
$ docker-compose -f docker-compose.nginx.yml up --build
```

#### 프론트엔드, 백엔드, AI, MySQL, Redis 컨테이너는 docker-compose.yml로 동시에 빌드된다. 아래 명령어를 실행하여 프론트엔드, 백엔드, AI, MySQL, Redis 컨테이너를 빌드, 실행한다. 이때 docker-compose.override.yml도 빌드, 실행에 적용된다.
```bash
$ docker-compose up --build  # docker-compose.override.yml 내용이 오버라이드됨
```

#### docker-compose.override.yml의 역할
  > 배포환경에서는 서비스 보안정책에 따라 지정된 포트로만 외부에서 접근 가능해야한다. 따라서 도커 네트워크 안에서만 통신할 수 있게 해주는 expose 옵션으로 컨테이너를 빌드해야한다. 하지만 개발환경에서는 설정, 디버깅, 서버와의 연동 등 다양한 이유로 인해 도커 네트워크 외부에서도 접속을 허용하는 경우가 많아 ports 옵션으로 컨테이너를 빌드할 필요가 있다. docker-compose.override.yml은 환경에 맞춰 포트, 볼륨 등 다양한 컨테이너 설정을 오버라이드 할 수 있어서 하나의 docker-compose.yml을 가지고 다양한 환경에서 커스텀할 수 있는 장점이 있다.


## 4. 리버스 프록시 설정 가이드
### 4.1 SSL 인증서 설정
#### 4.1.1 certbot 기본 패키지 설치
```bash
$ sudo apt install certbot
```

#### 4.1.2 Nginx 플러그인 설치
```bash
$ sudo apt install python3-certbot-nginx
```

#### 4.1.3 Certbot으로 SSL/TLS 인증서 발급
```bash
$ sudo certbot -d i12b212.p.ssafy.io
#                  ^ 이 부분은 호스팅할 서버의 도메인으로 교체
# 아래와 같이 뜨면 성공이다
# Congratulations! Your certificate and chain have been saved at:
#   /etc/letsencrypt/live/i12b212.p.ssafy.io/fullchain.pem
#   Your key file has been saved at:
#   /etc/letsencrypt/live/i12b212.p.ssafy.io/privkey.pem 
```

#### 4.1.4 Certbot으로 발급된 SSL/TLS 인증서 목록 확인
```bash
$ sudo certbot certificates
```

### 4.2 nginx 기본 설정
```conf
# nginx/nginx.conf
# nginx 실행 사용자 지정
user nginx;
# CPU 코어 수에 맞게 워커 프로세스 자동 설정
worker_processes auto;
# nginx 마스터 프로세스 ID 저장 위치
pid /run/nginx.pid;

events {
    # 워커 프로세스당 최대 동시 접속 수 설정
    worker_connections 768;
}

http {
    # 정적 파일 전송 최적화 설정
    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    # 접속 유지 시간 설정 (65초)
    keepalive_timeout 65;
    # MIME 타입 해시 테이블 크기 설정
    types_hash_max_size 2048;

    # MIME 타입 설정 파일 포함
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    # SSL 프로토콜 버전 설정
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_prefer_server_ciphers on;

    # 로그 파일 위치 설정
    access_log /var/log/nginx/access.log;
    error_log /var/log/nginx/error.log;

    # gzip 압축 사용
    gzip on;

    # 추가 설정 파일 포함
    include /etc/nginx/conf.d/*.conf;
}
```

### 4.3 nginx 확장 설정
> 주의사항 : server_name 부분을 호스팅할 서버의 도메인으로 대체하고, SSL 설정부분도 인증키의 절대경로를 찾아 대체할 것
```conf
# /nginx/conf.d/default.conf
resolver 127.0.0.11 valid=30s;  # Docker의 내부 DNS 서버

# Upstream 설정
upstream frontend {
    zone upstream_frontend 64k;
    server frontend:5173 resolve;
}

upstream backend {
    zone upstream_backend 64k;
    server backend:8080 resolve;
}

upstream ai {
    zone upstream_ai 64k;
    server ai:8000 resolve;
}

upstream socket-server {
    zone upstream_socket 64k;
    server backend:9090 resolve;  # WebSocket 서버
}

upstream jenkins {
    zone upstream_jenkins 64k;
    server jenkins:8443 resolve;
}

# HTTP to HTTPS redirect
server {
    listen 80;
    server_name i12b212.p.ssafy.io;
    return 301 https://$server_name$request_uri;
}

# Main server configuration
server {
    listen 443 ssl;
    listen [::]:443 ssl;
    server_name i12b212.p.ssafy.io;

    # SSL 설정
    ssl_certificate /etc/letsencrypt/live/i12b212.p.ssafy.io/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/i12b212.p.ssafy.io/privkey.pem;
    include /etc/letsencrypt/options-ssl-nginx.conf;
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;

    # 공통 프록시 설정
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;

    # Frontend routing
    location / {
        proxy_pass http://frontend/;
        proxy_intercept_errors on;
        error_page 502 503 504 = @maintenance;
    }

    # Backend REST API
    location /api/v1/ {
        proxy_pass http://backend/api/v1/;
        proxy_intercept_errors on;
        error_page 503 = @maintenance;
    }

    # AI API
    location /api/ai/ {
        proxy_pass http://ai/api/ai/;
        proxy_intercept_errors on;
        error_page 503 = @maintenance;
    }

    # WebSocket endpoint
    location /socket.io/ {
        proxy_pass http://socket-server/socket.io/;
        
        # WebSocket 필수 설정
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header X-Forwarded-Host $host; # 추가
        proxy_set_header X-Forwarded-Port $server_port; # 추가

        
        # 버퍼 설정
        proxy_buffering off;
        proxy_buffer_size 128k;
        proxy_buffers 4 256k;
        proxy_busy_buffers_size 256k;
        
        # 긴 연결 유지를 위한 타임아웃 설정
        proxy_connect_timeout 3600s;
        proxy_send_timeout 3600s;
        proxy_read_timeout 3600s;
        
        # TCP 최적화
        proxy_socket_keepalive on;
        tcp_nodelay on;
        
        # WebSocket 로깅
        error_log /var/log/nginx/socket-error.log debug;
        access_log /var/log/nginx/socket-access.log;
    }

    # Jenkins
    location /jenkins {
        proxy_pass http://jenkins;
        proxy_read_timeout 9000;
        proxy_intercept_errors on;
        error_page 503 = @maintenance;

        # Jenkins HTTPS 설정 추가
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Server $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Real-IP $remote_addr;
        
        # Jenkins WebSocket 지원
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        
        # Jenkins 경로 설정
        proxy_redirect http:// https://;
    }

    # Maintenance page
    location @maintenance {
        root /usr/share/nginx/html;
        try_files /maintenance.html =503;
    }
}
```

## 5. 데이터베이스 설정 가이드
### 5.1 MySQL 접속 방법
#### 5.1.1 MySQL 쉘로 컨테이너에 접속
```bash
$ docker exec -it mysql mysql -u YOUR_DB_ID -p
# docker exec -it 컨테이너명 쉘 -u 사용자 -p
```

#### 5.1.2 비밀번호를 요구하므로 환경변수로 설정해둔 비밀번호를 입력한다
> 주의사항: 비밀번호는 입력과정이 보이지 않는다.
```mysql
>  # 여기에 비밀번호를 입력
```

#### 5.1.3 mysql/init/init.sql에 작성된 초기쿼리문을 실행한다

### 5.2 Redis 접속 방법
#### 5.2.1 Redis-cli로 컨테이너에 접속
```bash
$ docker exec -it redis redis-cli
# docker exec -it 컨테이너명 쉘
```

#### 5.2.2 아래 명령어를 입력하여 초기 세팅을 진행한다
```redis-cli
flushall
```

```redis-cli
set room:sequence 0
```
> 만약 비밀번호를 요구할 경우, 아래 명령어를 입력한다. 이때, YOUR_PASSWORD 는 환경변수로 설정한 비밀번호를 입력한다. 또한 redis는 MySQL과 달리, 비밀번호가 그대로 노출되므로 주의한다.
```redis-cli
AUTH YOUR_PASSWORD
```

## 6. 서버 가동 및 종료
### 6.1 서버 가동
> 3~5 의 모든 과정을 빠짐없이, 성공적으로 마쳤다면 이미 컨테이너가 실행되서 서버가 열린 상태일 것이다.   
> 만약 서버를 켜고 싶다면 터미널상에서 프로젝트 루트 디렉토리로 이동한 후 아래 명령어를 입력하면 된다
> ```bash
> $ docker-compose up
> ```
> 만약 소스코드의 변동사항이 있다면, 다시 빌드한 뒤 서버를 가동하면 된다
> ```bash
> $ docker-compose up --build
> ```

### 6.2 서버 종료
> `docker-compose up` 명령어는 기본적으로 attatch모드에서 실행되므로 이 상태에서 서버를 종료하고 싶다면 ^C를 입력해주면 된다  
> 만약 다른 터미널에서 혹은 detatch모드로 실행중인 서버를 종료하고 싶을 경우, 아래 명령어로 종료할 수 있다.  
> ```bash
> $ docker-compose down
> ```
