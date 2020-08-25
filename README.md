# My Blog project
My Blog project is a blog demo based on Quarkus Framework, Flutter and Docker. Contains such features as:
- pages with demo posts
- comments and likes
- feature to add new post
- contact page
- about page

![](my-blog.gif)

## Used technologies
- Quarkus framework
- Panache with Hibernate-ORM
- Reactive Mailer
- Junit5
- AssertJ
- Mockito
- Swagger
- Docker
- PostgreSQL
- Flutter

## Mail
My Blog is capable of sending confirmation and contact e-mails. Currently email service is set to send e-mails from non-existing e-mail address. Please do not try to respond to `my-blog@mail.com`. If you have problems with receiving emails from `my-blog@mail.com`, check your spam folder.

## Swagger/OpenApi
Swagger-ui is available:

`http://localhost:8080/swagger-ui`

and OpenApi:

`http://localhost:8080/openapi`

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