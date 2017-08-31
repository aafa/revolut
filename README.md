### Money transfers

#### Task
Implement money transfers between accounts. 

Using Tech stack: `Finch` + `Circe`

#### API
- GET /accounts - returns back list of all accounts
- GET /account/$id - returns back account for given id or 404
- POST /account with NewAccountPayload - adds new account from payload details 
- POST /transfer with TransferPayload - creates money transfer from payload details, or 400 if not enough funds 

#### How to run

- `sbt ~reStart` for interactive development: server recompiled\restarted as you change something in a code 
- `sbt test` to run basic test scenarios

#### Load testing 
`wrk` has been used to do simple load testing for typical scenarios 
Tests were run after initial short 10s warm-up.

Here 3 consecutive scenarios has been tested
- add new accounts
- query particular account
- make money transfers between accounts   

```
✗ wrk/test.sh 30s
Running 30s test @ http://localhost:8080
  4 threads and 8 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   740.65us    3.68ms  95.84ms   96.45%
    Req/Sec    10.41k     2.92k   13.25k    83.50%
  Latency Distribution
     50%  158.00us
     75%  191.00us
     90%  313.00us
     99%   14.94ms
  1243447 requests in 30.02s, 214.51MB read
Requests/sec:  41416.76
Transfer/sec:      7.14MB
Running 30s test @ http://localhost:8080
  4 threads and 8 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   188.46us  569.46us  28.94ms   98.91%
    Req/Sec    13.57k     1.35k   25.89k    91.51%
  Latency Distribution
     50%  137.00us
     75%  145.00us
     90%  163.00us
     99%    1.05ms
  1621627 requests in 30.10s, 278.37MB read
Requests/sec:  53875.76
Transfer/sec:      9.25MB
Running 30s test @ http://localhost:8080
  4 threads and 8 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   298.73us    0.94ms  18.68ms   97.86%
    Req/Sec    11.00k     1.01k   12.29k    84.58%
  Latency Distribution
     50%  168.00us
     75%  182.00us
     90%  207.00us
     99%    6.02ms
  1313713 requests in 30.01s, 308.34MB read
Requests/sec:  43771.84
Transfer/sec:     10.27MB
``` 


See full details in a `/wrk` directory

#### 