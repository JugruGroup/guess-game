<a href="https://github.com/JugruGroup/guess-game/actions"><img alt="GitHub Actions status" src="https://github.com/JugruGroup/guess-game/workflows/Build/badge.svg"></a>
[![codecov](https://codecov.io/gh/JugruGroup/guess-game/branch/master/graph/badge.svg)](https://codecov.io/gh/JugruGroup/guess-game)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=JugruGroup_guess-game&metric=alert_status)](https://sonarcloud.io/dashboard?id=JugruGroup_guess-game)

# Find, See, Guess the Speakers

There are two parts of the program:
1. Search and view information about conferences, meetups, talks and speakers

1. **Guess the Speaker** game

Information and statistics of conferences, meetups, talks, speakers:

![Information](/documents/images/information.png)

*Statistics* page contains OLAP cubes for event types, speakers and companies:

![OLAP](/documents/images/olap-chart.png)

3D visualization of OLAP cube:

![3D](/documents/images/olap-3d.png)

You need to guess the speaker, talk, company or tag cloud in the game:

![Game](/documents/images/game.png)

## Running

### Online

https://jugspeakers.info

### Offline

1. Install [Java SE 25](https://www.oracle.com/java/technologies/downloads/) or higher (*JRE* or *JDK*)

1. Extract files from ZIP, for example:

    `unzip guess-game-<version>.zip`

1. Change directory:

    `cd guess-game-<version>`

1. Run:

    `runme.bat` (*Windows*)  
    `runme.sh` (*macOS* or *Linux*)

1. Access the running web application at:

    http://localhost:8080

## Download

1. Open [Releases](https://github.com/JugruGroup/guess-game/releases) page

1. Choose latest version

1. Download `guess-game-<version>.zip` file

## Compilation

1. Install [Java SE 25](https://www.oracle.com/java/technologies/downloads/) or higher (*JDK*)

1. Install [Apache Maven 3.9.0](https://maven.apache.org/download.cgi) or higher

1. From the command line with *Maven* (in the root directory):

    `mvn clean package -DskipTests`

1. Change directory:

    `cd guess-game-distrib/target`

1. Find distribution file:

    `guess-game-<version>.zip`
