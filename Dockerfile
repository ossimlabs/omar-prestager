FROM openjdk:14-alpine
COPY build/libs/omar-prestager-*.jar omar-prestager.jar
EXPOSE 8080
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx128m", "-jar", "omar-prestager.jar"]