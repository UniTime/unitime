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
package org.unitime.timetable.test;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.dao._RootDAO;

public class GenAllExamNames {

    public static void main(String[] args) {
        Transaction tx = null;
        try {
            HibernateUtil.configureHibernate(new Properties());
            Session hibSession = new _RootDAO().getSession();
            tx = hibSession.beginTransaction();
            
            List exams = hibSession.createQuery("select x from Exam x where x.name is null").list();
            for (Iterator i=exams.iterator();i.hasNext();) {
                Exam exam = (Exam)i.next();
                exam.setName(exam.generateName());
                hibSession.update(exam);
            }

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx!=null) tx.rollback();
        } finally {
            HibernateUtil.closeHibernate();
        }
    }
}
