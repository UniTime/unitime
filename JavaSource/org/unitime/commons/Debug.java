/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.commons;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.logging.LogFactory;

/**
 * This class provides logging of debug informations.
 *
 * Debug files are writen in directory provided via key DEBUG_DIR in configuration.
 * Maximum number of debug files is limited via key DEBUG_MAXFILES in configuration.
 * If the number of limited debug files exceeds, oldest debug file is deleted.
 *
 * @author Tomas Muller
 */
public class Debug {
    // Number format for logging (allocated memory)
    private static NumberFormat sNumberFormat =
            new DecimalFormat("###,#00.00", new DecimalFormatSymbols(Locale.US));

	/** Prints an error to log.
	 * @param e an error
	 */
	public static synchronized void error(Throwable e) {
		LogFactory.getLog(getSource(e)).error(e.getMessage(), e);
	}

	/** Prints an fatal error message to log.
	 * @param message a fatal error message
	 */
	public static synchronized void fatal(String message) {
        LogFactory.getLog(ToolBox.getCaller()).fatal(message);
	}

	/** Prints an error message to log.
	 * @param message an error message
	 */
	public static synchronized void error(String message) {
        LogFactory.getLog(ToolBox.getCaller()).error(message);
	}

	/** Prints an error message to log.
	 * @param message an error message
	 */
	public static synchronized void error(String message, Throwable t) {
        LogFactory.getLog(ToolBox.getCaller()).error(message, t);
	}

	/** Prints a warning message to log.
	 * @param message a warning message
	 */
	public static synchronized void warning(String message) {
        LogFactory.getLog(ToolBox.getCaller()).warn(message);
	}

	/** Prints a message to log.
	 * @param message a message
	 */
	public static synchronized void info(String message) {
        LogFactory.getLog(ToolBox.getCaller()).info(message);
	}

	/** Prints an debug message to log.
	 * @param message debug message
	 */
	public static synchronized void debug(String message) {
        LogFactory.getLog(ToolBox.getCaller()).debug(message);
	}

	/** Prints a message to log.
	 * @param message a message
	 */
	public static synchronized void log(String message) {
		if (message != null) {
			if (message.startsWith("ERROR:")) {
                LogFactory.getLog(ToolBox.getCaller()).error(message.substring("ERROR:".length()).trim());
				return;
			} else if (message.startsWith("WARNING:")) {
                LogFactory.getLog(ToolBox.getCaller()).warn(message.substring("WARNING:".length()).trim());
				return;
			} else if (message.startsWith("INFO:")) {
                LogFactory.getLog(ToolBox.getCaller()).info(message.substring("INFO:".length()).trim());
				return;
			}
		}
        LogFactory.getLog(ToolBox.getCaller()).debug(message);
	}

	public static synchronized void log(int depth, String message) {
		if (message != null) {
			if (message.startsWith("ERROR:")) {
                LogFactory.getLog(ToolBox.getCaller(4 + depth)).error(message.substring("ERROR:".length()).trim());
				return;
			} else if (message.startsWith("WARNING:")) {
                LogFactory.getLog(ToolBox.getCaller(4 + depth)).warn(message.substring("WARNING:".length()).trim());
				return;
			} else if (message.startsWith("INFO:")) {
                LogFactory.getLog(ToolBox.getCaller(4 + depth)).info(message.substring("INFO:".length()).trim());
				return;
			}
		}
        LogFactory.getLog(ToolBox.getCaller(4 + depth)).debug(message);
	}

	/** Return source from a class.
	 * @param source a class
	 * @return class name (without package)
	 */
	public static synchronized String getSource(Class source) {
		String name = source.getName();

		if (name != null && name.indexOf('.') > 0) {
			return name.substring(name.lastIndexOf('.') + 1);
		}
		return name;
	}

	/** Return source from an object.
	 * @param source am object
	 * @return class name (without package)
	 */
	public static synchronized String getSource(Object source) {
		if (source == null) {
			return "";
		} else {
			return getSource(source.getClass());
		}
	}
    
    /** Returns amount of allocated memory.
     * @return amount of allocated memory to be written in the log
     */
    public static synchronized String getMem() {
        return sNumberFormat.format((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()) / 1048576.0)+"M ";
    }
    

    public static synchronized void init(Properties properties) {
        try {
            Class.
                forName("org.apache.log4j.PropertyConfigurator").
                getMethod("configure", new Class[] {Properties.class}).
                invoke(null, new Object[]{properties});
        } catch (Exception e) {
            System.err.println("Unable to init log4j -- "+e.getMessage());
        }
    }
    
}
