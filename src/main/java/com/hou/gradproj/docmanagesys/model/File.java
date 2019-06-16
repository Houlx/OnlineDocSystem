package com.hou.gradproj.docmanagesys.model;

import com.hou.gradproj.docmanagesys.model.audit.UserDateAudit;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigInteger;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "files")
@Data
@NoArgsConstructor
public class File extends UserDateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 140)
    private String name;

    private BigInteger size;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_id", nullable = false)
    private FileType type;

    @NotBlank
    private String groupName;

    @NotBlank
    private String remoteFileName;

    public File(@NotBlank @Size(max = 140) String name, @NotBlank BigInteger size, FileType type, @NotBlank String groupName, @NotBlank String remoteFileName) {
        this.name = name;
        this.size = size;
        this.type = type;
        this.groupName = groupName;
        this.remoteFileName = remoteFileName;
    }
}
