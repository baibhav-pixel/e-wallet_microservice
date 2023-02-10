package com.example.microservices;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserCacheRepository {

    private final RedisTemplate<String,Object> redisTemplate;
    public static final String KEY_PREFIX="user::";

    UserCacheRepository(RedisTemplate<String,Object> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    public void save(User user)
    {
        redisTemplate.opsForValue().set(getKey(user.getId()), user);
    }

    private String getKey(int id){
        return KEY_PREFIX+id;
    }

    public User get(int userId) {
        return (User) redisTemplate.opsForValue().get(getKey(userId));
    }
}
