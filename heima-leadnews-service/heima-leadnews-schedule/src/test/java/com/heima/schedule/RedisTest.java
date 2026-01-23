package com.heima.schedule;

import com.heima.common.redis.CacheService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RedisTest {

    @Autowired
    private CacheService cacheService;

    @Test
    public void zsetTest() {
        cacheService.zAdd("zKey", "001", 80);
        cacheService.zAdd("zKey", "002", 30);
        cacheService.zAdd("zKey", "003", 50);
        cacheService.zAdd("zKey", "004", 10);
    }
}
