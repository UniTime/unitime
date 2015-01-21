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
