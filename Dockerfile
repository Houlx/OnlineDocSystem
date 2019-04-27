FROM java:8
VOLUME /file_storage
EXPOSE 9090
ADD ./docmanagesys-0.0.1-SNAPSHOT.jar docmanagesys.jar
ENTRYPOINT ["java","-jar","docmanagesys.jar","--spring.profiles.active=prod"]