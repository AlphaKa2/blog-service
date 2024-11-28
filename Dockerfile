# 자바 버전 선택
FROM openjdk:17-jdk-alpine

# 작업 디렉토리 설정
WORKDIR /app/blog-service

# gradle로 빌드된 jar파일을 현재 디렉토리에 복사
COPY build/libs/*.jar app.jar

# Elastic APM Agent 다운로드 및 복사
RUN apk add --no-cache wget \
    && wget -O elastic-apm-agent.jar https://search.maven.org/remotecontent?filepath=co/elastic/apm/elastic-apm-agent/1.50.0/elastic-apm-agent-1.50.0.jar

# 포트 노출
EXPOSE 8003

# APM Agent와 함께 애플리케이션 실행
ENTRYPOINT java -javaagent:/app/blog-service/elastic-apm-agent.jar \
            -Delastic.apm.server_urls=$ELASTIC_APM_SERVER_URLS \
            -Delastic.apm.service_name=blog-service \
            -Delastic.apm.environment=develop \
            -Delastic.apm.secret_token=$ELASTIC_APM_SECRET_TOKEN \
            -Delastic.apm.application_packages=com.alphaka.blogservice \
            -jar app.jar --spring.profiles.active=develop
