package io.s4.core;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.felix.framework.FrameworkFactory;
import org.apache.felix.main.AutoProcessor;
import org.osgi.framework.launch.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrameworkLauncher {

	private Framework fwk;
	final Logger logger = LoggerFactory.getLogger(FrameworkLauncher.class);
	private static final String CONFIG_PROPERTIES_FILE_VALUE = "config.properties";

	
	public void launch(){
		
		try {
			Properties configProps = loadProperties();
            fwk = getFrameworkFactory().newFramework(configProps);
            fwk.init();
            AutoProcessor.process(configProps, fwk.getBundleContext());
            fwk.start();
            logger.info("Framework started");
            fwk.waitForStop(0);
            System.exit(0);
        } catch (Exception ex){
            logger.error("Could not create framework",ex);
            System.exit(-1);
        }
		
	}
	
	public Framework getFwk() {
		return fwk;
	}
	
	private FrameworkFactory getFrameworkFactory() throws Exception{
		
		URL url = getClass().getClassLoader().getResource("META-INF/services/org.osgi.framework.launch.FrameworkFactory");
		if (url != null){
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            try {
                for (String s = br.readLine(); s != null; s = br.readLine()){
                    s = s.trim();
                    // Try to load first non-empty, non-commented line.
                    if ((s.length() > 0) && (s.charAt(0) != '#')){
                        return (FrameworkFactory) Class.forName(s).newInstance();
                    }
                }
            } finally {
                if (br != null) br.close();
            }
        }

        throw new Exception("Could not find framework factory.");
	}
	
	private Properties loadProperties() {

        try {
            InputStream is = this.getClass().getResourceAsStream(
                    "/conf/config.properties");
            PropertiesConfiguration config = new PropertiesConfiguration();
            config.load(is);

            return ConfigurationConverter.getProperties(config);
        } catch (ConfigurationException e) {
            logger.error("Error reading config file" + CONFIG_PROPERTIES_FILE_VALUE, e);
            return null;
        }
	}
	
}
