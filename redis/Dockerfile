FROM redis:alpine

# 타임존 설정
ENV TZ=Asia/Seoul

# Redis 설정 파일 복사 : 프젝 디렉토리를 통짜로 복사 안해도 됨
COPY redis.conf /usr/local/etc/redis/redis.conf

# 메모리 제한
LABEL org.opencontainers.image.resources.memory.limit=256M

# 컨테이너 실행 시 설정 파일 사용
CMD ["redis-server", "/usr/local/etc/redis/redis.conf"]