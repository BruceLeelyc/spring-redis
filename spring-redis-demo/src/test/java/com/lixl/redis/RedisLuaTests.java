package com.lixl.redis;

import com.lixl.redis.config.RedisService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringRedisDemoApplication.class)
public class RedisLuaTests {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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

    private DefaultRedisScript<List> getRedisScript;

    @Before
    public void init() {
        getRedisScript = new DefaultRedisScript<List>();
        getRedisScript.setResultType(List.class);
        ClassPathResource classPathResource = new ClassPathResource("/luascript/simple.lua");
//        ClassPathResource classPathResource = new ClassPathResource("/luascript/LimitLoadTimes.lua");
        getRedisScript.setScriptSource(new ResourceScriptSource(classPathResource));
    }

    @Test
    public void testRedis() {
        /**
         * List设置lua的KEYS
         */
        List<String> keyList = new ArrayList();
        keyList.add("test.count");
        keyList.add("test.rate");

        /**
         * 用Mpa设置Lua的ARGV[1]
         */
        List<Map> args = new ArrayList<>();
        Map<String,Object> argvMap = new HashMap<String,Object>();
        argvMap.put("expire", 10000);
        argvMap.put("times", 10);
        args.add(argvMap);

        /**
         * 调用脚本并执行
         */
        List<String> execute = (List<String>)redisTemplate.execute(getRedisScript, keyList, args);
        System.out.println(execute);
        System.out.println(666);
    }

}
