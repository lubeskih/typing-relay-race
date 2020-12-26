# TYPING RELAY RACE

This is the official repository of the **Typing Relay Race** game from **Group 1**, Distributed Systems, **Project 1**, WS20/21. :)

**Table of Contents**

- [Messaging Protocol](#Messaging-Protocol)
    - [Status Codes](#Status-Codes)
    - [Messages](#Messages)
- [Payloads](#Payloads)
    - [InfoPayload](#InfoPayload)
    - [RegisterPayload](#RegisterPayload)
    - [LoginPayload](#LoginPayload)
    - [ScoreboardPayload](#ScoreboardPayload)
    - [CreateTeamPayload](#CreateTeamPayload)
    - [JoinTeamPayload](#JoinTeamPayload)
    - [ListOnlineTeams](#ListOnlineTeams)
- [User Commands](#User-Commands)

## Messaging Protocol

### Status Codes

```
CODE    MESSAGE               
====    =====================
100     OK                    
110     Info                  

200     Bad Request          
210     Internal Server Error
220     Forbidden            
230     Conflict    
240     Not Found         
```

### Messages

```
{
    isResponse<boolean>: false,
    isError<boolean>: false,
    reply<null | int>
    payload<<payload_name>Payload>: {
        ...
    }
}
```

## Payloads

### InfoPayload (Server Providing Information)

Sent by: **Server**


```
{
    message<String>: "pong!"
}
```

### RegisterPayload

Sent by: **Client**

```
{
    username<String>: "john",
    password<String>: "doe",
    repeat_password<String>: "doe"
}
```

### LoginPayload

Sent by: **Client**

```
{
    username<String>: "john",
    password<String>: "doe"
}
```

### ScoreboardPayload

Sent by: **Server**, **Client**

```
{
    scoreboard<Team[]>: [
        {Team Nr. 1},
        {Team Nr. 2},
        ...,
        {Team Nr. 5}
    ]
}
```

### CreateTeamPayload


```
{
    teamname<String>: "wolfs",
    public<boolean>: true,     # <- if false, server will reply with a generated password, which can be used to join the team
}
```

### JoinTeamPayload


```
{
    teamname<String>: "wolfs",
    password<String>: null,
}
```

### ListOnlineTeams


(Shows only **public** teams that are waiting for the **second** player)

```
{
    online<Team[]>: [
        {Team Nr. 1},
        {Team Nr. 2},
        ...,
    ]
}
```

EXAMPLE MESSAGE
---------------

    {
        isResponse: false,
        reply: null,
        payload: {                  # <- payload is instanceof LoginPayload
            username: "tin",
            password: "QWERTY1234",
        }
        isError: false,
    }

    ...

    {
        isResponse: true,
        reply: 100,         # 100 - OK
        payload: {
            sessionToken: <session_token>
        },
        isError: false,
    }

    ...

    {
        isResponse: false,
        reply: null,
        payload: {},        # payload is not null, but instanceof ScoreboardPayload
        isError: false,
    }

    ...

    {
        isResponse: true,
        reply: 110,          # 110 - Info
        payload: {
            scoreboard: { ... }
        },
        isError: false,
    }

## User Commands


```
COMMAND                                             ACTION
===========================================================================
:help Show Help                                     Show help
:login <username> <password>                        Login
:register <username> <password> <repeat_password>   Register an account
:scoreboard                                         View Scoreboard
:create <team_name>                                 Create a New Team
:join <team_name>                                   Join an Existing Team
:teams                                              List All Teams
:ping                                               Connection Healthcheck
:clear                                              Clears the Screen
===========================================================================
```
