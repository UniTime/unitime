/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.ApplicationConfig;
import org.unitime.timetable.model.SessionConfig;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;


/**
 * Sets the system properties for any application.
 * The properties in this file is adapted for each application    
 * @author Heston Fernandes
 */
public class ApplicationProperties {
	private static Formats.Format<Date> sDF_file = Formats.getDateFormat("dd-MMM-yy_HHmmssSSS");
	
	private static Properties props = new Properties();
    private static long appPropertiesLastModified = -1, custPropertiesLastModified = -1;  
    private static PropertyFileChangeListener pfc=null;
    private static Properties configProps = null;
    private static Map<Long, Properties> sSessionProperties = new HashMap<Long, Properties>();
    
    private static final ThreadLocal<Long> sAcademicSession = new ThreadLocal<Long>() {
		 @Override
		 protected Long initialValue() {
            return null;
		 }
	};
	
	public static Long getSessionId() {
		return sAcademicSession.get();
	}
	
	public static void setSessionId(Long sessionId) {
		if (sessionId == null)
			sAcademicSession.remove();
		else {
			sAcademicSession.set(sessionId);
		}
	}
	
	/**
	 * Sets the properties 
	 */
	static {
        load();

		// Spawn thread to dynamically reload 
		// by design once this thread is set up it cannot be destroyed even if the reloaded property is set to false
		String dynamicReload = props.getProperty("tmtbl.properties.dynamic_reload", null);
		if ((appPropertiesLastModified>0 || custPropertiesLastModified>0) && dynamicReload!=null && dynamicReload.equalsIgnoreCase("true")) {
			pfc = new PropertyFileChangeListener();
	        pfc.start();
		}		
	}
	
