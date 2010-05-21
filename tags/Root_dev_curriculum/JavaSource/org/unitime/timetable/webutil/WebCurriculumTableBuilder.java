package org.unitime.timetable.webutil;

import java.util.List;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

import org.hibernate.Query;
import org.unitime.timetable.form.CurriculumListForm;
import org.unitime.timetable.model.dao.EventDAO;

public class WebCurriculumTableBuilder {

	protected List loadCurricula (CurriculumListForm form) {
		
		String query = "select distinct c from Curriculum c";
		Query hibQuery = new EventDAO().getSession().createQuery(query);
		
		return hibQuery.setCacheable(true).list();
	}
	
    public void htmlTableForCurricula (HttpSession httpSession, CurriculumListForm form, JspWriter outputStream){
        List curricula = loadCurricula(form);
    }
}
