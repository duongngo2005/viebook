package com.ndd.viebook.common.utils;

import com.github.slugify.Slugify;

public final class SlugUtils {
    private static final Slugify SLUGIFY = Slugify.builder()
            .customReplacement("đ", "d")
            .customReplacement("Đ", "d")
            .lowerCase(true)
            .build();

    private SlugUtils(){}

    public static String toSlug(String input){
        return SLUGIFY.slugify(input);
    }
}
