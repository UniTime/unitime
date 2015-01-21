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
package org.unitime.timetable.test;

import java.io.PrintWriter;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.RoomDAO;
import org.unitime.timetable.model.dao._RootDAO;

/**
 * @author Tomas Muller
 */
public class RoomCoordinates {
    protected static Logger sLog = Logger.getLogger(AssignFirstAvailableTimePattern.class);

	public static void main(String[] args) {
        try {
            Properties props = new Properties();
            props.setProperty("log4j.rootLogger", "DEBUG, A1");
            props.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
            props.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
            props.setProperty("log4j.appender.A1.layout.ConversionPattern","%-5p %c{2}: %m%n");
            props.setProperty("log4j.logger.org.hibernate","INFO");
            props.setProperty("log4j.logger.org.hibernate.cfg","WARN");
            props.setProperty("log4j.logger.org.hibernate.cache.EhCacheProvider","ERROR");
            props.setProperty("log4j.logger.org.unitime.commons.hibernate","INFO");
            props.setProperty("log4j.logger.net","INFO");
            PropertyConfigurator.configure(props);
            
            HibernateUtil.configureHibernate(ApplicationProperties.getProperties());

            org.hibernate.Session hibSession = new _RootDAO().getSession();
            
            PrintWriter pw = new PrintWriter("rooms.sql");
            for (Building b: BuildingDAO.getInstance().findAll()) {
            	if (b.getCoordinateX() != null && b.getCoordinateY() != null) {
            		pw.println("update building set coordinate_x = " + b.getCoordinateX() + ", coordinate_y = " + b.getCoordinateY() + 
            				" where uniqueid = " + b.getUniqueId() + ";");
            	}
                for (Room r: RoomDAO.getInstance().findByBuilding(hibSession, b.getUniqueId())) {
                	if (r.getCoordinateX() != null && r.getCoordinateY() != null) {
                		pw.println("update room set coordinate_x = " + r.getCoordinateX() + ", coordinate_y = " + r.getCoordinateY() + 
                				" where uniqueid = " + r.getUniqueId() + ";");
                	}
                }
            }
            pw.flush(); pw.close();


            hibSession.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
