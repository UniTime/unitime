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

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.dao._RootDAO;

/**
 * @author Tomas Muller
 */
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
