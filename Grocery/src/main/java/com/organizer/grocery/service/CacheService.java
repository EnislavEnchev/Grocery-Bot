package com.organizer.grocery.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public CacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(String key, Object value) {
        System.out.println("Saving key: " + key + " with value: " + value);
        if(value == null) {
            System.out.println("Value is null, deleting key: " + key);
            delete(key);
            return;
        }
        redisTemplate.opsForValue().set(key, value);
        System.out.println("Just saved key: " + key + " with value: " + redisTemplate.opsForValue().get(key));
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
