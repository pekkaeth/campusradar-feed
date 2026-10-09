package com.campusradar.feed;

import com.campusradar.common.User;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/feed/join", "/feed/leave", "/feed/cancel"})
public class ActivityActionServlet extends HttpServlet {
    private final ActivityDAO dao = new ActivityDAO();

    private String json(boolean ok, String msg) {
        String m = (msg == null) ? "" : msg.replace("\\", "\\\\").replace("\"", "\\\"")
                                           .replace("\n", " ").replace("\r", " ");
        return "{\"ok\":" + ok + ",\"message\":\"" + m + "\"}";
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        res.setHeader("Cache-Control", "no-store");
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().print(json(false, "Please log in again."));
            return;
        }
        try {
            int id = Integer.parseInt(req.getParameter("id"));
            String path = req.getServletPath();
            if (path.equals("/feed/join")) {
                String result = dao.join(id, user.getId());
                res.getWriter().print(result == null ? json(true, "You joined! See you there.")
                                                     : json(false, result));
            } else if (path.equals("/feed/leave")) {
                dao.leave(id, user.getId());
                res.getWriter().print(json(true, "You left the activity."));
            } else {
                boolean done = dao.cancel(id, user.getId());
                res.getWriter().print(done ? json(true, "Activity cancelled.")
                                           : json(false, "Only the host can cancel a live activity."));
            }
        } catch (Exception e) {
            res.getWriter().print(json(false, "Something went wrong. Please try again."));
        }
    }
}