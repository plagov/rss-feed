# RSS feed reader

This is my pet-project to read an RSS feed of blogs I follow and save new posts into the database for later reading.

## Run locally
Run the database locally:
```shell
docker run --name rss-feed-postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:17
```
