# gRPC Wallet Server & Client
Project consists of a wallet server and a wallet client. The wallet server keeps track
of a users monetary balance in the system. The client emulates users depositing and
withdrawing funds.

## Pre-requirements:
* [Docker][d] should be installed
* Network should be created: `docker network create -d bridge walletnet`

## Used technologies
* Java
* gRPC (via [grpc-spring-boot-starter][g])
* [PostgreSQL][p]
* Gradle
* JUnit
* SLF4J (via [Lombok][l])
* [Docker][d] & docker-compose
* Hibernate (via Spring data)
* Spring Boot
* [Testcontainers][t] (to run PostgreSQL DB in integration test)

## Run
### Server
``docker-compose up --build``
from the root project directory.

This will run both [PostgreSQL][p] database and wallet server

### Client
To run client, you need to join existing network and pass `server_address`:
```docker build -t wallet-client . &&
docker run --network=walletnet --server_address=walletserver wallet-client --users=10 --concurrent_threads_per_user=10 --rounds_per_thread=5
```

#### Arguments
| Name                        | Description                |
|:----------------------------|:---------------------------|
|users                        |Concurrent users            |  
|concurrent_threads_per_user  |Threads per each users      |
|rounds_per_thread            |Rounds, each thread executes|

## Performance
Highest rate is about **700 reqs/sec**
on ```--users=100 --concurrent_threads_per_user=10 --rounds_per_thread=5```
with 2 CPU and 2GB RAM docker env for both client & server.

Tested separate channel/service per thread, and it didn't increase performance noticeably.

To measure this number a [PerformanceMeasurementInterceptor](/server/src/main/java/by/botyanov/wallet/server/grpc/interceptor/PerformanceMeasurementInterceptor.java)
counting gRPC requests at every second is used.

## Important choices in solution:
* gRPC schema & model(created on `build` action) is stored in separate module.
* Wallet is created on first deposit using custom insert statement, which helped to avoid locking:
```java
@SQLInsert(sql = "INSERT INTO wallet(amount, currency, user_id) VALUES (?, ?, ?) " +
        "ON CONFLICT (user_id, currency) DO UPDATE SET amount = wallet.amount + EXCLUDED.amount")
```
* Locking is used on deposit and withdraw operations by annotating repository method with
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```
* Service logic is separated from gRPC layer
* Clients are started in parallel using `ExecutorService#invokeAll` of size `usersCount * concurrentThreads`

[d]: https://www.docker.com/
[p]: https://www.postgresql.org
[g]: https://github.com/LogNet/grpc-spring-boot-starter
[l]: https://projectlombok.org/
[t]: https://www.testcontainers.org/
