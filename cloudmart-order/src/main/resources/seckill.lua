local stock = redis.call('get', KEYS[1])
if stock and tonumber(stock) >= tonumber(ARGV[1]) then
    redis.call('decrby', KEYS[1], tonumber(ARGV[1]))
    return 1  -- 扣减成功
end
return 0  -- 库存不足