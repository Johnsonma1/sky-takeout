package com.sky.dto;

import lombok.Data;

@Data
public class EditPasswordDTO {
    private String oldPassword;
    private String newPassword;
}
