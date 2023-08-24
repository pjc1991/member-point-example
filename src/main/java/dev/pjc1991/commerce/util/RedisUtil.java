package dev.pjc1991.commerce.util;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.stereotype.Component;

/**
 * Redis 관련 유틸
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisUtil {

    private final CacheManager cacheManager;

    /**
     * 특정한 패턴으로 시작되는 캐시를 삭제합니다.
     * @param name 캐시 이름
     * @param keyPattern 키 패턴
     */
    public void evictCacheWithPattern(String name, String keyPattern) {
        RedisCache cache = (RedisCache) cacheManager.getCache(name);
        assert cache != null;
        cache.getNativeCache().clean(name, (name + "::" + keyPattern).getBytes());
    }
}
