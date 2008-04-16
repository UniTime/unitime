/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.test;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventType;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.dao._RootDAO;

import net.sf.cpsolver.ifs.util.ToolBox;

public class MakeEventsForAllCommitedAssignments {

    public static void main(String args[]) {
        try {
            ToolBox.configureLogging();
            HibernateUtil.configureHibernate(new Properties());
            
            Session hibSession = new _RootDAO().getSession();
            List commitedSolutions = hibSession.createQuery("select distinct s from Solution s inner join s.assignments a where s.commited = true and a.event is null").list();
            
            for (Iterator i=commitedSolutions.iterator();i.hasNext();) {
                Solution s = (Solution)i.next();
                
                System.out.println("Procession solution "+s.getOwner().getName()+" of "+s.getSession().getLabel()+" (committed:"+s.getCommitDate()+")");
                
                Transaction tx = null;
                try {
                    tx = hibSession.beginTransaction();

                    EventType eventType = EventType.findByReference(EventType.sEventTypeClass);
                    for (Iterator j=hibSession.createQuery(
                            "select a from Assignment a "+
                            "where a.event is null and a.solution.uniqueId = :solutionId")
                            .setLong("solutionId", s.getUniqueId())
                            .iterate();
                        j.hasNext();) {
                        Assignment a = (Assignment)j.next();
                        Event event = a.generateCommittedEvent(eventType, true);
                        if (event!=null) {
                            System.out.println("  "+a.getClassName()+" "+a.getPlacement().getLongName());
                            a.setEvent(event);
                            hibSession.save(event);
                            hibSession.update(a);
                        }
                    }
                    
                    tx.commit();
                } catch (Exception e) {
                    if (tx!=null) tx.rollback();
                    throw e;
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
