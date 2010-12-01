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
