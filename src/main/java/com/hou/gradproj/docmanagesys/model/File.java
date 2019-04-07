package com.hou.gradproj.docmanagesys.model;

import com.hou.gradproj.docmanagesys.model.audit.UserDateAudit;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigInteger;

@Entity
@Table(name = "files")
@NoArgsConstructor
@Getter
@Setter
public class File extends UserDateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 140)
    private String name;

    @NotBlank
    private BigInteger size;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_type_id", nullable = false)
    private FileType type;

    @NotBlank
    private String path;

    @NotBlank
    private Boolean deleted;

    public File(@NotBlank @Size(max = 140) String name, @NotBlank BigInteger size, FileType type, @NotBlank String path, @NotBlank Boolean deleted) {
        this.name = name;
        this.size = size;
        this.type = type;
        this.path = path;
        this.deleted = deleted;
    }
}
