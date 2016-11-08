# Replace {id} placeholder with a valid subscription ID (obtained after
# creating a new subscription).

## Create subscription
curl -X "POST" "http://localhost:8080/subscriptions" \
     -H "Content-Type: application/x-www-form-urlencoded; charset=utf-8" \
     --data-urlencode "name=foo" \
     --data-urlencode "messageTypes=type1,type2,type3"

## Get all subscriptions
curl -X "GET" "http://localhost:8080/subscriptions"

## Get subscription
curl -X "GET" "http://localhost:8080/subscriptions/{id}"

## Update subscription name
curl -X "PUT" "http://localhost:8080/subscriptions/{id}" \
     -H "Content-Type: application/x-www-form-urlencoded; charset=utf-8" \
     --data-urlencode "name=bar"

## Update subscription types
curl -X "PUT" "http://localhost:8080/subscriptions/{id}" \
     -H "Content-Type: application/x-www-form-urlencoded; charset=utf-8" \
     --data-urlencode "messageTypes=type9,type8,type7,type6"

## Update subscription name and types
curl -X "PUT" "http://localhost:8080/subscriptions/{id}" \
     -H "Content-Type: application/x-www-form-urlencoded; charset=utf-8" \
     --data-urlencode "name=baz" \
     --data-urlencode "messageTypes=type99,type88"

## Create message
curl -X "POST" "http://localhost:8080/messages" \
     -H "Content-Type: application/x-www-form-urlencoded; charset=utf-8" \
     --data-urlencode "type=type1" \
     --data-urlencode "content=hello world"

## Get all messages
curl -X "GET" "http://localhost:8080/messages"
