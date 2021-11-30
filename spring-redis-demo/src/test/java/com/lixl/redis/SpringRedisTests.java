package com.lixl.redis;

import com.lixl.redis.config.RedisService;
import com.lixl.redis.pojo.User;
import com.lixl.redis.utils.RedisKeyUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringRedisDemoApplication.class)
public class SpringRedisTests {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void testRedis() {
        System.out.println(666);
        String key = "test.key";
        String value = "test.value";
        redisTemplate.opsForValue().set(key, value, 60 * 60, TimeUnit.SECONDS);

        String val = redisTemplate.opsForValue().get(key).toString();
        System.out.println(val);
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ValueOperations<String, Object> valueOperations;

    @Autowired
    private HashOperations<String, String, Object> hashOperations;

    @Autowired
    private ListOperations<String, Object> listOperations;

    @Autowired
    private SetOperations<String, Object> setOperations;

    @Autowired
    private ZSetOperations<String, Object> zsetOperations;

    @Resource
    private RedisService redisService;

    @Test
    public void testLockTime() {
        try {
            redisTemplate.opsForValue().set("test_key", "test_value", 10, TimeUnit.SECONDS);
            Object key = redisTemplate.opsForValue().get("test_key");
            System.out.println("key="+key);
            redisService.expandLockTimeHold("field", "test_key", "test_value", 10);
            Thread.sleep(50*1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            redisService.deleteKey("key");
        }
        System.out.println("处理成功.");
    }

    @Test
    public void testObj() throws Exception {
        User user = new User();
        user.setRemark("上海");
        user.setUserName("测试dfas");
        user.setAge(123);
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        redisService.expireKey("name", 20, TimeUnit.SECONDS);
        String key = RedisKeyUtil.getKey(user.getClass().getSimpleName(), "name", user.getUserName());
        operations.set(key, user, 20, TimeUnit.SECONDS);
        User vo = (User) operations.get(key);
        System.out.println(vo);
    }

    @Test
    public void testValueOption() throws Exception {
        User User = new User();
        User.setRemark("上海");
        User.setUserName("name");
        User.setAge(23);
        valueOperations.set("test", User);

        System.out.println(valueOperations.get("test"));
    }

    @Test
    public void testSetOperation() throws Exception {
        User User = new User();
        User.setRemark("北京");
        User.setUserName("hahaha");
        User.setAge(23);
        User aUser = new User();
        aUser.setRemark("天津");
        aUser.setUserName("tianjing");
        aUser.setAge(23);
        setOperations.add("user:test", User, aUser);
        Set<Object> result = setOperations.members("user:test");
        System.out.println(result);
    }

    @Test
    public void HashOperations() throws Exception {
        User User = new User();
        User.setRemark("北京");
        User.setUserName("beijing");
        User.setAge(23);
        hashOperations.put("hash:user", User.hashCode() + "", User);
        System.out.println(hashOperations.get("hash:user", User.hashCode() + ""));
    }

    @Test
    public void ListOperations() throws Exception {
        User User = new User();
        User.setRemark("北京");
        User.setUserName("beijing");
        User.setAge(23);
//        listOperations.leftPush("list:user",User);
//        System.out.println(listOperations.leftPop("list:user"));
        // pop之后 值会消失
        System.out.println(listOperations.leftPop("list:user"));
    }
}
