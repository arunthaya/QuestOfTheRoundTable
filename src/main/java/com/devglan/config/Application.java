package com.devglan.config;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Application {
    public static Logger logIt = Logger.getLogger(Application.class.getName());


    public static void main(String[] args) {
        //final Level CLIENT = Level.
        //logIt.log(CLIENT, "a verbose message");
        //BasicConfigurator.configure();
        PropertyConfigurator.configure("log4j.properties");
        logIt.info("testing log 4j");
        SpringApplication.run(Application.class, args);
    }
}
