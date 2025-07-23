package com.shootingplace.shootingplace.utils.update;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public class RunPowerShell {

    private final Logger LOG = LogManager.getLogger();

    public ResponseEntity<?> code;

    public RunPowerShell(ResponseEntity<?> code) throws IOException {
        this.code = code;
        runUpdate();
    }

    public void runUpdate() throws IOException {

        if (this.code.getStatusCode().equals(HttpStatus.OK)) {
            LOG.info("RunPowerShell");

            LOG.info("Start: downloadfrontdziesiatka");

            Process pb1;
            pb1 = new ProcessBuilder("powershell", "-ExecutionPolicy", "Bypass", "-File", "C:/Program Files/Apache Software Foundation/Tomcat 9.0/webapps/downloadfrontdziesiatka.ps1")
                    .inheritIO()
                    .start();
            LOG.info("Stop: downloadfrontdziesiatka");
            LOG.info("Start: downloadbackdziesiatka");
            Process pb;
            pb = new ProcessBuilder("powershell", "-ExecutionPolicy", "Bypass", "-File", "C:/Program Files/Apache Software Foundation/Tomcat 9.0/webapps/downloadbackdziesiatka.ps1")
                    .inheritIO()
                    .start();
            LOG.info("Stop: downloadbackdziesiatka");
        }

    }
}
