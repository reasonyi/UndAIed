FROM node:22

WORKDIR /FE

COPY package*.json ./

RUN npm cache clean --force && \
    rm -rf node_modules package-lock.json && \
    npm install && \
    npm install sonner && \
    npm install socket.io-client

COPY . .

EXPOSE 5173

CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]