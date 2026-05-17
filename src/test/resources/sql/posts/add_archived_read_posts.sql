INSERT INTO posts (blog_id, post_name, post_url, is_read, is_ignored, ai_reason, date_added, date_read)
VALUES (1, 'Post 1', 'https://post1.com', TRUE, TRUE, 'Duplicate content', '2021-01-01 00:00:00', '2021-01-02 00:00:00');

INSERT INTO posts (blog_id, post_name, post_url, is_read, is_ignored, ai_reason, date_added, date_read)
VALUES (1, 'Post 2', 'https://post2.com', TRUE, TRUE, 'AI flagged for archive', '2021-01-03 00:00:00', '2021-01-04 00:00:00');

INSERT INTO posts (blog_id, post_name, post_url, is_read, is_ignored, ai_reason, date_added)
VALUES (1, 'Post 3', 'https://post3.com', FALSE, FALSE, NULL, '2021-01-05 00:00:00');
