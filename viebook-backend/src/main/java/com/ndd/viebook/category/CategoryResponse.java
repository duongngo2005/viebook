package com.ndd.viebook.category;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private boolean active;
    private int totalContentCount;
    private Long parent = null;
    private List<CategoryResponse> children = null;
}
