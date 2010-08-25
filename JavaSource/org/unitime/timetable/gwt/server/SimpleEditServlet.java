package org.unitime.timetable.gwt.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.gwt.services.SimpleEditService;
import org.unitime.timetable.gwt.shared.CurriculaException;
import org.unitime.timetable.gwt.shared.SimpleEditException;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.ListItem;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Type;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.dao.AcademicAreaDAO;
import org.unitime.timetable.model.dao.AcademicClassificationDAO;
import org.unitime.timetable.model.dao.CurriculumDAO;
import org.unitime.timetable.model.dao.PosMajorDAO;
import org.unitime.timetable.model.dao.PosMinorDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.util.Constants;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class SimpleEditServlet extends RemoteServiceServlet implements SimpleEditService {
	private static final long serialVersionUID = 8338135183971720592L;
	private static Logger sLog = Logger.getLogger(SimpleEditServlet.class);


	public void init() throws ServletException {
	}

	@Override
	public SimpleEditInterface load(Type type) throws SimpleEditException {
		org.hibernate.Session hibSession = null;
		try {
			hibSession = CurriculumDAO.getInstance().getSession();
			Long sessionId = getAcademicSessionId();
			SimpleEditInterface data = null;
			switch (type) {
			case area:
				data = new SimpleEditInterface(type,
						new Field("External Id", FieldType.text, 120, 40),
						new Field("Abbreviation", FieldType.text, 80, 10),
						new Field("Short Title", FieldType.text, 200, 50),
						new Field("Long Title", FieldType.text, 500, 100)
						);
				data.setSortBy(1,2,3);
				for (AcademicArea area: AcademicAreaDAO.getInstance().findBySession(hibSession, sessionId)) {
					Record r = data.addRecord(area.getUniqueId());
					r.setField(0, area.getExternalUniqueId());
					r.setField(1, area.getAcademicAreaAbbreviation());
					r.setField(2, area.getShortTitle());
					r.setField(3, area.getLongTitle());
				}
				break;
			case classification:
				data = new SimpleEditInterface(type,
						new Field("External Id", FieldType.text, 120, 40),
						new Field("Code", FieldType.text, 80, 10),
						new Field("Name", FieldType.text, 500, 50));
				data.setSortBy(1,2);
				for (AcademicClassification clasf: AcademicClassificationDAO.getInstance().findBySession(hibSession, sessionId)) {
					Record r = data.addRecord(clasf.getUniqueId());
					r.setField(0, clasf.getExternalUniqueId());
					r.setField(1, clasf.getCode());
					r.setField(2, clasf.getName());
				}
				break;
			case major:
				List<ListItem> areas = new ArrayList<ListItem>();
				for (AcademicArea area: AcademicAreaDAO.getInstance().findBySession(hibSession, sessionId)) {
					areas.add(new ListItem(area.getUniqueId().toString(), area.getAcademicAreaAbbreviation() + " - " + (area.getLongTitle() == null ? area.getShortTitle() : area.getLongTitle())));
				}
				data = new SimpleEditInterface(type,
						new Field("External Id", FieldType.text, 120, 40),
						new Field("Code", FieldType.text, 80, 10),
						new Field("Name", FieldType.text, 300, 50),
						new Field("Academic Area(s)", FieldType.list, 300, areas));
				data.setSortBy(3,1,2);
				for (PosMajor major: PosMajorDAO.getInstance().findBySession(hibSession, sessionId)) {
					Record r = data.addRecord(major.getUniqueId());
					r.setField(0, major.getExternalUniqueId());
					r.setField(1, major.getCode());
					r.setField(2, major.getName());
					for (AcademicArea area: major.getAcademicAreas())
						r.setField(3, area.getUniqueId().toString());
				}
				break;
			case minor:
				areas = new ArrayList<ListItem>();
				for (AcademicArea area: AcademicAreaDAO.getInstance().findBySession(hibSession, sessionId)) {
					areas.add(new ListItem(area.getUniqueId().toString(), area.getAcademicAreaAbbreviation() + " - " + (area.getLongTitle() == null ? area.getShortTitle() : area.getLongTitle())));
				}
				data = new SimpleEditInterface(type,
						new Field("External Id", FieldType.text, 120, 40),
						new Field("Code", FieldType.text, 80, 10),
						new Field("Name", FieldType.text, 300, 50),
						new Field("Academic Area(s)", FieldType.list, 300, areas));
				data.setSortBy(3,1,2);
				for (PosMinor minor: PosMinorDAO.getInstance().findBySession(hibSession, sessionId)) {
					Record r = data.addRecord(minor.getUniqueId());
					r.setField(0, minor.getExternalUniqueId());
					r.setField(1, minor.getCode());
					r.setField(2, minor.getName());
					for (AcademicArea area: minor.getAcademicAreas())
						r.setField(3, area.getUniqueId().toString());
				}
				break;
			}
			data.setEditable(isAdmin());
			return data;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			if (e instanceof SimpleEditException)
				throw (SimpleEditException)e;
			throw new SimpleEditException(e.getMessage());
		} finally {
			if (hibSession != null && hibSession.isOpen())
				hibSession.close();
		}
	}
	
	@Override
	public SimpleEditInterface save(SimpleEditInterface data) {
		org.hibernate.Session hibSession = null;
		try {
			if (!isAdmin())
				throw new SimpleEditException("not authorized to edit " + data.getType().getTitle().toLowerCase());
			hibSession = CurriculumDAO.getInstance().getSession();
			Long sessionId = getAcademicSessionId();
			Transaction tx = null;
			try {
				tx = hibSession.beginTransaction();
				
				switch (data.getType()) {
				case area:
					for (AcademicArea area: AcademicAreaDAO.getInstance().findBySession(hibSession, sessionId)) {
						Record r = data.getRecord(area.getUniqueId());
						if (r == null) {
							hibSession.delete(area);
						} else {
							area.setExternalUniqueId(r.getField(0));
							area.setAcademicAreaAbbreviation(r.getField(1));
							area.setShortTitle(r.getField(2));
							area.setLongTitle(r.getField(3));
							hibSession.saveOrUpdate(area);
						}
					}
					for (Record r: data.getNewRecords()) {
						AcademicArea area = new AcademicArea();
						area.setExternalUniqueId(r.getField(0));
						area.setAcademicAreaAbbreviation(r.getField(1));
						area.setShortTitle(r.getField(2));
						area.setLongTitle(r.getField(3));
						area.setSession(SessionDAO.getInstance().get(sessionId, hibSession));
						r.setUniqueId((Long)hibSession.save(area));
					}	
					break;
				case classification:
					for (AcademicClassification clasf: AcademicClassificationDAO.getInstance().findBySession(hibSession, sessionId)) {
						Record r = data.getRecord(clasf.getUniqueId());
						if (r == null) {
							hibSession.delete(clasf);
						} else {
							clasf.setExternalUniqueId(r.getField(0));
							clasf.setCode(r.getField(1));
							clasf.setName(r.getField(2));
							hibSession.saveOrUpdate(clasf);
						}
					}
					for (Record r: data.getNewRecords()) {
						AcademicClassification clasf = new AcademicClassification();
						clasf.setExternalUniqueId(r.getField(0));
						clasf.setCode(r.getField(1));
						clasf.setName(r.getField(2));
						clasf.setSession(SessionDAO.getInstance().get(sessionId, hibSession));
						r.setUniqueId((Long)hibSession.save(clasf));
					}	
					break;
				case major:
					for (PosMajor major: PosMajorDAO.getInstance().findBySession(hibSession, sessionId)) {
						Record r = data.getRecord(major.getUniqueId());
						if (r == null) {
							hibSession.delete(major);
						} else {
							major.setExternalUniqueId(r.getField(0));
							major.setCode(r.getField(1));
							major.setName(r.getField(2));
							Set<AcademicArea> delete = new HashSet<AcademicArea>(major.getAcademicAreas());
							for (String areaId: r.getValues(3)) {
								AcademicArea area = AcademicAreaDAO.getInstance().get(Long.valueOf(areaId), hibSession);
								if (!delete.remove(area)) {
									major.getAcademicAreas().add(area);
									area.getPosMajors().add(major);
								}
							}
							for (AcademicArea area: delete) {
								major.getAcademicAreas().remove(area);
								area.getPosMajors().remove(major);
							}
							hibSession.saveOrUpdate(major);
						}
					}
					for (Record r: data.getNewRecords()) {
						PosMajor major = new PosMajor();
						major.setExternalUniqueId(r.getField(0));
						major.setCode(r.getField(1));
						major.setName(r.getField(2));
						major.setSession(SessionDAO.getInstance().get(sessionId, hibSession));
						major.setAcademicAreas(new HashSet<AcademicArea>());
						for (String areaId: r.getValues(3)) {
							AcademicArea area = AcademicAreaDAO.getInstance().get(Long.valueOf(areaId), hibSession);
							major.getAcademicAreas().add(area);
							area.getPosMajors().add(major);
						}
						r.setUniqueId((Long)hibSession.save(major));
					}
					break;
				case minor:
					for (PosMinor minor: PosMinorDAO.getInstance().findBySession(hibSession, sessionId)) {
						Record r = data.getRecord(minor.getUniqueId());
						if (r == null) {
							hibSession.delete(minor);
						} else {
							minor.setExternalUniqueId(r.getField(0));
							minor.setCode(r.getField(1));
							minor.setName(r.getField(2));
							Set<AcademicArea> delete = new HashSet<AcademicArea>(minor.getAcademicAreas());
							for (String areaId: r.getValues(3)) {
								AcademicArea area = AcademicAreaDAO.getInstance().get(Long.valueOf(areaId), hibSession);
								if (!delete.remove(area)) {
									minor.getAcademicAreas().add(area);
									area.getPosMinors().add(minor);
								}
							}
							for (AcademicArea area: delete) {
								minor.getAcademicAreas().remove(area);
								area.getPosMinors().remove(minor);
							}
							hibSession.saveOrUpdate(minor);
						}
					}
					for (Record r: data.getNewRecords()) {
						PosMinor minor = new PosMinor();
						minor.setExternalUniqueId(r.getField(0));
						minor.setCode(r.getField(1));
						minor.setName(r.getField(2));
						minor.setSession(SessionDAO.getInstance().get(sessionId, hibSession));
						minor.setAcademicAreas(new HashSet<AcademicArea>());
						for (String areaId: r.getValues(3)) {
							AcademicArea area = AcademicAreaDAO.getInstance().get(Long.valueOf(areaId), hibSession);
							minor.getAcademicAreas().add(area);
							area.getPosMinors().add(minor);
						}
						r.setUniqueId((Long)hibSession.save(minor));
					}
					break;
				}
				hibSession.flush();
				tx.commit(); tx = null;
			} finally {
				try {
					if (tx != null && tx.isActive()) {
						tx.rollback();
					}
				} catch (Exception e) {}
			}
			return data;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			if (e instanceof SimpleEditException)
				throw (SimpleEditException)e;
			throw new SimpleEditException(e.getMessage());
		} finally {
			if (hibSession != null && hibSession.isOpen())
				hibSession.close();
		}
	}
	
	private Long getAcademicSessionId() {
		User user = Web.getUser(getThreadLocalRequest().getSession());
		if (user == null) throw new CurriculaException("not authenticated");
		Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
		if (sessionId == null) throw new CurriculaException("academic session not selected");
		return sessionId;
	}
	
	public Boolean isAdmin() throws CurriculaException {
		try {
			User user = Web.getUser(getThreadLocalRequest().getSession());
			if (user == null) throw new CurriculaException("not authenticated");
			return Roles.ADMIN_ROLE.equals(user.getRole());
		} catch  (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}

}
