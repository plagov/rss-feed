# Product Requirements Document (PRD): Slow Feed (Serial Fetching)

## 1. Problem Statement
RSS users often experience "inbox fatigue" when subscribing to high-frequency blogs. Traditional RSS readers fetch all available new posts, leading to an overwhelming number of unread items. This creates pressure on the user and often results in them abandoning the feed or the reader altogether.

## 2. Objective
Implement a "Slow Feed" mechanism that limits the intake of new content to a manageable pace: one post at a time. This ensures the user's reading inbox remains small and focused, prioritizing attention over absolute currency.

## 3. User Stories
- As a subscriber, I want the system to stop fetching new posts for a blog if I already have an unread post from that blog, so I don't feel overwhelmed.
- As a subscriber, I want to receive the next chronological post from a blog only after I have finished reading the current one.
- As a subscriber, I want the system to gracefully handle situations where the next chronological post is no longer available in the RSS feed (e.g., due to feed rotation).
- As a subscriber, I want the system to automatically fetch the next post as soon as I finish reading the current one, so I don't have to wait for a background process to run.

## 4. Proposed Solution: Serial Fetching
The fetching logic will be modified to follow these rules for each subscribed blog:

### 4.1. "One-In-The-Chamber" Rule
Before attempting to fetch new posts for a blog, the system must check if there is at least one `unread` post for that blog in the database.
- If an unread post exists: **Skip** fetching for this blog.
- If no unread posts exist: **Proceed** to fetch.

### 4.2. "Next-In-Line" Fetching Logic
When fetching is allowed (no unread posts), the system should:
1.  Identify the `latestSavedPost` for the blog (even if it's already marked as read).
2.  Retrieve all entries from the RSS feed.
3.  If `latestSavedPost` is still present in the feed:
    - Find the post that immediately follows it in chronological order (the oldest post that is newer than `latestSavedPost`).
    - Save **only** this single post.
4.  If `latestSavedPost` is **NOT** present in the feed (fallback):
    - Save the **oldest** post currently available in the RSS feed.
    - *Rationale:* This ensures the user doesn't miss too much content while still maintaining the serial nature.

### 4.3. Dismissal Flow (Automatic Trigger)
The act of marking a post as `read` (or deleting it) is the primary trigger that "unlocks" and **immediately initiates** the next fetch cycle for that specific blog.

1.  When a user marks a post as read:
    - The system updates the post status in the database.
    - The system **asynchronously** triggers the fetching logic (Section 4.2) for the blog associated with that post.
2.  *Rationale:* This creates a seamless "just-in-time" delivery system where the next content piece is prepared the moment the user is ready, eliminating the need for periodic polling or external cron jobs.

## 5. Non-Functional Requirements
- **Performance:** The fetcher should remain efficient. Checking for unread posts should be a fast database operation.
- **Reliability:** Errors in parsing a single feed should not block other feeds.
- **Asynchronicity:** The fetch operation triggered by marking a post as read MUST be asynchronous to ensure the user's "mark as read" action is not delayed by network latency from RSS feed providers.

## 6. Success Metrics
- Reduced number of unread posts per user.
- Increased "read completion rate" for high-frequency blogs.
- Positive user feedback regarding reduced "inbox pressure".
