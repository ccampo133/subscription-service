# subscription-service

A simple message subscription microservice, used as an example in decent REST API development techniques.

This program makes heavy use of 
[Google Guava's immutable collections](https://github.com/google/guava/wiki/ImmutableCollectionsExplained). Primarily
this is done for thread safety, but it also just makes rationalizing about and working with the code easier. In general,
the code is written with immutability as a key objective. All POJOs are immutable, all parameters, fields, and local
variables are final, and all collections are immutable (minus a single `ConcurrentHashMap`).

Additionally, the use of `null` is explicitly avoided and discouraged 
[for various reasons](http://winterbe.com/posts/2015/03/15/avoid-null-checks-in-java/). This code makes heavy use of the
[JetBrains annotations](https://www.jetbrains.com/help/idea/2016.2/nullable-and-notnull-annotations.html) to mark when
parameters and methods are explicitly not-null, since there is no way to opt-out of null by default in Java.

Explicitly thrown unchecked exceptions are used in favor of checked exceptions to handle standard error cases.

The `Subscription` objects and their corresponding data (messages, etc) are stored in memory for simplicity.

# To run

Linux/OS X: 

    ./gradlew bootRun
     
Windows:
 
    gradlew.bat bootRun

Or alternatively, build the jar file and run that from the command line:

Linux/OS X:

    ./gradlew build
    java -jar build/libs/subscription-service-<version>.jar

Once started, the application will run on `http://localhost:8080` by default.

A bunch of sample `cURL` commands have been included in `example-curls.sh`.

# API Specifications

The serialization format for all responses is JSON.

## Create a subscription

`POST /subscriptions`

#### Content type: 
* x-www-form-urlencoded

#### Required body parameters:
* `name`: the name of the subscription (string)
* `messageTypes`: the types of messages supported (string list, comma separated. Example: `type1,type2,...`)
    
#### Response:
* `201 CREATED` - successfully created subscription
    
A subscription ID will be auto-generated as a UUID and the path to the newly created resource will be returned in the
`Location` header of the response.

#### Example:
    
    POST /subscriptions HTTP/1.1
    Content-Type: application/x-www-form-urlencoded; charset=utf-8
    Host: localhost:8080
    
    name=foo&messageTypes=type1%2Ctype2%2Ctype3
    
    
    HTTP/1.1 201 
    Location: http://localhost:8080/subscriptions/6e2ef583-fa9d-4ccc-b10e-f099211ec6d1
    Content-Type: application/json;charset=UTF-8

    {
      "id": "6e2ef583-fa9d-4ccc-b10e-f099211ec6d1",
      "name": "foo",
      "messageTypes": [
        "type1",
        "type2",
        "type3"
      ],
      "messages": [],
      "messageCountsByType": {
        "type3": 0,
        "type2": 0,
        "type1": 0
      }
    }
    
    
## Get a subscription

`GET /subscriptions/{id}`

#### Required path parameters:
* `id`: the UUID of the subscription
    
#### Response:
* `200 OK` - successfully retrieved subscription
* `404 NOT FOUND` - no subscription with that ID exists

#### Example:
    
    GET /subscriptions/6e2ef583-fa9d-4ccc-b10e-f099211ec6d1 HTTP/1.1
    Host: localhost:8080
    
    
    HTTP/1.1 200 
    Content-Type: application/json;charset=UTF-8
    
    {
      "id": "6e2ef583-fa9d-4ccc-b10e-f099211ec6d1",
      "name": "foo",
      "messageTypes": [
        "type1",
        "type2",
        "type3"
      ],
      "messages": [],
      "messageCountsByType": {
        "type3": 0,
        "type2": 0,
        "type1": 0
      }
    }


## Update a subscription

`PUT /subscriptions`

#### Content type: 
* x-www-form-urlencoded

#### Body parameters:
* `name`: the new name of the subscription (string, optional)
* `messageTypes`: the new types of messages supported (string list, comma separated. Example: `type1,type2,...`, optional)
    
#### Response:
* `200 OK` - successfully updated the subscription
* `204 NO CONTENT` - nothing was done to the existing subscription (identity operation)
* `404 NOT FOUND` - the subscription does not exist
    
#### Example:
        
    PUT /subscriptions/6e2ef583-fa9d-4ccc-b10e-f099211ec6d1 HTTP/1.1
    Content-Type: application/x-www-form-urlencoded; charset=utf-8
    Host: localhost:8080
    
    name=baz&messageTypes=type99%2Ctype88
    
    
    HTTP/1.1 200 
    Content-Type: application/json;charset=UTF-8

    {
      "id": "6e2ef583-fa9d-4ccc-b10e-f099211ec6d1",
      "name": "baz",
      "messageTypes": [
        "type99",
        "type88"
      ],
      "messages": [],
      "messageCountsByType": {
        "type99": 0,
        "type88": 0
      }
    }


## Create a message

`POST /messages`

#### Content type: 
* x-www-form-urlencoded

#### Required body parameters:
* `type`: the type of message (string)
* `content`: the message content (string)
    
#### Response:
* `201 CREATED` - successfully created the message

The message will be "sent" to every subscription that supports its "type".
Query the individual subscriptions themselves to verify that the message
has made it there properly.

#### Example:

    POST /messages HTTP/1.1
    Content-Type: application/x-www-form-urlencoded; charset=utf-8
    Host: localhost:8080
    
    type=type1&content=hello+world
    
    
    HTTP/1.1 201 
    Content-Type: application/json;charset=UTF-8
        
    {
      "id": "5d981900-175d-4df0-a151-4f97d641ef05",
      "type": "type1",
      "content": "hello world"
    }
