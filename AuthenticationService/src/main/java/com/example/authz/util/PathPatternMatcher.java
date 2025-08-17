package com.example.authz.util;

import org.springframework.util.AntPathMatcher;

public class PathPatternMatcher {
    private static final AntPathMatcher matcher = new AntPathMatcher();

    public static boolean matches(String pattern, String path) {
        if ("*".equals(pattern)) pattern = "**";
        return matcher.match(pattern, path);
    }

    public static int computeSpecificity(String pattern) {
        if ("**".equals(pattern)) return 0;
        String[] segments = pattern.split("/");
        int score = 0;
        for (String seg : segments) {
            if (!seg.contains("*")) score += 1000;
        }
        return score + segments.length;
    }
}