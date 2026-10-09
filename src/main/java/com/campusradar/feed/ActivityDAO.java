package com.campusradar.feed;

import com.campusradar.common.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ActivityDAO {

    // all times use Indian time, so the database and Java always agree
    public static LocalDateTime nowIst() {
        return LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
    }

    private Timestamp nowTs() {
        return Timestamp.valueOf(nowIst());
    }

    public List<Activity> listActive(int userId, String category) throws Exception {
        boolean filter = category != null && !category.isEmpty() && !category.equals("All");
        String sql = "SELECT a.id, a.title, a.description, a.category, a.slots_needed, a.end_time, a.created_by, "
                + "u.name AS host, COALESCE(l.name, a.place, 'On campus') AS place, "
                + "(SELECT COUNT(*) FROM activity_members m WHERE m.activity_id = a.id) AS joined, "
                + "(SELECT COUNT(*) FROM activity_members m2 WHERE m2.activity_id = a.id AND m2.user_id = ?) AS mine "
                + "FROM activities a JOIN users u ON u.id = a.created_by "
                + "LEFT JOIN locations l ON l.id = a.location_id "
                + "WHERE a.status = 'ACTIVE' AND a.end_time > ? "
                + (filter ? "AND a.category = ? " : "")
                + "ORDER BY a.end_time ASC LIMIT 100";
        List<Activity> list = new ArrayList<>();
        LocalDateTime now = nowIst();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setTimestamp(2, Timestamp.valueOf(now));
            if (filter) ps.setString(3, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Activity a = new Activity();
                    a.setId(rs.getInt("id"));
                    a.setTitle(rs.getString("title"));
                    a.setDescription(rs.getString("description"));
                    a.setCategory(rs.getString("category"));
                    a.setSlotsNeeded(rs.getInt("slots_needed"));
                    a.setHostId(rs.getInt("created_by"));
                    a.setHostName(rs.getString("host"));
                    a.setPlace(rs.getString("place"));
                    a.setJoinedCount(rs.getInt("joined"));
                    a.setJoinedByMe(rs.getInt("mine") > 0);
                    long secs = Duration.between(now, rs.getTimestamp("end_time").toLocalDateTime()).getSeconds();
                    a.setMinutesLeft(Math.max(0, (secs + 59) / 60));
                    list.add(a);
                }
            }
        }
        return list;
    }

    // list of {id, name} for the place dropdown
    public List<String[]> listLocations() throws Exception {
        List<String[]> list = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT id, name FROM locations ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[]{ String.valueOf(rs.getInt(1)), rs.getString(2) });
            }
        }
        return list;
    }

    public void create(String title, String description, String category, Integer locationId,
                       String place, int slots, int minutes, int userId) throws Exception {
        String sql = "INSERT INTO activities (title, description, category, location_id, place, "
                + "slots_needed, created_by, start_time, end_time) VALUES (?,?,?,?,?,?,?,?,?)";
        LocalDateTime now = nowIst();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, category);
            if (locationId == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, locationId);
            if (place == null || place.isEmpty()) ps.setNull(5, Types.VARCHAR); else ps.setString(5, place);
            ps.setInt(6, slots);
            ps.setInt(7, userId);
            ps.setTimestamp(8, Timestamp.valueOf(now));
            ps.setTimestamp(9, Timestamp.valueOf(now.plusMinutes(minutes)));
            ps.executeUpdate();
        }
    }

    // Returns null when the join worked, otherwise a message to show.
    // FOR UPDATE locks the activity row, so two students cannot take the last spot together.
    public String join(int activityId, int userId) throws Exception {
        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);
            try {
                int slots;
                int hostId;
                String status;
                Timestamp end;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT slots_needed, created_by, status, end_time FROM activities WHERE id = ? FOR UPDATE")) {
                    ps.setInt(1, activityId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) { con.rollback(); return "Activity not found."; }
                        slots = rs.getInt(1);
                        hostId = rs.getInt(2);
                        status = rs.getString(3);
                        end = rs.getTimestamp(4);
                    }
                }
                if (!"ACTIVE".equals(status) || !end.toLocalDateTime().isAfter(nowIst())) {
                    con.rollback();
                    return "This activity has ended.";
                }
                if (hostId == userId) { con.rollback(); return "You are the host of this activity."; }

                int joined;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COUNT(*) FROM activity_members WHERE activity_id = ?")) {
                    ps.setInt(1, activityId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        joined = rs.getInt(1);
                    }
                }
                if (joined >= slots) { con.rollback(); return "Sorry, all the spots are taken."; }

                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT IGNORE INTO activity_members (activity_id, user_id) VALUES (?,?)")) {
                    ps.setInt(1, activityId);
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                }
                con.commit();
                return null;
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    public void leave(int activityId, int userId) throws Exception {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "DELETE FROM activity_members WHERE activity_id = ? AND user_id = ?")) {
            ps.setInt(1, activityId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    // only the host can cancel their own activity
    public boolean cancel(int activityId, int userId) throws Exception {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "UPDATE activities SET status = 'CANCELLED' WHERE id = ? AND created_by = ? AND status = 'ACTIVE'")) {
            ps.setInt(1, activityId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    // called by the background thread: marks finished activities as EXPIRED
    public int expireOld() throws Exception {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "UPDATE activities SET status = 'EXPIRED' WHERE status = 'ACTIVE' AND end_time <= ?")) {
            ps.setTimestamp(1, nowTs());
            return ps.executeUpdate();
        }
    }
}