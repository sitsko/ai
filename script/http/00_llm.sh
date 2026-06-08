### OUTPUT the exactly same name
http GET http://localhost:8080/api/vessels/hapag-lloyd/heavy


### incomplete
http GET http://localhost:8080/api/vessels/hapag/heavy


### incorrect name, typo
http GET http://localhost:8080/api/vessels/hopag/heavy

### INPUT, Bad word
http GET http://localhost:8080/api/vessels/bad/heavy

### OUTPUT GUARD RAIL, Dummy not returned, reprompt
http GET http://localhost:8080/api/vessels/evergreen/heavy


### Check via Internet, structured response
http GET http://localhost:8080/api/vessels/hapag/count


### Check via Internet, structured response
http GET http://localhost:8080/api/vessels/forecast?years=1