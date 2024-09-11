인프런 JSCODE 의 Redis 입문/실전으로 학습한 후 운영환경을 위한 Redis 설정 내용들을 정리

1. Redis 의 Persistence 전략
  - RDB
    - Redis Database
    - 스냅샷 형태로 덤프한다.
    - 재시작 시 해당 데이터를 그대로 옮기므로 빠르게 복구할 수 있다.
    - RDB 를 사용하지 않으려면 SAVE 를 모두 주석처리하면 된다.
    - 설정파일에서 RDB 저장 사용 시 BGSAVE 방식을 사용한다.
      - SAVE : 순간적으로 redis 의 동작을 정지시키고 snapshot 을 디스크에 저장하는 blocking 방식
      - BGSAVE : 별개의 프로세스를 생성하여 수행 당시의 snaphot 을 디스크에 저장하는 non-blocking 방식. 메모리 사용량이 두 배 가량 증가.
  - AOF
    - Append Only File
    - 서버에서 발생한 Command 명령을, 명령 형식으로 기록한다.
    - 서버가 재시작되면 이 파일을 읽어들여 동일한 명령들을 재실행함으로 이전 상태를 복원할 수 있다.
    - Rewriting : 주기적으로 AOF 파일을 압축하여 가장 적은 수의 명령으로 현재 상태를 기록하도록 함
    - AOF 를 사용하지 않으려면 appendonly 옵션 값으로 no 를 지정하면 된다.
  - RDB + AOF
    - RDB 로 성능을 높이고, AOF 로 무결성을 지킨다.
    - Redis 의 로드맵으로서 추후에 AOF 와 RDB 방식을 합치는 것을 계획 중에 있음

2. RDB Persistence 옵션
---
save 900 1     # Save the DB snapshot if 1 or more keys change in 900 seconds (15 minutes)
save 300 10    # Save the DB snapshot if 10 or more keys change in 300 seconds (5 minutes)
save 60 10000  # Save the DB snapshot if 10,000 or more keys change in 60 seconds (1 minute)
stop-writes-on-bgsave-error yes  # Stop writes if the last BGSAVE failed (default is yes)
rdbcompression yes  # Compress the RDB file to save space (default is yes)
rdbchecksum yes  # Enable RDB file checksum to detect data corruption (default is yes)
dbfilename dump.rdb  # Name of the RDB file (default is dump.rdb)
dir ./  # Directory where the RDB file will be saved
---

3. AOP 옵션
---
appendonly yes  # Enable AOF persistence (default is no)
appendfilename "appendonly.aof"  # Name of the AOF file (default is appendonly.aof)
appendfsync everysec  # everysec : Sync AOF to disk every second (good balance of safety and performance)
                      # always : Sync AOF to disk after every write (highest data safety, lowest performance)
                      # no : Let the operating system handle syncing (highest performance, potential data loss)
no-appendfsync-on-rewrite no  # Do not delay AOF syncs during rewrite (default is no)
auto-aof-rewrite-percentage 100  # When the current AOF file size is 100% larger than the size after the last AOF rewrite, a new rewrite is triggered.
                                 # This setting controls the threshold for triggering an automatic rewrite of the AOF file.
                                 # For example, if the AOF file was 50MB after the last rewrite, a new rewrite will occur when the file reaches 100MB.
                                 # A higher percentage means less frequent rewrites but larger AOF files, whereas a lower percentage can lead to more frequent rewrites and better space management.
auto-aof-rewrite-min-size 64mb  # Sets the minimum AOF file size required to trigger a rewrite. The default is 64MB.
                                # This option prevents the system from performing unnecessary rewrites on small AOF files, which would be inefficient.
                                # For example, if your AOF file is smaller than 64MB, even if it has doubled in size since the last rewrite, a new rewrite will not be triggered.
                                # This helps to avoid frequent rewrites, which could negatively impact performance, especially in environments with a small dataset.
