/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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

import java.util.List;
import java.util.Properties;

import org.hibernate.Session;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.dao._RootDAO;

public class FinalExamRoomFix {
	
    public static void main(String[] args) {
        try {
            HibernateUtil.configureHibernate(new Properties());
            Session hibSession = new _RootDAO().getSession();
            
            ExamType type = ExamType.findByReference("final");
            
            for(Location location: (List<Location>)hibSession.createQuery(
            		"select distinct p.location from ExamLocationPref p where p.examPeriod.examType.uniqueId = :type"
            		).setLong("type", type.getUniqueId()).list()) {
            	if (!location.hasFinalExamsEnabled()) {
            		System.out.println("Fixing " + location.getLabel() + " (" + location.getSession().getLabel() + ")");
            		location.setExamEnabled(type, true);
            		hibSession.saveOrUpdate(location);
            	}
            }

            for(Location location: (List<Location>)hibSession.createQuery(
            		"select distinct r from Exam x inner join x.assignedRooms r where x.examType.uniqueId = :type"
            		).setLong("type", type.getUniqueId()).list()) {
            	if (!location.hasFinalExamsEnabled()) {
            		System.out.println("Fixing " + location.getLabel() + " (" + location.getSession().getLabel() + ")");
            		location.setExamEnabled(type, true);
            		hibSession.saveOrUpdate(location);
            	}
            }

            hibSession.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            HibernateUtil.closeHibernate();
        }
    }

}
