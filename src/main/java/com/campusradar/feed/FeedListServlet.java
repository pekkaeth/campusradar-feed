package com.campusradar.feed;

import com.campusradar.common.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/feed/list")
public class FeedListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            res.sendRedirect(req.getContextPath() + "/feed/devlogin");
            return;
        }
        // the activity cards are loaded by the page itself through /feed/data
        req.getRequestDispatcher("/WEB-INF/views/feed/list.jsp").forward(req, res);
    }
}