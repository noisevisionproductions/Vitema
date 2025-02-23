package com.noisevisionsoftware.nutrilog.model.user;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.nutrilog.security.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    private Timestamp createdAt;
    private String email;
    private String nickname;
    private Gender gender;
    private Timestamp birthDate;
    private Integer storedAge;
    private Boolean profileCompleted;
    private UserRole role;
    private String note;
}