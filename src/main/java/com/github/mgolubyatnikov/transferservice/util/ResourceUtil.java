package com.github.mgolubyatnikov.transferservice.util;

import com.google.common.io.Resources;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ResourceUtil {

    public static String toString(String filename) {
        try {
            return Resources.toString(Resources.getResource(filename), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
