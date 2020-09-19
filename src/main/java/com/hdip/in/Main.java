package com.hdip.in;

import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.io.IOException;

    public class Main {
        private static final int PORT = 8080;

        public static void main(String[] args) throws Exception {
            String appBase = ".";
            Tomcat tomcat = new Tomcat();
            tomcat.setHostname("localhost");
//            tomcat.setBaseDir(appBase);
            tomcat.setPort(PORT);
            tomcat.getHost().setAppBase(appBase);
            tomcat.addWebapp("/hapi-fhir-jpaserver",
                    new File(System.getProperty("java.io.tmpdir"))
                            .getAbsolutePath());
            tomcat.start();
            tomcat.getServer().await();
        }
}
