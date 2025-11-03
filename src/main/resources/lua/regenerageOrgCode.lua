-- KEYS[1] = org:{orgId}
-- KEYS[2] = orgcode:{newCode}
-- ARGV[1] = orgId
-- ARGV[2] = newCode
-- ARGV[3] = nowMillis

local orgKey    = KEYS[1]
local newIdxKey = KEYS[2]

local orgId   = ARGV[1]
local newCode = ARGV[2]
local now     = ARGV[3]

-- 1) 현재 코드 조회
local oldCode = redis.call('HGET', orgKey, 'code')

-- 2) 새 코드 점유 확인
if redis.call('EXISTS', newIdxKey) == 1 then
  local mapped = redis.call('GET', newIdxKey)
  if mapped ~= orgId then
    return {err="CODE_IN_USE"}  -- 다른 조직이 점유
  end
end

-- 3) 새 인덱스 점유(자기 자신이 이미 점유 중이면 idempotent)
redis.call('SET', newIdxKey, orgId)

-- 4) 조직 코드 교체 + 타임스탬프
redis.call('HSET', orgKey, 'code', newCode, 'updatedAt', now)

-- 5) 이전 인덱스 제거 (동일 코드면 스킵)
if oldCode and oldCode ~= false and oldCode ~= newCode then
  redis.call('DEL', 'orgcode:' .. oldCode)
end

-- 결과: 이전 코드(혹은 nil) 반환
return oldCode