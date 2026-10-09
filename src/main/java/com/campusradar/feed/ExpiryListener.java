package com.campusradar.feed;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

// Starts when the web app starts and stops when it shuts down.
@WebListener
public class ExpiryListener implements ServletContextListener {
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "activity-expiry");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            try {
                int n = new ActivityDAO().expireOld();
                if (n > 0) System.out.println("[Feed] Expired " + n + " activities");
            } catch (Throwable t) {
                // a failed run must not stop the schedule
                System.out.println("[Feed] Expiry check failed: " + t.getMessage());
            }
        }, 10, 60, TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) scheduler.shutdownNow();
    }
}