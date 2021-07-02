--获取KEY
local key1 = KEYS[1]
local key2 = KEYS[2]
print("8888888")
-- 获取ARGV[1],这里对应到应用端是一个List<Map>.
--  注意，这里接收到是的字符串，所以需要用cjson库解码成table类型
local receive_arg_json =  cjson.decode(ARGV[1])
--local temp =  cjson.encode({["times"] = 100, ["expire"] = 30000})
--local receive_arg_json =  cjson.decode(temp)

--返回的变量
local result = {}

--打印日志到reids
--注意，这里的打印日志级别，需要和redis.conf配置文件中的日志设置级别一致才行
redis.log(redis.LOG_DEBUG, key1)
redis.log(redis.LOG_DEBUG, key2)
redis.log(redis.LOG_WARNING, ARGV[1], #ARGV[1])

--获取ARGV内的参数并打印
local expire = receive_arg_json.expire  -- ???获取不到值.需要研究
local times = receive_arg_json.times
redis.log(redis.LOG_VERBOSE, tostring(times))
redis.log(redis.LOG_NOTICE, tostring(expire))

--往redis设置值
redis.call("set", key1, tostring(receive_arg_json))
redis.call("incr", key2)
redis.call("expire", key2, 30000) -- expire 因为获取值先写成变量

--用一个临时变量来存放json,json是要放入要返回的数组中的
local jsonRedisTemp={}
jsonRedisTemp[key1] = redis.call("get", key1)
jsonRedisTemp[key2] = redis.call("get", key2)
jsonRedisTemp["ttl"] = redis.call("ttl", key2)
redis.log(redis.LOG_DEBUG, cjson.encode(jsonRedisTemp))


result[1] = cjson.encode(receive_arg_json) --springboot redistemplate接收的是List,如果返回的数组内容是json对象,需要将json对象转成字符串,客户端才能接收
result[2] = ARGV[1] --将源参数内容一起返回
redis.log(redis.LOG_DEBUG, cjson.encode(result)) --打印返回的数组结果，这里返回需要以字符返回

return result