aof-load-truncated yes  # Instructs Redis to load the AOF file even if it is found to be truncated.
                        # This is useful in scenarios where Redis was unexpectedly shut down and the AOF file may be incomplete.
                        # The default setting (yes) allows Redis to start up and recover as much data as possible, instead of failing to start.
                        # However, this may result in some data loss if the file was truncated, so careful consideration is needed.
                        # Setting this to "no" will prevent Redis from loading a potentially corrupt AOF file, ensuring stricter data integrity.
aof-use-rdb-preamble yes  # Instructs Redis to start new AOF files with an RDB format preamble.
                          # This improves the efficiency of AOF rewrites by allowing Redis to store the initial dataset using the more compact RDB format, followed by subsequent commands in AOF format.
                          # The default setting (yes) helps reduce AOF file size and rewrite time, especially for large datasets.
                          # This option is particularly useful in environments where frequent AOF rewrites occur or where large amounts of data need to be compacted.
                          # However, if this setting is disabled (no), the AOF file will consist solely of AOF commands, which may be larger and take longer to rewrite.
---

4. Redis Persistence 전략 의사결정
  - 캐시로만 사용하는 등 영속성이 필요 없다면 백업을 하지 않는다.
  - 백업이 필요하지만 어느 정도의 데이터 손실이 발생해도 괜찮다면 RDB 를 단독 사용한다.
  - 장애 직전까지 모든 데이터를 보장해야 한다면 AOF 와 RDB 를 혼용하여 사용한다.

5. Redis Maxmemory
  - Redis 설정으로 최대 메모리 크기 제한 및 메모리 초과 시의 정책에 대한 내용을 결정할 수 있다.
  - 캐시 목적을 위해서라면 allkeys-lru 를 사용할 수 있다.
    - 가장 오래동안 사용되지 않은 데이터를 우선적으로 삭제하여 캐시 효율 극대화할 수 있다.
    - 다양한 워크로드 및 일관되지 않은 데이터 접근 패턴에 대해서 적응(적응성)하여 캐시 히트율을 높일 수 있다.

---
maxmemory 256mb  # Set the maximum memory usage to 256MB. Default is No Limit
maxmemory-policy noeviction  # Do not evict any keys, just return an error on writes(default)
                             # allkeys-lru : Remove the least recently used key among all keys
                             # volatile-lru : Remove the least recently used key among keys with an expiration set
                             # allkeys-random : Remove a random key among all keys
                             # volatile-random : Remove a random key among keys with an expiration set
                             # volatile-ttl : Remove the key with the nearest expiration time (TTL)
                             # volatile-lfu : Remove the least frequently used key among keys with an expiration set
                             # allkeys-lfu : Remove the least frequently used key among all keys
---

6. LazyFree
  - 메모리 초과 시 연속 범위 키에 대한 삭제 작업 등을 수행하면 성능 지연이 발생한다.
  - LazyGree 스레드를 이용하여 백그라운드 작업을 수행하면 이러한 성능 지연 문제를 해결할 수 있다.
  - 단점
    - 지연된 메모리 해제
    - 백그라운드 스레드의 추가적인 리소스 소비
    - 비동기 메모리 해제에 의한 일관성 문제
      - 일반적으로 큰 문제는 아니지만, 메모리 일관성이 매우 중요한 애플리케이션이라면 문제가 될 수 있다.
---
lazyfree-lazy-eviction no  # If set to "yes", eviction of keys due to maxmemory limit will be done asynchronously
lazyfree-lazy-expire no  # If set to "yes", expiration of keys will be done asynchronously
lazyfree-lazy-server-del no  # If set to "yes", all explicit DEL operations will be done asynchronously
lazyfree-lazy-user-del no  # If set to "yes", all deletions resulting from user commands like UNLINK, LREM, etc. will be done asynchronously
lazyfree-lazy-user-flush no  # If set to "yes", FLUSHDB and FLUSHALL operations will be done asynchronously
---
