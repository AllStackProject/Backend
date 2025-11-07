local orgKey    = KEYS[1]
local newIdxKey = KEYS[2]

local orgId   = ARGV[1]
local newCode = ARGV[2]
local now     = ARGV[3]

local oldCode = redis.call('HGET', orgKey, 'code')

if redis.call('EXISTS', newIdxKey) == 1 then
  local mapped = redis.call('GET', newIdxKey)
  if mapped ~= orgId then
    return {err="CODE_IN_USE"}  -- 다른 조직이 점유
  end
end

redis.call('SET', newIdxKey, orgId)

redis.call('HSET', orgKey, 'code', newCode, 'updatedAt', now)

if oldCode and oldCode ~= false and oldCode ~= newCode then
  redis.call('DEL', 'orgcode:' .. oldCode)
end

return oldCode