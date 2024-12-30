package com.learn.microservices.userservice.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PaginationOptions {
    private int page = 0;
    private int size = 10;
    private String sortBy;
    private String sortOrder;
}
