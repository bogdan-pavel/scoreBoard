# scoreBoard

The REST server app is running on port 8081

The app was developed using java version 11

#### Login REST call
GET -> http://localhost:8081/4711/login --> UICSNDK

#### Add user score REST call
POST -> http://localhost:8081/2/score?sessionkey=UICSNDK
(with the post body:1500)

#### Get high score per level REST call
GET -> http://localhost:8081/2/highscorelist - > 4711=1500,131=1220

### Build application
$ mvn clean install

### Start http Server
$ java -jar scoreBoard-1.0.jar

#### TODO

- add more JUnits
- define constants as system variables
- add configured logs for debugging