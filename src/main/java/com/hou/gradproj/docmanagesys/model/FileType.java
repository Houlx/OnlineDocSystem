package com.hou.gradproj.docmanagesys.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;

@Entity
@Table(name = "file_types")
@Data
@NoArgsConstructor
public class FileType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NaturalId
    @Column(length = 30)
    private FileTypeName name;

    public FileType(FileTypeName name) {
        this.name = name;
    }
}
