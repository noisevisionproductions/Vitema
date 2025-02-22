package com.noisevisionsoftware.nutrilog.security.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FirebaseUser {

    private String uid;
    private String email;
    private String role;
}
