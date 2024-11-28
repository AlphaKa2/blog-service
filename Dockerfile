# 빌드 스테이지
FROM gradle:8.3-jdk17 AS build-stage

# 작업 디렉토리 설정
WORKDIR /app/blog-service

# 의존성 캐시를 활용하기 위해 build.gradle 파일만 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Gradle 의존성 다운로드
RUN gradle dependencies --no-daemon

# 전체 프로젝트 복사
COPY . .

# 빌드 (테스트 제외)
RUN gradle clean build -Dspring.profiles.active=develop -x test --no-daemon || true

---

# 런타임 스테이지
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app/blog-service

# 빌드 결과물 복사
COPY --from=build-stage /app/build/libs/*.jar app.jar

# Elastic APM Agent 다운로드 및 복사
RUN apt-get update && apt-get install -y wget \
    && wget -O elastic-apm-agent.jar https://search.maven.org/remotecontent?filepath=co/elastic/apm/elastic-apm-agent/1.50.0/elastic-apm-agent-1.50.0.jar \
    && apt-get remove -y wget && apt-get autoremove -y && apt-get clean && rm -rf /var/lib/apt/lists/*

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
