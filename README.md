# My Blog project


## Docker
To run this project with Docker just replace `SERVER` and `FLUTTER_APP` enviroment variables in docker-compose.yaml with your own server ip. Localhost is a default and allows to run My Blog locally.

```
DEFAULT:

x-variables: &server
    SERVER: localhost:8080
    FLUTTER_APP: localhost:8090

EXAMPLE:

x-variables: &server
    SERVER: 192.168.0.12:8080
    FLUTTER_APP: 192.168.0.12:8090

```

To start My Blog run from `/docker` folder command:
```
docker-compose up -d
```