package com.jingtie.organizer;

/**
 * Created by jingtie on 8/22/15.
 */
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.File;


public class OrganizerProperties {

    private OrganizerProperties() {
    }

    public static CompositeConfiguration getInstance() {
        if (configuration == null) {
            synchronized (LOCK) {
                if (configuration == null) {
                    try {
                        String propsLocation = DEFAULT_PROPS_LOCATION;
                        File propsFile = new File(propsLocation);
                        if (!propsFile.exists()) {
                            propsLocation = "/var/lib/tomcat8/webapps/organizer/organizer.props";
                            propsFile = new File(propsLocation);
                        }
                        if (!propsFile.exists()) {
                            propsLocation = "/var/lib/tomcat7/webapps/organizer/organizer.props";
                            propsFile = new File(propsLocation);
                        }
                        if (!propsFile.exists()) {
                            propsLocation = "/var/lib/tomcat/webapps/organizer/organizer.props";
                            propsFile = new File(propsLocation);
                        }
                        if (!propsFile.exists()) {
                            throw new IllegalArgumentException("Cannot find organizer.props file");
                        }

                        configuration = new CompositeConfiguration();
                        configuration.addConfiguration(new PropertiesConfiguration(propsFile.getAbsoluteFile()));

                        // config logger
                        File loggerConfigFile = new File(propsFile.getParent(), "log4j.xml");
                        String loggerFilename = configuration.getString(LOG_CONFIG_FILE, loggerConfigFile.getAbsolutePath());
                        loggerConfigFile = new File(loggerFilename);

                        if (!loggerConfigFile.exists()) {
                            throw new IllegalArgumentException("Cannot find log4j.xml file");
                        }

                        DOMConfigurator.configure(loggerConfigFile.getAbsolutePath());

                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }
            }
        }

        return configuration;
    }


    private static CompositeConfiguration configuration = null;
    private static final Object LOCK = new Object();
    private static final String LOG_CONFIG_FILE = "log4j_config_file";
    public static String DEFAULT_PROPS_LOCATION = "src/main/resources/config/organizer.props";

}
