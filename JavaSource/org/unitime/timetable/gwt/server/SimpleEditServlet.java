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
package org.unitime.timetable.gwt.server;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;

import net.sf.cpsolver.ifs.util.ToolBox;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.gwt.services.SimpleEditService;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.SimpleEditException;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.ListItem;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Type;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.CourseCreditFormat;
import org.unitime.timetable.model.CourseCreditType;
import org.unitime.timetable.model.CourseCreditUnitType;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.dao.AcademicAreaDAO;
import org.unitime.timetable.model.dao.AcademicClassificationDAO;
import org.unitime.timetable.model.dao.CourseCreditFormatDAO;
import org.unitime.timetable.model.dao.CourseCreditTypeDAO;
import org.unitime.timetable.model.dao.CourseCreditUnitTypeDAO;
import org.unitime.timetable.model.dao.CurriculumDAO;
import org.unitime.timetable.model.dao.OfferingConsentTypeDAO;
import org.unitime.timetable.model.dao.PosMajorDAO;
import org.unitime.timetable.model.dao.PosMinorDAO;
import org.unitime.timetable.model.dao.PositionTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentGroupDAO;
import org.unitime.timetable.util.Constants;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * @author Tomas Muller
 */
public class SimpleEditServlet extends RemoteServiceServlet implements SimpleEditService {
	private static final long serialVersionUID = 8338135183971720592L;
	private static Logger sLog = Logger.getLogger(SimpleEditServlet.class);


	public void init() throws ServletException {
	}

