/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
