# RSS feed reader

This is my pet-project to read an RSS feed of blogs I follow and save new posts into the database for later reading.

## Run locally

1. Run the Postgres database in a docker container:

```shell
docker run -p 5432:5432 --name rss-feed-postgres \
    -e POSTGRES_PASSWORD=test \
    -e POSTGRES_DB=test \
    -e POSTGRES_USER=test \
    -d postgres
```

2. Then, run the Gradle task to run Flyway migrations for local required for local development:

```shell
./gradlew flywayMigrateLocal
```

3. Start the application either by running the `RssFeeApplication` class with a `LOCAL` Spring profile within the IDE, 
or by running the Gradle command:

```shell
./gradlew bootRun --args='--spring.profiles.active=LOCAL'
```
