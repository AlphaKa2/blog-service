# 자바 버전 선택
FROM openjdk:17-jdk-alpine

# 작업 디렉토리 설정
WORKDIR /app/blog-service

# Elastic APM Java Agent 다운로드
ADD https://github.com/elastic/apm-agent-java/releases/download/v1.50.0/elastic-apm-agent-1.50.0.jar /opt/elastic-apm-agent.jar

# gradle로 빌드된 jar파일을 현재 디렉토리에 복사
COPY build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8003

# 애플리케이션 실행
ENTRYPOINT [
  "java",
  "-javaagent:/opt/elastic-apm-agent.jar",
  "-Delastic.apm.service_name=${ELASTIC_APM_SERVICE_NAME}",
  "-Delastic.apm.server_urls=${ELASTIC_APM_SERVER_URLS}",
  "-Delastic.apm.application_packages=${ELASTIC_APM_APPLICATION_PACKAGES}",
  "-Delastic.apm.secret_token=${ELASTIC_APM_SECRET_TOKEN}",
  "-Delastic.apm.environment=${ELASTIC_APM_ENVIRONMENT}",
  "-jar",
  "app.jar",
  "--spring.profiles.active=develop"
]