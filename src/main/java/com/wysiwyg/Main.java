package com.wysiwyg;

import java.io.File;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.Integer;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;


public class Main {

    public static void main(String[] args) throws Exception {

        assert args.length > 0 : "My Index missing!";

        PrintWriter writer = new PrintWriter(BackMgr.CACHE_INDEX, "UTF-8");
        writer.println(args[0]);
        writer.close();

        // FileReader fileReader = new FileReader(new File("CONFIG"));
        BufferedReader bufferedReader = new BufferedReader(new FileReader("CONFIG"));
        int lineCount = 0;
        String webPort = null;
        while (lineCount < Integer.parseInt(args[0])) {
            lineCount++;
            String line = bufferedReader.readLine();
            String[] info = line.split(":");
            webPort = info[1];
        }

        String webappDirLocation = "src/main/webapp/";
        Tomcat tomcat = new Tomcat();

        //The port that we should run on can be set into an environment variable
        //Look for that variable and default to 8080 if it isn't there.
        // String webPort = System.getenv("PORT");
        if(webPort == null || webPort.isEmpty()) {
            webPort = "8080";
        }

        tomcat.setPort(Integer.valueOf(webPort));

        StandardContext ctx = (StandardContext) tomcat.addWebapp("/", new File(webappDirLocation).getAbsolutePath());
        System.out.println("configuring app with basedir: " + new File("./" + webappDirLocation).getAbsolutePath());

        // Declare an alternative location for your "WEB-INF/classes" dir
        // Servlet 3.0 annotation will work
        File additionWebInfClasses = new File("target/classes");
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                additionWebInfClasses.getAbsolutePath(), "/"));
        ctx.setResources(resources);

        tomcat.start();
        tomcat.getServer().await();
    }
}
