---
title: "Driver Cache"
---

## Concept
The DriverCache interface is a very simply lazy caching mechanism to allow for objects to be queried with a predefined timeout. 

The cache is updated in the following conditions:
a) Data is requested via get and it does not exist in cache. It is then queried from the database.
b) Data is requested via get and the timeout has expired (default 15 minutes). This is treated the same as a cache miss and data is thus queried from the Database
c) A set is called, then the same data is inserted to the cache

## Example
{{< highlight java >}}
@Autowired
CasquatchDao casquatchDao;

DatabaseCache<MyObj> myObjCache = casquatchDao.getCache(MyObj.cache)

MyObj obj = myObjCache.get('key1.key2');
{{< /highlight >}}

