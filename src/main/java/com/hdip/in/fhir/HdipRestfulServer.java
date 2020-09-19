package com.hdip.in.fhir;

import ca.uhn.fhir.jpa.starter.JpaRestfulServer;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletException;

public class HdipRestfulServer extends JpaRestfulServer {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    @Override
    protected void initialize() throws ServletException {
        super.initialize();
        ApplicationContext context =  (ApplicationContext) getServletContext()
                .getAttribute("org.springframework.web.context.WebApplicationContext.ROOT");
        super.registerInterceptor(ResourceCreatedInterceptor.class);
    }

}
