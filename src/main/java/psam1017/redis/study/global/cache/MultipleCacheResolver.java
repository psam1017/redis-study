package psam1017.redis.study.global.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import psam1017.redis.study.domain.book.BoardService;
import psam1017.redis.study.domain.user.UserService;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 만약 한 번에 하나의 캐시 매니저만을 적용한다면 SimpleCacheResolver 를 사용할 수도 있다.
 */
@SuppressWarnings("NullableProblems")
public class MultipleCacheResolver implements CacheResolver {

    private final CacheManager userCacheManager;
    private final CacheManager boardCacheManager;

    public MultipleCacheResolver(CacheManager userCacheManager, CacheManager boardCacheManager) {
        this.userCacheManager = userCacheManager;
        this.boardCacheManager = boardCacheManager;
    }

    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        Object target = context.getTarget();
        Collection<Cache> caches = new ArrayList<>();

        if (target instanceof UserService) {
            caches.add(userCacheManager.getCache(CacheName.USER_CACHE_NAME));
        } else if (target instanceof BoardService) {
            caches.add(boardCacheManager.getCache(CacheName.BOARD_CACHE_NAME));
        }
        return caches;
    }
}
