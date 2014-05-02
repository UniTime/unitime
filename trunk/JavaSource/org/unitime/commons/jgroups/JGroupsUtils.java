/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.commons.jgroups;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jgroups.conf.ConfiguratorFactory;
import org.jgroups.conf.ProtocolConfiguration;
import org.jgroups.conf.ProtocolStackConfigurator;
import org.jgroups.util.Util;
import org.unitime.timetable.ApplicationProperties;

/**
 * @author Tomas Muller
 */
public class JGroupsUtils {

    public static String getProperty(String s) {
        String var, default_val, retval=null;
        int index=s.indexOf(":");
        if(index >= 0) {
            var=s.substring(0, index);
            default_val=s.substring(index+1);
            if(default_val != null && default_val.length() > 0)
                default_val=default_val.trim();
            // retval=System.getProperty(var, default_val);
            retval=_getProperty(var, default_val);
        }
        else {
            var=s;
            // retval=System.getProperty(var);
            retval=_getProperty(var, null);
        }
        return retval;
    }

	private static String _getProperty(String var, String default_value) {
        if(var == null)
            return null;
        List<String> list=Util.parseCommaDelimitedStrings(var);
        if(list == null || list.isEmpty()) {
            list=new ArrayList<String>(1);
            list.add(var);
        }
        String retval=null;
        for(String prop: list) {
            try {
                retval=ApplicationProperties.getProperty(prop);
                if(retval != null)
                    return retval;
            }
            catch(Throwable e) {
            }
        }
        return default_value;
    }
    
    private static String _substituteVar(String val) {
        int start_index, end_index;
        start_index=val.indexOf("${");
        if(start_index == -1)
            return val;
        end_index=val.indexOf("}", start_index+2);
        if(end_index == -1)
            throw new IllegalArgumentException("missing \"}\" in " + val);

        String tmp=getProperty(val.substring(start_index +2, end_index));
        if(tmp == null)
            return val;
        StringBuilder sb=new StringBuilder();
        sb.append(val.substring(0, start_index));
        sb.append(tmp);
        sb.append(val.substring(end_index+1));
        return sb.toString();
    }
    
    public static String substituteVariable(String val) {
        if(val == null) return val;
        String retval=val, prev;

        while(retval.contains("${")) { // handle multiple variables in val
            prev=retval;
            retval=_substituteVar(retval);
            if(retval.equals(prev))
                break;
        }
        return retval;
    }
    
    public static void substituteVariables(ProtocolConfiguration config) {
    	Map<String, String> properties = config.getProperties();
        for(Iterator<Map.Entry<String, String>> it=properties.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, String> entry=it.next();
            String key=entry.getKey();
            String val=entry.getValue();
            String tmp=substituteVariable(val);
            if(!val.equals(tmp)) {
                properties.put(key, tmp);
            }
        }
    }
    
    public static void substituteVariables(ProtocolStackConfigurator configurator) {
    	for(ProtocolConfiguration config: configurator.getProtocolStack())
    		substituteVariables(config);
    }
    
    public static ProtocolStackConfigurator getConfigurator(String properties) throws Exception {
		ProtocolStackConfigurator configurator = ConfiguratorFactory.getStackConfigurator(properties);
		JGroupsUtils.substituteVariables(configurator);
		return configurator;
    }
    
    public static String getConfigurationString(String properties) throws Exception {
		ProtocolStackConfigurator configurator = ConfiguratorFactory.getStackConfigurator(properties);
		StringBuffer ret = new StringBuffer();
    	for(ProtocolConfiguration config: configurator.getProtocolStack()) {
    		substituteVariables(config);
    		if (ret.length() > 0) ret.append(":");
    		ret.append(config.getProtocolString());
    	}
		return ret.toString();
    }
}
