CREATE TABLE IF NOT EXISTS blogs
(
    id       SERIAL PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    feed_url VARCHAR(255) NOT NULL
);

INSERT INTO blogs (name, feed_url)
VALUES ('Of Dollars And Data', 'https://ofdollarsanddata.com/feed/');

CREATE TABLE IF NOT EXISTS posts
(
    id        SERIAL PRIMARY KEY,
    blog_id   INTEGER      NOT NULL,
    post_name VARCHAR(255) NOT NULL,
    post_url  VARCHAR(255) NOT NULL,
    post_date DATE         NOT NULL,
    FOREIGN KEY (blog_id) REFERENCES blogs (id)
);
