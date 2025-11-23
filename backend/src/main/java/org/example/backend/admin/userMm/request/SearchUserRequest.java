package org.example.backend.admin.userMm.request;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class SearchUserRequest {
    private String q;
    private Integer status;
    private String gender;
    @PositiveOrZero
    private Integer pageNum;
    @PositiveOrZero
    private Integer pageSize;
}