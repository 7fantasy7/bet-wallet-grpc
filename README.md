Pre-requirements:
docker should be installed

To run server, 
docker-compose up --build
from the root project directory

To run client,
TODO

Highest rate is about 2.5k reqs/sec
on --users=100 --concurrent_threads_per_user=10 --rounds_per_thread=5.
Separate channel/service per thread don't increase performance noticeably.

To measure this number I created a PerformanceMeasurementInterceptor
counting gRPC requests at every second.

Important choices in solution:
TODO