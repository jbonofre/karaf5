package org.apache.karaf.core;

import lombok.extern.java.Log;
import org.apache.felix.framework.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

import java.util.logging.Level;

@Log
public class KarafLogger extends Logger {

    @Override
    protected void doLog(Bundle bundle, ServiceReference sr, int level, String msg, Throwable throwable) {
        switch (level) {
            case LOG_DEBUG: {
                log.log(Level.FINE, msg, throwable);
                break;
            }
            case LOG_ERROR: {
                log.log(Level.SEVERE, msg, throwable);
                break;
            }
            case LOG_INFO: {
                log.log(Level.INFO, msg, throwable);
                break;
            }
            case LOG_WARNING: {
                log.log(Level.WARNING, msg, throwable);
                break;
            }
        }
    }

    @Override
    protected void doLog(int level, String msg, Throwable throwable) {
        switch (level) {
            case LOG_DEBUG: {
                log.log(Level.FINE, msg, throwable);
                break;
            }
            case LOG_ERROR: {
                log.log(Level.SEVERE, msg, throwable);
                break;
            }
            case LOG_INFO: {
                log.log(Level.INFO, msg, throwable);
                break;
            }
            case LOG_WARNING: {
                log.log(Level.WARNING, msg, throwable);
                break;
            }
        }
    }
}
