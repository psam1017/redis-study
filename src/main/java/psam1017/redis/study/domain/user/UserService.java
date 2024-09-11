package psam1017.redis.study.domain.user;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import psam1017.redis.study.global.cache.CacheName;

import java.util.List;

@SuppressWarnings("JavadocBlankLines")
@Transactional
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 1. @Caching 을 사용하여 여러 개의 @Cacheable 을 사용할 수 있고, 결과적으로 여러 개의 CacheManager 를 유연하게 사용할 수 있다.
     * 관련 학습 : "Multi-layered Caching Strategies", "Distributed Caching" 검색
     *
     * 2. cacheManager 를 지정하지 않으면 CacheResolver 에서 조건에 맞는 CacheManager 를 찾아서 적용한다.
     * 3. key 설정에는 SpEL, KeyGenerator 등을 사용할 수 있다. 생략 시 SimpleKeyGenerator 에 의해 key 가 생성된다.
     * redis 에서 ex) users::page:1:size:10 라는 키에 값이 저장된다.
     */
    @Cacheable(
            cacheNames = CacheName.USER_CACHE_NAME,
            key = """
                    T(String).format('page:%d:size:%d', #page, #size)
                    """
    )
    public List<User> getUsers(int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<User> users = userRepository.findAllByOrderByIdDesc(pageable);
        return users.getContent();
    }
}