	/**
	 * Load properties 
	 */
	public static void load() {
		try {
            // Load properties set in application.properties
			URL appPropertiesUrl = ApplicationProperties.class.getClassLoader().getResource("application.properties");
            if (appPropertiesUrl!=null) {
				Debug.info("Reading " + URLDecoder.decode(appPropertiesUrl.getPath(), "UTF-8") + " ...");
				props.load(appPropertiesUrl.openStream());
			}
            try {
                try {
                    appPropertiesLastModified = new File(appPropertiesUrl.toURI()).lastModified();
                } catch (URISyntaxException e) {
                    appPropertiesLastModified = new File(appPropertiesUrl.getPath()).lastModified();
                }
            } catch (Exception e) {}
            
            // Load properties set in custom properties
            String customProperties = System.getProperty("tmtbl.custom.properties");
            if (customProperties==null)
                customProperties = props.getProperty("tmtbl.custom.properties", "custom.properties");
            URL custPropertiesUrl = ApplicationProperties.class.getClassLoader().getResource(customProperties);
            if (custPropertiesUrl!=null) {
                Debug.info("Reading " + URLDecoder.decode(custPropertiesUrl.getPath(), "UTF-8") + " ...");
                props.load(custPropertiesUrl.openStream());
                try {
                    try {
                        custPropertiesLastModified = new File(custPropertiesUrl.toURI()).lastModified();
                    } catch (URISyntaxException e) {
                        custPropertiesLastModified = new File(custPropertiesUrl.getPath()).lastModified();
                    }
                } catch (Exception e) {}
            } else if (new File(customProperties).exists()) {
                Debug.info("Reading " + customProperties + " ...");
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(customProperties);
                    props.load(fis);
                    custPropertiesLastModified = new File(customProperties).lastModified();
                } finally {
                    if (fis!=null) fis.close();
                }
            }
            
            // Load system properties
            props.putAll(System.getProperties());
		} catch (Exception e) {
			Debug.error(e);
		}
	}

	/**
	 * Reload properties from file application.properties
	 */
	public static void reloadIfNeeded() {
		if (appPropertiesLastModified>=0) {
            URL appPropertiesUrl = ApplicationProperties.class.getClassLoader().getResource("application.properties");
            long appPropTS = -1;
            try {
                try {
                    appPropTS = new File(appPropertiesUrl.toURI()).lastModified();
                } catch (URISyntaxException e) {
                    appPropTS = new File(appPropertiesUrl.getPath()).lastModified();
                }
            } catch (Exception e) {}

            String customProperties = System.getProperty("tmtbl.custom.properties");
            if (customProperties==null) 
                customProperties = props.getProperty("tmtbl.custom.properties", "custom.properties");
            URL custPropertiesUrl = ApplicationProperties.class.getClassLoader().getResource(customProperties);
            long custPropTS = -1;
            try {
                if (custPropertiesUrl!=null) {
                    try {
                        custPropTS = new File(custPropertiesUrl.toURI()).lastModified();
                    } catch (URISyntaxException e) {
                        custPropTS = new File(custPropertiesUrl.getPath()).lastModified();
                    }
                } else if (new File(customProperties).exists()) {
                    custPropTS = new File(customProperties).lastModified();
                }
            } catch (Exception e) {}
            
            if (appPropTS>appPropertiesLastModified || custPropTS>custPropertiesLastModified)
                load();
        }
	}
	
	public static Properties getConfigProperties() {
	    if (configProps==null && _RootDAO.isConfigured())
	        configProps = ApplicationConfig.toProperties();
	    return (configProps==null?new Properties():configProps);
	}
	
	public static void clearConfigProperties() {
	    configProps = null;
	}
	
	public static Properties getSessionProperties(Long sessionId) {
		if (!_RootDAO.isConfigured() || sessionId == null)
			return new Properties();
		
		Properties properties = sSessionProperties.get(sessionId);
		if (properties == null) {
			properties = SessionConfig.toProperties(sessionId);
			sSessionProperties.put(sessionId, properties);
		}
		return properties;
	}
	
	public static void clearSessionProperties(Long sessionId) {
		if (sessionId == null)
			sSessionProperties.clear();
		else
			sSessionProperties.remove(sessionId);
	}
	
	/**
	 * Retrieves value for the property key
	 * @param key
	 * @return null if invalid key / key does not exist
	 */
	public static String getProperty(String key) {
	    return getProperty(key, null);
	}
	
	public static String getProperty(ApplicationProperty property) {
		return getProperty(property.key(), property.defaultValue());
	}

	/**
	 * Retrieves value for the property key
	 * @param defaultValue
	 * @param key
	 * @return default value if invalid key / key does not exist
	 */
	public static String getProperty(String key, String defaultValue) {
	    if(key==null || key.trim().length()==0)
	        return defaultValue;
	    
	    Long sessionId = ApplicationProperties.getSessionId();
	    if (sessionId != null) {
	    	String value = getSessionProperties(sessionId).getProperty(key);
	    	if (value != null) return value;
	    }
        
        String value = getConfigProperties().getProperty(key);
        if (value!=null) return value;
        
        return props.getProperty(key, defaultValue);
	}
	
	/**
	 * Return default properties (excluding application configuration)
	 */
	public static Properties getDefaultProperties() {
		return props;
	}
	
    /**
	 * Gets the properties used to configure the application 
	 * @return Properties object
	 */
	public static Properties getProperties() {
        Properties ret = (Properties)props.clone();
        ret.putAll(getConfigProperties());
        
	    Long sessionId = ApplicationProperties.getSessionId();
        if (sessionId != null) {
	    	ret.putAll(getSessionProperties(sessionId));
	    }
        
		return ret;
	}

	/**
	 * Most resources are located in /WEB-INF folder
	 * This function constructs the absolute path to /WEB-INF
	 * @return Absolute file path 
	 */
	public static String getBasePath() {
		//Get the URL of the class location (usually in /WEB-INF/classes/...) 		
		URL url = ApplicationProperties.class.
							getProtectionDomain().getCodeSource().getLocation();
		
		if (url==null) return null;
		
		//Get file and parent		
		File file = null;
		try {
			// Try to use URI to avoid bug 4466485 on Windows (see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4466485)
			file = new File(new URI(url.toString()).getPath());
		} catch (URISyntaxException e) {
			file = new File(url.getFile());
		}
		File parent = file.getParentFile();

		// Iterate up the folder structure till WEB-INF is encountered
		while (parent!=null && !parent.getName().equals("WEB-INF"))
			parent = parent.getParentFile();

		return (parent==null?null:parent.getAbsolutePath());
	}
	
	public static File getDataFolder() {
		if (getProperty("unitime.data.dir") != null) {
			File dir = new File(getProperty("unitime.data.dir"));
			dir.mkdirs();
			return dir;
		}
		File dir = new File(getBasePath());
		if (!dir.getName().equals("webapps")) dir = dir.getParentFile();
		dir = dir.getParentFile().getParentFile();
		dir = new File(dir, "data");
		dir =  new File(dir,"unitime");
		dir.mkdirs();
		return dir;
	}
	
	public static File getBlobFolder() {
		File dir = new File(getDataFolder(),"blob");
		dir.mkdir();
		return dir;
	}

	public static File getRestoreFolder() {
		File dir = new File(getDataFolder(),"restore");
		dir.mkdir();
		return dir;
	}
	
	public static File getPassivationFolder() {
		File dir = new File(getDataFolder(),"passivate");
		dir.mkdir();
		return dir;
	}
	
	public static File getTempFolder() {
        File dir = new File(new File(getBasePath()).getParentFile(), "temp");
		dir.mkdir();
		return dir;
	}

	public static boolean isLocalSolverEnabled() {
		return "true".equalsIgnoreCase(getProperty("tmtbl.solver.local.enabled","true"));
	}
	
	public static File getTempFile(String prefix, String ext) {
		File file = null;
		try {
			file = File.createTempFile(prefix+"_"+sDF_file.format(new Date()),"."+ext,getTempFolder());
		} catch (IOException e) {
			Debug.error(e);
			file = new File(getTempFolder(), prefix+"_"+sDF_file.format(new Date())+"."+ext);
		}
		file.deleteOnExit();
		return file;
	}
	
	/**
	 * Stop Property File Change Listener Thread 
	 */
	public static void stopListener() {
		if (pfc!=null && pfc.isAlive() && !pfc.isInterrupted()) {
			Debug.info("Stopping Property File Change Listener Thread ...");
			pfc.interrupt();
		}
	}
    
    /**
     * Thread to check if property file has changed
     * and reload the properties on the fly. Interval = 1 minute
     */
    static class PropertyFileChangeListener extends Thread {

        public PropertyFileChangeListener() {
            setName("Property File Change Listener Thread");
            setDaemon(true);
        }

        public void run() {
            try {
                Debug.info("Starting Property File Change Listener Thread ...");
                
                long threadInterval = Constants.getPositiveInteger(
                        ApplicationProperties.getProperty("tmtbl.properties.dynamic_reload_interval"), 15000 );
                
                while (true) {
                    try {
                        sleep(threadInterval);
                        reloadIfNeeded();
                    } catch (InterruptedException e) {
                        Debug.info("Property File Change Listener Thread interrupted ...");
                        break;
                    }
                }
                
            } catch (Exception e) {
                Debug.warning("Property File Change Listener Thread failed, reason: "+e.getMessage());
            }
        }
    }
}
