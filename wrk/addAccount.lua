wrk.method = "POST"
wrk.body   = "{\"user\":{\"name\":\"test acc guy\"},\"amount\":1337}"
wrk.path = "/account"
wrk.headers["Content-Type"] = "application/json"