# 자바 버전 선택
FROM openjdk:17-jdk-alpine

# 작업 디렉토리 설정
WORKDIR /app/blog-service

# APM 에이전트 다운로드 및 복사
ADD https://search.maven.org/remotecontent?filepath=co/elastic/apm/elastic-apm-agent/1.50.0/elastic-apm-agent-1.50.0.jar /elastic-apm-agent.jar

# gradle로 빌드된 jar파일을 현재 디렉토리에 복사
COPY build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8003

# 애플리케이션 실행
ENTRYPOINT ["java", "-javaagent:/elastic-apm-agent.jar", "-jar", "app.jar", "--spring.profiles.active=develop"]