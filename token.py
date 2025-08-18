import http.client

conn = http.client.HTTPSConnection("dev-b41q6birjs2bsosm.us.auth0.com")

payload = "{\"client_id\":\"Pgy1KWCzwXriYBcyJQXq4jxBTAP4asY2\",\"client_secret\":\"O3IGePl1SXpb6nE29YWIfH8ThoDXnOVzUifSND4-1s7tanw96JbYi8-i06QrBlkz\",\"audience\":\"https://api.authz-service.com\", \"grant_type\":\"Credentials\"}"

headers = { 'content-type': "application/json" }

conn.request("POST", "/oauth/token", payload, headers)

res = conn.getresponse()
data = res.read()

print(data.decode("utf-8"))