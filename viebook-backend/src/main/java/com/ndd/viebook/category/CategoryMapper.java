package com.ndd.viebook.category;

import java.util.ArrayList;

public class CategoryMapper {
    public static CategoryResponse fromEntity(Category category){
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .totalContentCount(category.getTotalContentCount())
                .slug(category.getSlug())
                .active(category.isActive())
                .parent(category.getParent() == null ? null : category.getParent().getId())
                .children(
                        category.getChildren() == null ?
                            new ArrayList<>() :
                            category.getChildren().stream().map(CategoryMapper::fromEntity).toList()
                )
                .build();
    }
}
