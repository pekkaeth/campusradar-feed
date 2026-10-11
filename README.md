# CampusRadar: Live Activity Feed

Module 1 of CampusRadar. Students post short-lived activities ("Cricket near the ground, need 3 more"), others tap JOIN, and the activity disappears by itself when its time is up.

## Features

- Post an activity with a title, category, place, spots needed and duration (30 minutes to 4 hours)
- JOIN and Leave, with a live "Need N more" counter
- The host can cancel their own activity
- The feed refreshes itself every 5 seconds without a page reload
- Category filter
- Automatic expiry
- A host can have at most 3 live activities, to stop spam

## How it works

- **Joining safely:** `ActivityDAO.join` runs in one transaction and locks the activity row with `SELECT ... FOR UPDATE`, so two students cannot take the last spot at the same time.
- **Expiry:** `ExpiryListener` is a `ServletContextListener` that starts a `ScheduledExecutorService` thread when the app starts. Every 60 seconds it marks finished activities as `EXPIRED`. The feed query also filters on `end_time`, so expired cards vanish immediately.
- **Live refresh:** the page calls `fetch('/feed/data')` every 5 seconds. `FeedDataServlet` forwards to `cards.jsp`, which returns an HTML fragment. JOIN, Leave and Cancel post to `/feed/...` and get JSON back.
- All times use Asia/Kolkata.

## Files

```
src/main/java/com/campusradar/feed/
  Activity.java, ActivityDAO.java, ExpiryListener.java
  FeedListServlet.java, FeedDataServlet.java
  ActivityCreateServlet.java, ActivityActionServlet.java
  DevLoginServlet.java        (temporary, for testing this module alone)
src/main/webapp/WEB-INF/views/feed/
  list.jsp, cards.jsp, create.jsp
```

`DBUtil.java` and `User.java` in the `common` package are shared with the other modules.

## Database

```sql
CREATE TABLE activities (
  id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(120) NOT NULL,
  description VARCHAR(300),
  category VARCHAR(40) NOT NULL,
  location_id INT NULL,
  place VARCHAR(100) NULL,
  slots_needed INT NOT NULL,
  created_by INT NOT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  status ENUM('ACTIVE','CANCELLED','EXPIRED') DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE activity_members (
  activity_id INT NOT NULL,
  user_id INT NOT NULL,
  joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (activity_id, user_id),
  FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

It also reads the shared `users` and `locations` tables.

## Run this module alone

```
$env:DB_URL  = "jdbc:mysql://HOST:PORT/campusradar?sslMode=REQUIRED"
$env:DB_USER = "your-db-user"
$env:DB_PASS = "your-db-password"
mvn package cargo:run
```

Open `http://localhost:8080/campusradar/feed/devlogin?id=1` (logs in as the user with that id) and you land on `/feed/list`. Use a normal window and a private window to test as two people. Database credentials are read from environment variables and must never be committed.

## Test checklist

- [ ] Create an activity; it appears within 5 seconds in another window
- [ ] JOIN from a second user; the counter changes in both windows
- [ ] Try the last spot from two users: only one succeeds
- [ ] Leave, then Cancel as the host
- [ ] A fourth live activity from one host is refused
- [ ] Set an activity's `end_time` to the past; the card disappears and the status becomes `EXPIRED`

## Integration

In the combined project, `DevLoginServlet` is removed. `AuthFilter` protects `/feed/*`, and the pages use the shared header and footer.

## Team and ownership

| Role | Person | Notes |
|---|---|---|
| Original development and integration | Himanshu (`pekkaeth`) | Repository owner |
| Module lead (study, testing, documentation, extension) | Harsh (`harsh-sankhla`) | Collaborator |

**Module lead responsibilities:** understand the join transaction, the expiry listener and the live refresh; test every flow in the checklist above; keep this README accurate; build one extension.

| Contribution | Status | Commits |
|---|---|---|
| Test checklist completed and results recorded | Planned | |
| Extension: "My activities" tab (joined and hosted activities) | Planned | |
