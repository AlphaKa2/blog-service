# 1단계: 빌드 단계
FROM gradle:8.0-jdk17 AS build

# 작업 디렉토리 설정
WORKDIR /home/gradle/project

# 소스 코드를 컨테이너로 복사
COPY --chown=gradle:gradle . .

# 프로젝트 빌드
RUN gradle build --no-daemon

# 2단계: 실행 단계
FROM openjdk:17-jdk-alpine

# 빌드한 JAR 파일을 복사
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8003

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]