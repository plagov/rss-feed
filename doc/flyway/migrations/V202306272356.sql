CREATE TABLE IF NOT EXISTS blogs
(
    id       SERIAL PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    feed_url VARCHAR(255) NOT NULL
);

INSERT INTO blogs (name, feed_url) VALUES ('Of Dollars And Data', 'https://ofdollarsanddata.com/feed/');

CREATE TABLE IF NOT EXISTS posts
(
    blog_name VARCHAR(255),
    post_name VARCHAR(255),
    post_date TIMESTAMP,
)
