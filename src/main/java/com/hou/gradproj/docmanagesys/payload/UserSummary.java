package com.hou.gradproj.docmanagesys.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UserSummary {
    private Long id;
    private String username;
    private String name;
}
