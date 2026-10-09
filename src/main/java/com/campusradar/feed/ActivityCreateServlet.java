package com.campusradar.feed;

import com.campusradar.common.User;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/feed/create")
public class ActivityCreateServlet extends HttpServlet {
    private final ActivityDAO dao = new ActivityDAO();

    private static final List<String> CATEGORIES =
            Arrays.asList("Sports", "Study", "Music", "Food", "Gaming", "Coding", "Other");

    private void showForm(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            req.setAttribute("locations", dao.listLocations());
        } catch (Exception e) {
            req.setAttribute("error", "Could not load places: " + e.getMessage());
        }
        req.getRequestDispatcher("/WEB-INF/views/feed/create.jsp").forward(req, res);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        if (req.getSession().getAttribute("user") == null) {
            res.sendRedirect(req.getContextPath() + "/feed/devlogin");
            return;
        }
        showForm(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            res.sendRedirect(req.getContextPath() + "/feed/devlogin");
            return;
        }

        String title = req.getParameter("title");
        String description = req.getParameter("description");
        String category = req.getParameter("category");
        String place = req.getParameter("place");
        String locParam = req.getParameter("locationId");
        String error = null;

        try {
            int slots = Integer.parseInt(req.getParameter("slots"));
            int minutes = Integer.parseInt(req.getParameter("minutes"));
            Integer locationId = (locParam == null || locParam.isEmpty()) ? null : Integer.valueOf(locParam);
            place = (place == null) ? "" : place.trim();
            description = (description == null) ? "" : description.trim();

            if (title == null || title.trim().length() < 3 || title.trim().length() > 120) {
                error = "The title must be 3 to 120 characters.";
            } else if (category == null || !CATEGORIES.contains(category)) {
                error = "Choose a category.";
            } else if (slots < 1 || slots > 50) {
                error = "People needed must be between 1 and 50.";
            } else if (minutes < 10 || minutes > 480) {
                error = "Duration must be between 10 minutes and 8 hours.";
            } else if (description.length() > 300) {
                error = "The description is too long (maximum 300 characters).";
            } else if (place.length() > 100) {
                error = "The place name is too long.";
            } else if (locationId == null && place.isEmpty()) {
                error = "Choose a place or type one.";
            } else if (dao.countActiveByHost(user.getId()) >= 3) {
                error = "You already have 3 live activities. Cancel one or wait until it ends.";
            }

            if (error == null) {
                dao.create(title.trim(), description, category, locationId, place, slots, minutes, user.getId());
                res.sendRedirect(req.getContextPath() + "/feed/list?msg=Your+activity+is+live");
                return;
            }
        } catch (NumberFormatException e) {
            error = "Please fill in all the numbers correctly.";
        } catch (Exception e) {
            error = "Could not create the activity: " + e.getMessage();
        }
        req.setAttribute("error", error);
        showForm(req, res);
    }
}