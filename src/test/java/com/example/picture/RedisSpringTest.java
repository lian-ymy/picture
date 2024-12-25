package com.example.picture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@SpringBootTest
public class RedisSpringTest {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void testRedisStringOperations() {
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();

        String key = "jingliu";
        String value = "kafuka";

        valueOperations.set(key, value);
        String value2 = valueOperations.get(key);
    }
}
