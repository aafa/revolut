#!/usr/bin/env bash

wrk -t4 -c8 -d$1 -s wrk/addAccount.lua --latency http://localhost:8080
wrk -t4 -c8 -d$1 -s wrk/accounts.lua --latency http://localhost:8080
wrk -t4 -c8 -d$1 -s wrk/transfer.lua --latency http://localhost:8080
