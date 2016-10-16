# User Service #

## Build & Run ##

```sh
$ ./sbt
> jetty:start
> browse
```

If `browse` doesn't launch your browser, manually open [http://localhost:8080/](http://localhost:8080/) in your browser.

## Other ##

### Set the appropriate environment variables

```sh
$ export DB_PG_URL="jdbc:postgresql://localhost:5432"
$ export DB_PG_USER=<username>
$ export DB_PG_PWD=<password>
```

### Set up initial db

```GET /setup```