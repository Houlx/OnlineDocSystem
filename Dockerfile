FROM java:8
EXPOSE 9090
VOLUME /config
ADD ./docmanagesys-0.0.1-SNAPSHOT.jar docmanagesys.jar
ENTRYPOINT ["java","-jar","docmanagesys.jar","--spring.profiles.active=prod"]