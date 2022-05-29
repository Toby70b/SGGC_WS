# SteamGroupGameChecker Web Service - Tool to find out common steam games between users

⛔ PLEASE NOTE: This should be obvious but none of the code here should be considered "production ready" by a business standard.

:wave:: a working version of this can be found hosted on AWS [here](https://www.steamgroupgamechecker.co.uk/). 
For maximum cost-effectiveness (and because I'm cheap :smile:) the API is running on Spot EC2 instances, so it could 
go down if the AZ its running on is in particular demand

## AWS architecture diagram

![SGGC Diagram](https://user-images.githubusercontent.com/35812795/160235199-29a246e0-6da8-4371-b02b-027195adb380.png)

## Rationale for development

I belong to a group of 5-6 20 something friends whose primary hobby is gaming, as such we all have steam accounts with 
each of us owning at least 200 games. We all prefer to play games together, we don't mind replaying games, including 
older games, if it means we can play together. We would occasionally ask what games we have in common, however since 
steam only allows users to compare their lists with one other user at a time this task was very tedious. I wanted more 
excuses to practice on REST API's so I researched Steam's API and once I discovered it was feasible to make a tool 
that could compare multiple users lists for common games I decided to make this tool.

## REST API

The REST API is a spring boot project (created using spring initializr) run using a Tomcat server. The API is exposed 
(by default) on port 8080, it currently only has two endpoints. The primary endpoint is a POST endpoint which takes a 
json object consisting of an array of Strings which correspond to either Steam user ids or vanity URLs
(these are publicly available, either by using the Steam client or through the Steam API) it also takes a boolean 
parameter to flag whether only multiplayer games should be returned within the result set

An example of the body of the post request is:
```
{
	"steamIds" : [76561198045206229,SomeVanityURL,76561198171740181,AnotherVanityUrl],
	"multiplayerOnly": true
}
```

Upon success the API will return a json object consisting of game objects that contain the game's Application Id on 
the Steam db as well as the game's name. The returned games are multiplyer games the users entered in the input 
have in common.

An example of the output of ther API is:
```
[
    {
       "appid": 730,
       "name": "Counter-Strike: Global Offensive"
    },
    {
        "appid": 218230,
        "name": "PlanetSide 2"
    }
]
```

A swagger endpoint on the root of port 8080 if you want to view documentation in swagger format

## UI

The code for the UI is stored within another repo [here](https://github.com/Toby70b/SGGC_UI)

## Docker

The API has a docker image ready for containerization

tobypeel/steam_group_game_checker_api:prod

## Things to improve
* Improved handling of the various responses from Steam
* Move from REST to a Queue-based API
* Integration tests


## Thanks to
https://steamcommunity.com/dev (without it this wouldn't exist, and I wouldn't have had the practice) 
