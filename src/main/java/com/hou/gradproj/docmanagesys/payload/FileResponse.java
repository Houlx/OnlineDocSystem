package com.hou.gradproj.docmanagesys.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FileResponse {
    private Long id;
    private String name;
    private Long type;
    private int size;
    private String path;
    private String createdAt;
    private String updatedAt;
}
