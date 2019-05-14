package com.hou.gradproj.docmanagesys.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.time.Instant;

@AllArgsConstructor
@Getter
@Setter
public class UserProfile {
    private Long id;
    private String username;
    private String name;
    private BigInteger storageRoom;
    private BigInteger alreadyUsedRoom;
//    private Instant joinedAt;
    private String joinedAt;
}
