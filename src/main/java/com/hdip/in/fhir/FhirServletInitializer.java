package com.hdip.in.fhir;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

public class FhirServletInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        ServletRegistration.Dynamic dispatcher = servletContext
            .addServlet("fhirServlet", (Servlet) new HdipRestfulServer());
        dispatcher.setLoadOnStartup(0);
        dispatcher.addMapping("/fhir/*");
    }
}
