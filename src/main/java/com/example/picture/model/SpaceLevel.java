package com.example.picture.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
@AllArgsConstructor
public class SpaceLevel {
    private int value;

    private String text;

    private long maxCount;

    private long maxSize;
}
