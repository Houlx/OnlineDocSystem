package com.hou.gradproj.docmanagesys;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

@SpringBootApplication
@EntityScan(basePackageClasses = {
        DocumentManagementApplication.class,
        Jsr310JpaConverters.class
})
public class DocumentManagementApplication {
    @PostConstruct
    void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8:00"));
    }

    public static void main(String[] args) {
        SpringApplication.run(DocumentManagementApplication.class, args);
    }

}
