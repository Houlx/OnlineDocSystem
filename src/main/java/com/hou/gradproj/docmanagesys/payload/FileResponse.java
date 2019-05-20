package com.hou.gradproj.docmanagesys.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class FileResponse {
    private Long id;
    private String name;
    private Long type;
    private int size;
    private String createdAt;
    private String updatedAt;
    private String groupName;
    private String remoteFileName;
}
