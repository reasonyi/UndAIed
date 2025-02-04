FROM node:latest

WORKDIR /FE

# package*.json 파일 복사
COPY package*.json ./

# 기존 의존성 제거 및 캐시 정리
RUN rm -rf node_modules package-lock.json && \
    npm cache clean --force

# 의존성 설치
RUN npm install

# 소스 코드 복사
COPY . .

EXPOSE 5173

# Vite 개발 서버 실행
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]