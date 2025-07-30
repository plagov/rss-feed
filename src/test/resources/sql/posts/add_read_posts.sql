INSERT INTO posts (blog_id, post_name, post_url, is_read, date_added, date_read)
VALUES (1, 'Post 1', 'https://example.com/first-read-post', TRUE, NOW(), NOW() - make_interval(days => 40));

INSERT INTO posts (blog_id, post_name, post_url, is_read, date_added)
VALUES (1, 'Post 2', 'https://example.com/unread-post', FALSE, NOW());

INSERT INTO posts (blog_id, post_name, post_url, is_read, date_added, date_read)
VALUES (1, 'Post 3', 'https://example.com/second-read-post', TRUE, NOW(), NOW() - make_interval(days => 10));