	@Override
	public SimpleEditInterface load(Type type) throws SimpleEditException, PageAccessException {
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
						new Field("Academic Area", FieldType.list, 300, areas));
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
						new Field("Academic Area", FieldType.list, 300, areas));
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
			case group:
				data = new SimpleEditInterface(type,
						new Field("External Id", FieldType.text, 120, 40),
						new Field("Code", FieldType.text, 80, 10),
						new Field("Name", FieldType.text, 300, 50));
				data.setSortBy(1,2);
				for (StudentGroup group: StudentGroupDAO.getInstance().findBySession(hibSession, sessionId)) {
					Record r = data.addRecord(group.getUniqueId());
					r.setField(0, group.getExternalUniqueId());
					r.setField(1, group.getGroupAbbreviation());
					r.setField(2, group.getGroupName());
				}
				break;
			case consent:
				data = new SimpleEditInterface(type,
						new Field("Reference", FieldType.text, 160, 20),
						new Field("Name", FieldType.text, 300, 60),
						new Field("Abbreviation", FieldType.text, 160, 20));
				data.setSortBy(0, 1);
				data.setAddable(false);
				for (OfferingConsentType consent: OfferingConsentTypeDAO.getInstance().findAll()) {
					Record r = data.addRecord(consent.getUniqueId(), false);
					r.setField(0, consent.getReference(), false);
					r.setField(1, consent.getLabel(), true);
					r.setField(2, consent.getAbbv(), true);
				}
				break;
			case creditFormat:
				data = new SimpleEditInterface(type,
						new Field("Reference", FieldType.text, 160, 20),
						new Field("Name", FieldType.text, 300, 60),
						new Field("Abbreviation", FieldType.text, 80, 10));
				data.setSortBy(0, 1, 2);
				data.setAddable(false);
				for (CourseCreditFormat credit: CourseCreditFormatDAO.getInstance().findAll()) {
					Record r = data.addRecord(credit.getUniqueId(), false);
					r.setField(0, credit.getReference(), false);
					r.setField(1, credit.getLabel());
					r.setField(2, credit.getAbbreviation());
				}
				break;
			case creditType:
				data = new SimpleEditInterface(type,
						new Field("Reference", FieldType.text, 160, 20),
						new Field("Name", FieldType.text, 300, 60),
						new Field("Abbreviation", FieldType.text, 80, 10));
				data.setSortBy(0, 1, 2);
				for (CourseCreditType credit: CourseCreditTypeDAO.getInstance().findAll()) {
					int used =
						((Number)hibSession.createQuery(
								"select count(c) from CourseCreditUnitConfig c where c.creditType.uniqueId = :uniqueId")
								.setLong("uniqueId", credit.getUniqueId()).uniqueResult()).intValue();
					Record r = data.addRecord(credit.getUniqueId(), used == 0);
					r.setField(0, credit.getReference());
					r.setField(1, credit.getLabel());
					r.setField(2, credit.getAbbreviation());
				}
				break;
			case creditUnit:
				data = new SimpleEditInterface(type,
						new Field("Reference", FieldType.text, 160, 20),
						new Field("Name", FieldType.text, 300, 60),
						new Field("Abbreviation", FieldType.text, 80, 10));
				data.setSortBy(0, 1, 2);
				for (CourseCreditUnitType credit: CourseCreditUnitTypeDAO.getInstance().findAll()) {
					int used =
						((Number)hibSession.createQuery(
								"select count(c) from CourseCreditUnitConfig c where c.creditUnitType.uniqueId = :uniqueId")
								.setLong("uniqueId", credit.getUniqueId()).uniqueResult()).intValue();
					Record r = data.addRecord(credit.getUniqueId(), used == 0);
					r.setField(0, credit.getReference());
					r.setField(1, credit.getLabel());
					r.setField(2, credit.getAbbreviation());
				}
				break;
			case position:
				data = new SimpleEditInterface(type,
						new Field("Reference", FieldType.text, 160, 20),
						new Field("Name", FieldType.text, 300, 60),
						new Field("Sort Order", FieldType.text, 80, 10)
						);
				data.setSortBy(2, 0, 1);
				DecimalFormat df = new DecimalFormat("0000");
				for (PositionType position: PositionTypeDAO.getInstance().findAll()) {
					int used =
						((Number)hibSession.createQuery(
								"select count(f) from Staff f where f.positionType.uniqueId = :uniqueId")
								.setLong("uniqueId", position.getUniqueId()).uniqueResult()).intValue() +
						((Number)hibSession.createQuery(
								"select count(f) from DepartmentalInstructor f where f.positionType.uniqueId = :uniqueId")
								.setLong("uniqueId", position.getUniqueId()).uniqueResult()).intValue();
					Record r = data.addRecord(position.getUniqueId(), used == 0);
					r.setField(0, position.getReference());
					r.setField(1, position.getLabel());
					r.setField(2, df.format(position.getSortOrder()));
				}
				break;
			}
			data.setEditable(isAdmin());
			return data;
		} catch (PageAccessException e) {
			throw e;
		} catch (SimpleEditException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SimpleEditException(e.getMessage());
		} finally {
			if (hibSession != null && hibSession.isOpen())
				hibSession.close();
		}
	}
	
	@Override
	public SimpleEditInterface save(SimpleEditInterface data) throws SimpleEditException, PageAccessException {
		org.hibernate.Session hibSession = null;
		try {
			checkAdmin();
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
							ChangeLog.addChange(hibSession,
									getThreadLocalRequest(),
									area,
									area.getAcademicAreaAbbreviation() + " " + area.getLongTitle(),
									Source.SIMPLE_EDIT, 
									Operation.DELETE,
									null,
									null);
							hibSession.delete(area);
						} else {
							boolean changed = 
								!ToolBox.equals(area.getExternalUniqueId(), r.getField(0)) ||
								!ToolBox.equals(area.getAcademicAreaAbbreviation(), r.getField(1)) ||
								!ToolBox.equals(area.getShortTitle(), r.getField(2)) ||
								!ToolBox.equals(area.getLongTitle(), r.getField(3));
							area.setExternalUniqueId(r.getField(0));
							area.setAcademicAreaAbbreviation(r.getField(1));
							area.setShortTitle(r.getField(2));
							area.setLongTitle(r.getField(3));
							hibSession.saveOrUpdate(area);
							if (changed)
								ChangeLog.addChange(hibSession,
										getThreadLocalRequest(),
										area,
										area.getAcademicAreaAbbreviation() + " " + area.getLongTitle(),
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										null);
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
						ChangeLog.addChange(hibSession,
								getThreadLocalRequest(),
								area,
								area.getAcademicAreaAbbreviation() + " " + area.getLongTitle(),
								Source.SIMPLE_EDIT, 
								Operation.CREATE,
								null,
								null);
					}	
					break;
				case classification:
					for (AcademicClassification clasf: AcademicClassificationDAO.getInstance().findBySession(hibSession, sessionId)) {
						Record r = data.getRecord(clasf.getUniqueId());
						if (r == null) {
							ChangeLog.addChange(hibSession,
									getThreadLocalRequest(),
									clasf,
									clasf.getCode() + " " + clasf.getName(),
									Source.SIMPLE_EDIT, 
									Operation.DELETE,
									null,
									null);
							hibSession.delete(clasf);
						} else {
							boolean changed = 
								!ToolBox.equals(clasf.getExternalUniqueId(), r.getField(0)) ||
								!ToolBox.equals(clasf.getCode(), r.getField(1)) ||
								!ToolBox.equals(clasf.getName(), r.getField(2));
							clasf.setExternalUniqueId(r.getField(0));
							clasf.setCode(r.getField(1));
							clasf.setName(r.getField(2));
							hibSession.saveOrUpdate(clasf);
							if (changed)
								ChangeLog.addChange(hibSession,
										getThreadLocalRequest(),
										clasf,
										clasf.getCode() + " " + clasf.getName(),
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										null);
						}
					}
					for (Record r: data.getNewRecords()) {
						AcademicClassification clasf = new AcademicClassification();
						clasf.setExternalUniqueId(r.getField(0));
						clasf.setCode(r.getField(1));
						clasf.setName(r.getField(2));
						clasf.setSession(SessionDAO.getInstance().get(sessionId, hibSession));
						r.setUniqueId((Long)hibSession.save(clasf));
						ChangeLog.addChange(hibSession,
								getThreadLocalRequest(),
								clasf,
								clasf.getCode() + " " + clasf.getName(),
								Source.SIMPLE_EDIT, 
								Operation.CREATE,
								null,
								null);
					}	
					break;
				case major:
					for (PosMajor major: PosMajorDAO.getInstance().findBySession(hibSession, sessionId)) {
						Record r = data.getRecord(major.getUniqueId());
						if (r == null) {
							ChangeLog.addChange(hibSession,
									getThreadLocalRequest(),
									major,
									major.getCode() + " " + major.getName(),
									Source.SIMPLE_EDIT, 
									Operation.DELETE,
									null,
									null);
							hibSession.delete(major);
						} else {
							boolean changed =
								!ToolBox.equals(major.getExternalUniqueId(), r.getField(0)) ||
								!ToolBox.equals(major.getCode(), r.getField(1)) ||
								!ToolBox.equals(major.getName(), r.getField(2));
							major.setExternalUniqueId(r.getField(0));
							major.setCode(r.getField(1));
							major.setName(r.getField(2));
							Set<AcademicArea> delete = new HashSet<AcademicArea>(major.getAcademicAreas());
							for (String areaId: r.getValues(3)) {
								AcademicArea area = AcademicAreaDAO.getInstance().get(Long.valueOf(areaId), hibSession);
								if (!delete.remove(area)) {
									major.getAcademicAreas().add(area);
									area.getPosMajors().add(major);
									changed = true;
								}
							}
							for (AcademicArea area: delete) {
								major.getAcademicAreas().remove(area);
								area.getPosMajors().remove(major);
								changed = true;
							}
							hibSession.saveOrUpdate(major);
							if (changed)
								ChangeLog.addChange(hibSession,
										getThreadLocalRequest(),
										major,
										major.getCode() + " " + major.getName(),
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										null);
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
						ChangeLog.addChange(hibSession,
								getThreadLocalRequest(),
								major,
								major.getCode() + " " + major.getName(),
								Source.SIMPLE_EDIT, 
								Operation.CREATE,
								null,
								null);
					}
					break;
				case minor:
					for (PosMinor minor: PosMinorDAO.getInstance().findBySession(hibSession, sessionId)) {
						Record r = data.getRecord(minor.getUniqueId());
						if (r == null) {
							ChangeLog.addChange(hibSession,
									getThreadLocalRequest(),
									minor,
									minor.getCode() + " " + minor.getName(),
									Source.SIMPLE_EDIT, 
									Operation.DELETE,
									null,
									null);
							hibSession.delete(minor);
						} else {
							boolean changed =
								!ToolBox.equals(minor.getExternalUniqueId(), r.getField(0)) ||
								!ToolBox.equals(minor.getCode(), r.getField(1)) ||
								!ToolBox.equals(minor.getName(), r.getField(2));
							minor.setExternalUniqueId(r.getField(0));
							minor.setCode(r.getField(1));
							minor.setName(r.getField(2));
							Set<AcademicArea> delete = new HashSet<AcademicArea>(minor.getAcademicAreas());
							for (String areaId: r.getValues(3)) {
								AcademicArea area = AcademicAreaDAO.getInstance().get(Long.valueOf(areaId), hibSession);
								if (!delete.remove(area)) {
									minor.getAcademicAreas().add(area);
									area.getPosMinors().add(minor);
									changed = true;
								}
							}
							for (AcademicArea area: delete) {
								minor.getAcademicAreas().remove(area);
								area.getPosMinors().remove(minor);
								changed = true;
							}
							hibSession.saveOrUpdate(minor);
							if (changed)
								ChangeLog.addChange(hibSession,
										getThreadLocalRequest(),
										minor,
										minor.getCode() + " " + minor.getName(),
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										null);
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
						ChangeLog.addChange(hibSession,
								getThreadLocalRequest(),
								minor,
								minor.getCode() + " " + minor.getName(),
								Source.SIMPLE_EDIT, 
								Operation.CREATE,
								null,
								null);
					}
					break;
				case group:
					for (StudentGroup group: StudentGroupDAO.getInstance().findBySession(hibSession, sessionId)) {
						Record r = data.getRecord(group.getUniqueId());
						if (r == null) {
							ChangeLog.addChange(hibSession,
									getThreadLocalRequest(),
									group,
									group.getGroupAbbreviation() + " " + group.getGroupName(),
									Source.SIMPLE_EDIT, 
									Operation.DELETE,
									null,
									null);
							hibSession.delete(group);
						} else {
							boolean changed = 
								!ToolBox.equals(group.getExternalUniqueId(), r.getField(0)) ||
								!ToolBox.equals(group.getGroupAbbreviation(), r.getField(1)) ||
								!ToolBox.equals(group.getGroupName(), r.getField(2));
							group.setExternalUniqueId(r.getField(0));
							group.setGroupAbbreviation(r.getField(1));
							group.setGroupName(r.getField(2));
							hibSession.saveOrUpdate(group);
							if (changed)
								ChangeLog.addChange(hibSession,
										getThreadLocalRequest(),
										group,
										group.getGroupAbbreviation() + " " + group.getGroupName(),
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										null);
						}
					}
					for (Record r: data.getNewRecords()) {
						StudentGroup group = new StudentGroup();
						group.setExternalUniqueId(r.getField(0));
						group.setGroupAbbreviation(r.getField(1));
						group.setGroupName(r.getField(2));
						group.setSession(SessionDAO.getInstance().get(sessionId, hibSession));
						r.setUniqueId((Long)hibSession.save(group));
						ChangeLog.addChange(hibSession,
								getThreadLocalRequest(),
								group,
								group.getGroupAbbreviation() + " " + group.getGroupName(),
								Source.SIMPLE_EDIT, 
								Operation.CREATE,
								null,
								null);
					}	
					break;
				case consent:
					for (OfferingConsentType consent: OfferingConsentTypeDAO.getInstance().findAll()) {
						Record r = data.getRecord(consent.getUniqueId());
						if (r == null) {
							ChangeLog.addChange(hibSession,
									getThreadLocalRequest(),
									consent,
									consent.getReference() + " " + consent.getLabel(),
									Source.SIMPLE_EDIT, 
									Operation.DELETE,
									null,
									null);
							hibSession.delete(consent);
						} else {
							boolean changed = 
								!ToolBox.equals(consent.getReference(), r.getField(0)) ||
								!ToolBox.equals(consent.getLabel(), r.getField(1)) ||
								!ToolBox.equals(consent.getAbbv(), r.getField(2));
							consent.setReference(r.getField(0));
							consent.setLabel(r.getField(1));
							consent.setAbbv(r.getField(2));
							hibSession.saveOrUpdate(consent);
							if (changed)
								ChangeLog.addChange(hibSession,
										getThreadLocalRequest(),
										consent,
										consent.getReference() + " " + consent.getLabel(),
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										null);
						}
					}
					for (Record r: data.getNewRecords()) {
						OfferingConsentType consent = new OfferingConsentType();
						consent.setReference(r.getField(0));
						consent.setLabel(r.getField(1));
						consent.setAbbv(r.getField(2));
						r.setUniqueId((Long)hibSession.save(consent));
						ChangeLog.addChange(hibSession,
								getThreadLocalRequest(),
								consent,
								consent.getReference() + " " + consent.getLabel(),
								Source.SIMPLE_EDIT, 
								Operation.CREATE,
								null,
								null);
					}	
					break;
				case creditFormat:
					for (CourseCreditFormat credit: CourseCreditFormatDAO.getInstance().findAll()) {
						Record r = data.getRecord(credit.getUniqueId());
						if (r == null) {
							ChangeLog.addChange(hibSession,
									getThreadLocalRequest(),
									credit,
									credit.getReference() + " " + credit.getLabel(),
									Source.SIMPLE_EDIT, 
									Operation.DELETE,
									null,
									null);
							hibSession.delete(credit);
						} else {
							boolean changed = 
								!ToolBox.equals(credit.getReference(), r.getField(0)) ||
								!ToolBox.equals(credit.getLabel(), r.getField(1)) ||
								!ToolBox.equals(credit.getAbbreviation(), r.getField(2));
							credit.setReference(r.getField(0));
							credit.setLabel(r.getField(1));
							credit.setAbbreviation(r.getField(2));
							hibSession.saveOrUpdate(credit);
							if (changed)
								ChangeLog.addChange(hibSession,
										getThreadLocalRequest(),
										credit,
										credit.getReference() + " " + credit.getLabel(),
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										null);
						}
					}
					for (Record r: data.getNewRecords()) {
						CourseCreditFormat credit = new CourseCreditFormat();
						credit.setReference(r.getField(0));
						credit.setLabel(r.getField(1));
						credit.setAbbreviation(r.getField(2));
						r.setUniqueId((Long)hibSession.save(credit));
						ChangeLog.addChange(hibSession,
								getThreadLocalRequest(),
								credit,
								credit.getReference() + " " + credit.getLabel(),
								Source.SIMPLE_EDIT, 
								Operation.CREATE,
								null,
								null);
					}	
					break;
				case creditType:
					for (CourseCreditType credit: CourseCreditTypeDAO.getInstance().findAll()) {
						Record r = data.getRecord(credit.getUniqueId());
						if (r == null) {
							ChangeLog.addChange(hibSession,
									getThreadLocalRequest(),
									credit,
									credit.getReference() + " " + credit.getLabel(),
									Source.SIMPLE_EDIT, 
									Operation.DELETE,
									null,
									null);
							hibSession.delete(credit);
						} else {
							boolean changed = 
								!ToolBox.equals(credit.getReference(), r.getField(0)) ||
								!ToolBox.equals(credit.getLabel(), r.getField(1)) ||
								!ToolBox.equals(credit.getAbbreviation(), r.getField(2));
							credit.setReference(r.getField(0));
							credit.setLabel(r.getField(1));
							credit.setAbbreviation(r.getField(2));
							hibSession.saveOrUpdate(credit);
							if (changed)
								ChangeLog.addChange(hibSession,
										getThreadLocalRequest(),
										credit,
										credit.getReference() + " " + credit.getLabel(),
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										null);
						}
					}
					for (Record r: data.getNewRecords()) {
						CourseCreditType credit = new CourseCreditType();
						credit.setReference(r.getField(0));
						credit.setLabel(r.getField(1));
						credit.setAbbreviation(r.getField(2));
						r.setUniqueId((Long)hibSession.save(credit));
						ChangeLog.addChange(hibSession,
								getThreadLocalRequest(),
								credit,
								credit.getReference() + " " + credit.getLabel(),
								Source.SIMPLE_EDIT, 
								Operation.CREATE,
								null,
								null);
					}	
					break;
				case creditUnit:
					for (CourseCreditUnitType credit: CourseCreditUnitTypeDAO.getInstance().findAll()) {
						Record r = data.getRecord(credit.getUniqueId());
						if (r == null) {
							ChangeLog.addChange(hibSession,
									getThreadLocalRequest(),
									credit,
									credit.getReference() + " " + credit.getLabel(),
									Source.SIMPLE_EDIT, 
									Operation.DELETE,
									null,
									null);
							hibSession.delete(credit);
						} else {
							boolean changed = 
								!ToolBox.equals(credit.getReference(), r.getField(0)) ||
								!ToolBox.equals(credit.getLabel(), r.getField(1)) ||
								!ToolBox.equals(credit.getAbbreviation(), r.getField(2));
							credit.setReference(r.getField(0));
							credit.setLabel(r.getField(1));
							credit.setAbbreviation(r.getField(2));
							hibSession.saveOrUpdate(credit);
							if (changed)
								ChangeLog.addChange(hibSession,
										getThreadLocalRequest(),
										credit,
										credit.getReference() + " " + credit.getLabel(),
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										null);
						}
					}
					for (Record r: data.getNewRecords()) {
						CourseCreditUnitType credit = new CourseCreditUnitType();
						credit.setReference(r.getField(0));
						credit.setLabel(r.getField(1));
						credit.setAbbreviation(r.getField(2));
						r.setUniqueId((Long)hibSession.save(credit));
						ChangeLog.addChange(hibSession,
								getThreadLocalRequest(),
								credit,
								credit.getReference() + " " + credit.getLabel(),
								Source.SIMPLE_EDIT, 
								Operation.CREATE,
								null,
								null);
					}	
					break;
				case position:
					for (PositionType position: PositionTypeDAO.getInstance().findAll()) {
						Record r = data.getRecord(position.getUniqueId());
						if (r == null) {
							ChangeLog.addChange(hibSession,
									getThreadLocalRequest(),
									position,
									position.getReference() + " " + position.getLabel(),
									Source.SIMPLE_EDIT, 
									Operation.DELETE,
									null,
									null);
							hibSession.delete(position);
						} else {
							boolean changed = 
								!ToolBox.equals(position.getReference(), r.getField(0)) ||
								!ToolBox.equals(position.getLabel(), r.getField(1)) ||
								!ToolBox.equals(position.getSortOrder().toString(), r.getField(2));
							position.setReference(r.getField(0));
							position.setLabel(r.getField(1));
							position.setSortOrder(Integer.valueOf(r.getField(2)));
							hibSession.saveOrUpdate(position);
							if (changed)
								ChangeLog.addChange(hibSession,
										getThreadLocalRequest(),
										position,
										position.getReference() + " " + position.getLabel(),
										Source.SIMPLE_EDIT, 
										Operation.UPDATE,
										null,
										null);
						}
					}
					for (Record r: data.getNewRecords()) {
						PositionType position = new PositionType();
						position.setReference(r.getField(0));
						position.setLabel(r.getField(1));
						position.setSortOrder(Integer.valueOf(r.getField(2)));
						r.setUniqueId((Long)hibSession.save(position));
						ChangeLog.addChange(hibSession,
								getThreadLocalRequest(),
								position,
								position.getReference() + " " + position.getLabel(),
								Source.SIMPLE_EDIT, 
								Operation.CREATE,
								null,
								null);
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
		} catch (PageAccessException e) {
			throw e;
		} catch (SimpleEditException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SimpleEditException(e.getMessage());
		} finally {
			if (hibSession != null && hibSession.isOpen())
				hibSession.close();
		}
	}
	
	private Long getAcademicSessionId() {
		User user = Web.getUser(getThreadLocalRequest().getSession());
		if (user == null) throw new PageAccessException(
				getThreadLocalRequest().getSession().isNew() ? "Your timetabling session has expired. Please log in again." : "Login is required to use this page.");
		if (user.getRole() == null) throw new PageAccessException("Insufficient user privileges.");
		Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
		if (sessionId == null) throw new PageAccessException("Insufficient user privileges.");
		return sessionId;
	}
	
	public boolean isAdmin() {
		User user = Web.getUser(getThreadLocalRequest().getSession());
		return user != null && Roles.ADMIN_ROLE.equals(user.getRole());
	}
	
	public void checkAdmin() throws PageAccessException {
		User user = Web.getUser(getThreadLocalRequest().getSession());
		if (user == null) throw new PageAccessException(
				getThreadLocalRequest().getSession().isNew() ? "Your timetabling session has expired. Please log in again." : "Login is required to use this page.");
		if (!Roles.ADMIN_ROLE.equals(user.getRole())) throw new PageAccessException("Insufficient user privileges.");
	}

}
