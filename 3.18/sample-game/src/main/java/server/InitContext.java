package server;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class InitContext implements ServletContextListener {

    // Class is set as servlet context listener in Deployment Descriptor - ./webapp/WEB-INF/web.xml

    @Override
    public void contextInitialized(ServletContextEvent event) {

        // Set absolute path to 'WEB-INF' directory as a property in running VM
        ServletContext servletContext = event.getServletContext();
        System.setProperty("web-inf-path", servletContext.getRealPath("/WEB-INF/"));
    }
}
