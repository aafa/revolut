wrk.method = "POST"
wrk.body   = "{\"from\":\"0\",\"to\":\"1\",\"amount\":1}"
wrk.path = "/transfer"
wrk.headers["Content-Type"] = "application/json"
