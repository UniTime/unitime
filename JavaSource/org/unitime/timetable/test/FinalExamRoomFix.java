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

import java.util.List;
import java.util.Properties;

import org.hibernate.Session;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.dao._RootDAO;

/**
 * @author Tomas Muller
 */
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
