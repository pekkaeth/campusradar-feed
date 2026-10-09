package com.campusradar.feed;

import com.campusradar.common.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/feed/data")
public class FeedDataServlet extends HttpServlet {
    private final ActivityDAO dao = new ActivityDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please log in again.");
            return;
        }
        res.setHeader("Cache-Control", "no-store");
        try {
            req.setAttribute("activities", dao.listActive(user.getId(), req.getParameter("category")));
        } catch (Exception e) {
            req.setAttribute("error", "Could not load the feed: " + e.getMessage());
        }
        req.getRequestDispatcher("/WEB-INF/views/feed/cards.jsp").forward(req, res);
    }
}