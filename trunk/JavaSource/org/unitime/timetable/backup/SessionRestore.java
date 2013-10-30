/*
 * UniTime 3.3 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.backup;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import net.sf.cpsolver.ifs.util.Progress;
import net.sf.cpsolver.ifs.util.ProgressWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.hibernate.FlushMode;
import org.hibernate.NonUniqueResultException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.BinaryType;
import org.hibernate.type.BooleanType;
import org.hibernate.type.ByteType;
import org.hibernate.type.CharacterType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.CustomType;
import org.hibernate.type.DateType;
import org.hibernate.type.DoubleType;
import org.hibernate.type.EntityType;
import org.hibernate.type.FloatType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.ListType;
import org.hibernate.type.LongType;
import org.hibernate.type.PrimitiveType;
import org.hibernate.type.SetType;
import org.hibernate.type.ShortType;
import org.hibernate.type.StringType;
import org.hibernate.type.TimestampType;
import org.hibernate.type.Type;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.OnlineSectioningLog;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RefTableEntry;
import org.unitime.timetable.model.RelatedCourseInfo;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.SolverInfoDef;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao._RootDAO;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;

/**
 * @author Tomas Muller
 */
public class SessionRestore {
    private static Log sLog = LogFactory.getLog(SessionBackup.class);
    private SessionFactory iHibSessionFactory = null;
	private org.hibernate.Session iHibSession = null;
	private Progress iProgress = null;
	private boolean iIsClone = false;

	private Map<String, Map<String, Entity>> iEntities = new Hashtable<String, Map<String, Entity>>();
	private List<Entity> iAllEntitites = new ArrayList<Entity>();
	private Map<String, Student> iStudents = new Hashtable<String, Student>();
	private PrintWriter iDebug = null;

	private InputStream iIn;

	public SessionRestore(InputStream input, Progress progress) {
		iIn = input;
        iProgress = progress;
	}
	
	public Progress getProgress() {
		return iProgress;
	}
	
	public void debug(PrintWriter pw) {
		iDebug = pw;
	}
	
	private boolean lookup(Entity entity, String property, Object value) {
		try {
			Object object = iHibSession.createCriteria(entity.getMetaData().getMappedClass()).add(Restrictions.eq(property, value)).uniqueResult();
			if (object != null)
				entity.setObject(object);
			else
				message("Lookup " + entity.getAbbv() + "." + property + " failed", (value == null ? "null" : value.toString()));
			return object != null;
		} catch (NonUniqueResultException e) {
			message("Lookup " + entity.getAbbv() + "." + property + "=" + value +" is not unique", entity.getId());
			List<Object> list = iHibSession.createCriteria(entity.getMetaData().getMappedClass()).add(Restrictions.eq(property, value)).list();
			if (!list.isEmpty()) {
				Object object = list.get(0);
				entity.setObject(object);
				return true;
			}
			return false;
		}
	}
	
	private void add(Entity entity) {
		boolean save = true;
		boolean lookup = true;
		if (entity.getObject() instanceof Session) {
			Session oldSession = (Session)iHibSession.get(Session.class, Long.valueOf(entity.getId()));
			if (oldSession != null) iIsClone = true;
			Session session = (Session)entity.getObject();
			int attempt = 0;
			while (!iHibSession.createCriteria(Session.class)
					.add(Restrictions.eq("academicInitiative", session.getAcademicInitiative() + (attempt == 0 ? "" : " [" + attempt + "]")))
					.add(Restrictions.eq("academicYear", session.getAcademicYear()))
					.add(Restrictions.eq("academicTerm", session.getAcademicTerm())).list().isEmpty()) {
				attempt ++;
			}
			if (attempt > 0)
				session.setAcademicInitiative(session.getAcademicInitiative() + " [" + attempt + "]");
		}
		if (entity.getObject() instanceof PreferenceLevel && lookup(entity, "prefProlog", ((PreferenceLevel)entity.getObject()).getPrefProlog())) save = false;
		if (entity.getObject() instanceof RefTableEntry && lookup(entity, "reference", ((RefTableEntry)entity.getObject()).getReference())) save = false;
		if (entity.getObject() instanceof TimetableManager && lookup(entity, "externalUniqueId", ((TimetableManager)entity.getObject()).getExternalUniqueId())) save = false;
		if (entity.getObject() instanceof ItypeDesc && lookup(entity, "itype", Integer.valueOf(entity.getId()))) save = false;
		if (entity.getObject() instanceof SolverInfoDef && lookup(entity, "name", ((SolverInfoDef)entity.getObject()).getName())) save = false;
		if (entity.getObject() instanceof SolverParameterGroup && lookup(entity, "name", ((SolverParameterGroup)entity.getObject()).getName())) save = false;
		if (entity.getObject() instanceof SolverPredefinedSetting && lookup(entity, "name", ((SolverPredefinedSetting)entity.getObject()).getName())) save = false;
		if (entity.getObject() instanceof Roles && lookup(entity, "reference", ((Roles)entity.getObject()).getReference())) save = false;
		if (entity.getObject() instanceof OfferingConsentType && lookup(entity, "label", ((OfferingConsentType)entity.getObject()).getLabel())) save = false;
		if (entity.getObject() instanceof ChangeLog) { save = false; lookup = false; }
		if (entity.getObject() instanceof OnlineSectioningLog) { save = false; lookup = false; }
		if (entity.getObject() instanceof Settings && lookup(entity, "key", ((Settings)entity.getObject()).getKey())) save = false;
		if (entity.getObject() instanceof EventContact && lookup(entity, "externalUniqueId", ((EventContact)entity.getObject()).getExternalUniqueId())) save = false;
		if (save)
			iAllEntitites.add(entity);
		if (lookup) {
			Map<String, Entity> entityOfThisType = iEntities.get(entity.getName());
			if (entityOfThisType == null) {
				entityOfThisType = new Hashtable<String, Entity>();
				iEntities.put(entity.getName(), entityOfThisType);
			}
			entityOfThisType.put(entity.getId(), entity);
		}
		if (entity.getObject() instanceof Student) {
			Student student = (Student)entity.getObject();
			iStudents.put(student.getExternalUniqueId(), student);
		}
	}
	
	public void create(TableData.Table table) throws InstantiationException, IllegalAccessException, DocumentException {
		ClassMetadata metadata = iHibSessionFactory.getClassMetadata(table.getName());
		iProgress.setPhase(metadata.getEntityName().substring(metadata.getEntityName().lastIndexOf('.') + 1) + " [" + table.getRecordCount() + "]", table.getRecordCount());
		for (TableData.Record record: table.getRecordList()) {
			iProgress.incProgress();
			Object object = metadata.getMappedClass().newInstance();
			for (String property: metadata.getPropertyNames()) {
				TableData.Element element = null;
				for (TableData.Element e: record.getElementList())
					if (e.getName().equals(property)) {
						element = e; break;
					}
				if (element == null) continue;
				Object value = null;
				Type type = metadata.getPropertyType(property);
				if (type instanceof PrimitiveType) {
					if (type instanceof BooleanType) {
						value = new Boolean("true".equals(element.getValue(0)));
					} else if (type instanceof ByteType) {
						value = Byte.valueOf(element.getValue(0));
					} else if (type instanceof CharacterType) {
						value = Character.valueOf(element.getValue(0).charAt(0));
					} else if (type instanceof DoubleType) {
						value = Double.valueOf(element.getValue(0));
					} else if (type instanceof FloatType) {
						value = Float.valueOf(element.getValue(0));
					} else if (type instanceof IntegerType) {
						value = Integer.valueOf(element.getValue(0));
					} else if (type instanceof LongType) {
						value = Long.valueOf(element.getValue(0));
					} else if (type instanceof ShortType) {
						value = Short.valueOf(element.getValue(0));
					}
				} else if (type instanceof DateType) {
					value = new DateType().fromStringValue(element.getValue(0));
				} else if (type instanceof TimestampType) {
					value = new TimestampType().fromStringValue(element.getValue(0));
				} else if (type instanceof StringType) {
					value = element.getValue(0);
				} else if (type instanceof BinaryType) {
					value = ByteString.copyFromUtf8(element.getValue(0)).toByteArray();
				} else if (type instanceof CustomType && type.getReturnedClass().equals(Document.class)) {
					value = new SAXReader().read(new StringReader(element.getValue(0)));
				} else if (type instanceof EntityType) {
				} else if (type instanceof CollectionType) {
				} else {
					message("Unknown type " + type.getClass().getName() + " (property " + metadata.getEntityName() + "." + property + ", class " + type.getReturnedClass() + ")", "");
				}
				if (value != null)
					metadata.setPropertyValue(object, property, value);
			}
			add(new Entity(metadata, record, object, record.getId()));
		}
	}
	
	Map<String, Set<String>> iMessages = new HashMap<String, Set<String>>();
	private void message(String message, String id) {
		Set<String> ids = iMessages.get(message);
		if (ids == null) {
			ids = new HashSet<String>();
			iMessages.put(message, ids);
		}
		if (ids.add(id) && ids.size() <= 5)
			iProgress.warn(message + (id.isEmpty() ? "" : ": " + id));
	}
	
	
	private Object checkUnknown(Class clazz, String id, Object object) {
		if (object == null)
			message("Unknown " + clazz.getName().substring(clazz.getName().lastIndexOf('.') + 1), id);
		return object;
	}
	
	private void printMessages() {
		for (String message: new TreeSet<String>(iMessages.keySet())) {
			Set<String> ids = new TreeSet<String>(iMessages.get(message));
			if (ids.isEmpty() || (ids.size() == 1 && ids.contains("")))
				iProgress.info(message);
			else {
				String list = "";
				int size = 0;
				for (String id: ids) {
					if (!list.isEmpty()) list += ", ";
					if (size > 20) { list += "... " + (ids.size() - size) + " more"; break; }
					list += id; size ++;
				}
				iProgress.info(message + ": " + list);
			}
		}
	}
	
	private Object get(Class clazz, String id) {
		if (clazz.equals(String.class) || clazz.equals(StringType.class)) return id;
		if (clazz.equals(Character.class) || clazz.equals(CharacterType.class)) return (id == null || id.isEmpty() ? null : id.charAt(0));
		if (clazz.equals(Byte.class) || clazz.equals(ByteType.class)) return Byte.valueOf(id);
		if (clazz.equals(Short.class) || clazz.equals(ShortType.class)) return Short.valueOf(id);
		if (clazz.equals(Integer.class) || clazz.equals(IntegerType.class)) return Integer.valueOf(id);
		if (clazz.equals(Long.class) || clazz.equals(LongType.class)) return Long.valueOf(id);
		if (clazz.equals(Float.class) || clazz.equals(FloatType.class)) return Float.valueOf(id);
		if (clazz.equals(Double.class) || clazz.equals(DoubleType.class)) return Double.valueOf(id);
		if (clazz.equals(Boolean.class) || clazz.equals(BooleanType.class)) return Boolean.valueOf(id);
		
		Map<String, Entity> entities = iEntities.get(clazz.getName());
		if (entities != null) {
			Entity entity = entities.get(id);
			if (entity != null) return entity.getObject();
		}
        for (Map.Entry<String, Map<String, Entity>> entry: iEntities.entrySet()) {
        	Entity o = entry.getValue().get(id);
        	if (o != null && clazz.isInstance(o.getObject())) return o.getObject();
		}
        if (clazz.equals(Session.class))
        	return ((Entity)iEntities.get(Session.class.getName()).values().iterator().next()).getObject();
        if (clazz.equals(Student.class))
        	return checkUnknown(clazz, id, iStudents.get(id));
        if (iIsClone)
        	return checkUnknown(clazz, id,
        			iHibSession.get(clazz, clazz.equals(ItypeDesc.class) ? (Serializable) Integer.valueOf(id) : (Serializable) Long.valueOf(id)));
        return checkUnknown(clazz, id, null);
	}
	
	private boolean fix(Entity entity) {
		if (entity.getObject() instanceof SolverParameterDef) {
			SolverParameterDef def = (SolverParameterDef)entity.getObject();
			TableData.Element element = entity.getElement("group");
			SolverParameterGroup group = (SolverParameterGroup)get(SolverParameterGroup.class, element.getValue(0));
			if (group != null && group.getUniqueId() != null) {
				List list = iHibSession.createCriteria(SolverParameterDef.class)
					.add(Restrictions.eq("name", def.getName()))
					.add(Restrictions.eq("group", group)).list();
				if (!list.isEmpty()) {
					if (list.size() > 1) 
						message("Multiple results for SolverParameterDef (name=" + def.getName() + ", group=" + group.getName() + ")", "");
					entity.setObject(list.get(0));
					return false;
				}
			}
		}
		if (entity.getObject() instanceof ManagerRole) {
			Roles role = (Roles)get(Roles.class, entity.getElement("role").getValue(0));
			TimetableManager manager = (TimetableManager)get(TimetableManager.class, entity.getElement("timetableManager").getValue(0));
			if (role.getRoleId() != null && manager.getUniqueId() != null) {
				Object object = iHibSession.createCriteria(ManagerRole.class)
					.add(Restrictions.eq("role", role))
					.add(Restrictions.eq("timetableManager", manager)).uniqueResult();
				if (object != null) {
					entity.setObject(object);
					return false;
				}
			}
		}
		return true;
	}
	
	
	public void restore() throws IOException, InstantiationException, IllegalAccessException, DocumentException {
        iHibSession = new _RootDAO().createNewSession(); 
        iHibSessionFactory = iHibSession.getSessionFactory();
        try {
            CodedInputStream cin = CodedInputStream.newInstance(iIn);
            cin.setSizeLimit(1024*1024*1024); // 1 GB
            
            iProgress.setPhase("Loading data", 1);
            TableData.Table t = null;
            while ((t = readTable(cin)) != null) {
        		if (iDebug != null) {
        			iDebug.println("## " + t.getName() + " ##");
        			iDebug.print(t.toString());
        			iDebug.flush();
        		}
            	create(t);
            }
            iProgress.incProgress();
            
    		iHibSession.setFlushMode(FlushMode.MANUAL);
    		iProgress.setPhase("Fixing", iAllEntitites.size());
    		for (Iterator<Entity> i = iAllEntitites.iterator(); i.hasNext(); ) {
    			iProgress.incProgress();
    			if (!fix(i.next())) i.remove();
    		}
    		
    		iProgress.setPhase("Saving (not-null)", iAllEntitites.size());
    		List<Entity> save = new ArrayList<Entity>(iAllEntitites);
    		boolean saved = true;
    		while (!save.isEmpty() && saved) {
    			saved = false;
    			for (Iterator<Entity> i = save.iterator(); i.hasNext(); ) {
    				Entity e = i.next();
    				if (e.canSave() == null) {
    					iProgress.incProgress();
    					e.fixRelationsNullOnly();
    					iHibSession.save(e.getObject());
    					i.remove();
    					saved = true;
    				}
    			}
    			iHibSession.flush();
    		}

    		iProgress.setPhase("Saving (all)", iAllEntitites.size());
    		for (Entity e: iAllEntitites) {
    			iProgress.incProgress();
    			String property = e.canSave();
    			if (property == null) {
    				e.fixRelations();
    				iHibSession.update(e.getObject());
    			} else {
    				message("Skipping " + e.getAbbv() + " (missing not-null relation " + property + ")", e.getId());
    				continue;
    			}
    		}
    		
    		iProgress.setPhase("Flush", 1);
    		iHibSession.flush();
    		iProgress.incProgress();
    		
    		printMessages();
    		
    		iProgress.setStatus("All done.");
        } finally {
        	iHibSession.close();
        }
	}
	
	class Entity {
		private ClassMetadata iMetaData;
		private TableData.Record iRecord;
		private Object iObject;
		private String iId;
		
		Entity(ClassMetadata metadata, TableData.Record record, Object object, String id) {
			iMetaData = metadata;
			iRecord = record;
			iObject = object;
			iId = id;
		}
		
		public ClassMetadata getMetaData() { return iMetaData; }
		public String getName() { return getMetaData().getEntityName(); }
		public String getAbbv() { return getName().substring(getName().lastIndexOf('.') + 1); }
		public Object getObject() { return iObject; }
		public void setObject(Object object) { iObject = object; }
		public String getId() { return iId; }
		public TableData.Record getRecord() { return iRecord; }
		public TableData.Element getElement(String property) {
			for (TableData.Element e: getRecord().getElementList())
				if (e.getName().equals(property))
					return e;
			return null;
		}
		
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Entity)) return false;
			Entity e = (Entity)o;
			return getName().equals(e.getName()) && getId().equals(e.getId());
		}
		
		public int hashCode() {
			return getId().hashCode();
		}
		
		public String toString() {
			return getAbbv() + "@" + getId();
		}
		
		public String canSave() {
			for (int i = 0; i < getMetaData().getPropertyNames().length; i++) {
				if (getMetaData().getPropertyNullability()[i]) continue;
				Type type = getMetaData().getPropertyTypes()[i];
				if (type instanceof EntityType) {
					TableData.Element element = getElement(getMetaData().getPropertyNames()[i]);
					if (element == null) continue;
					Object value = get(type.getReturnedClass(), element.getValue(0));
					if (value == null || !iHibSession.contains(value)) return getMetaData().getPropertyNames()[i];
				}
			}
			return null;
		}
		
		public void fixRelationsNullOnly() {
			for (int i = 0; i < getMetaData().getPropertyNames().length; i++) {
				String property = getMetaData().getPropertyNames()[i];
				if (getMetaData().getPropertyNullability()[i]) continue;
				Type type = getMetaData().getPropertyTypes()[i];
				if (type instanceof EntityType) {
					TableData.Element element = getElement(getMetaData().getPropertyNames()[i]);
					if (element == null) continue;
					Object value = get(type.getReturnedClass(), element.getValue(0));
					if (value != null) {
						if (!iHibSession.contains(value))
							message("Required " + getAbbv() + "." + property + " has transient value", getId() + "-" + element.getValue(0));
						else
							getMetaData().setPropertyValue(getObject(), getMetaData().getPropertyNames()[i], value);
					}
				}
			}
		}
		
		public void fixRelations() {
			for (int i = 0; i < getMetaData().getPropertyNames().length; i++) {
				String property = getMetaData().getPropertyNames()[i];
				if (!getMetaData().getPropertyNullability()[i]) continue;
				Type type = getMetaData().getPropertyType(property);
				if (type instanceof EntityType) {
					TableData.Element element = getElement(property);
					if (element == null) continue;
					Object value = get(type.getReturnedClass(), element.getValue(0));
					if (value != null) {
						if (!iHibSession.contains(value))
							message("Optional " + getAbbv() + "." + property + " has transient value", getId() + "-" + element.getValue(0));
						else
							getMetaData().setPropertyValue(getObject(), property, value);
					}
				} else if (type instanceof CollectionType) {
					TableData.Element element = getElement(property);
					if (element == null) continue;
					Class clazz = ((CollectionType)type).getElementType((SessionFactoryImplementor)iHibSessionFactory).getReturnedClass();
					if (type instanceof SetType) {
						Set<Object> set = new HashSet<Object>();
						for (String id: element.getValueList()) {
							Object v = get(clazz, id);
							if (v != null) {
								if (!iHibSession.contains(v)) 
									message("Collection " + getAbbv() + "." + property + " has transient value", getId() + "-" + id);
								else
									set.add(v);
							}
						}
						getMetaData().setPropertyValue(getObject(), property, set);
					} else if (type instanceof ListType) {
						List<Object> set = new ArrayList<Object>();
						for (String id: element.getValueList()) {
							Object v = get(clazz, id);
							if (v != null) {
								if (!iHibSession.contains(v))
									message("Collection " + getAbbv() + "." + property + " has transient value", getId() + "-" + id);
								else
									set.add(v);
							}
						}
						getMetaData().setPropertyValue(getObject(), property, set);
					} else {
						message("Unimplemented collection type: " + type.getClass().getName() + " (" + getAbbv() + "." + property + ")", "");
					}
				}
			}
			if (getObject() instanceof ExamOwner) {
				ExamOwner owner = (ExamOwner)getObject();
				switch (owner.getOwnerType()) {
				case ExamOwner.sOwnerTypeClass:
					owner.setOwnerId(((Class_)get(Class_.class, owner.getOwnerId().toString())).getUniqueId());
					break;
				case ExamOwner.sOwnerTypeConfig:
					owner.setOwnerId(((InstrOfferingConfig)get(InstrOfferingConfig.class, owner.getOwnerId().toString())).getUniqueId());
					break;
				case ExamOwner.sOwnerTypeCourse:
					owner.setOwnerId(((CourseOffering)get(CourseOffering.class, owner.getOwnerId().toString())).getUniqueId());
					break;
				case ExamOwner.sOwnerTypeOffering:
					owner.setOwnerId(((InstructionalOffering)get(InstructionalOffering.class, owner.getOwnerId().toString())).getUniqueId());
					break;
				}
			}
			if (getObject() instanceof RelatedCourseInfo) {
				RelatedCourseInfo owner = (RelatedCourseInfo)getObject();
				switch (owner.getOwnerType()) {
				case ExamOwner.sOwnerTypeClass:
					owner.setOwnerId(((Class_)get(Class_.class, owner.getOwnerId().toString())).getUniqueId());
					break;
				case ExamOwner.sOwnerTypeConfig:
					owner.setOwnerId(((InstrOfferingConfig)get(InstrOfferingConfig.class, owner.getOwnerId().toString())).getUniqueId());
					break;
				case ExamOwner.sOwnerTypeCourse:
					owner.setOwnerId(((CourseOffering)get(CourseOffering.class, owner.getOwnerId().toString())).getUniqueId());
					break;
				case ExamOwner.sOwnerTypeOffering:
					owner.setOwnerId(((InstructionalOffering)get(InstructionalOffering.class, owner.getOwnerId().toString())).getUniqueId());
					break;
				}
			}
		}
	}
	
	public static TableData.Table readTable(CodedInputStream cin) throws IOException {
		if (cin.isAtEnd()) return null;
		int size = cin.readInt32();
		int limit = cin.pushLimit(size);
		TableData.Table ret = TableData.Table.parseFrom(cin);
		cin.popLimit(limit);
		cin.resetSizeCounter();
		return ret;
	}
	
	public static void main(String[] args) {
		try {
            Properties props = new Properties();
            props.setProperty("log4j.rootLogger", "DEBUG, A1");
            props.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
            props.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
            props.setProperty("log4j.appender.A1.layout.ConversionPattern","%-5p %m%n");
            props.setProperty("log4j.logger.org.hibernate","INFO");
            props.setProperty("log4j.logger.org.hibernate.cfg","WARN");
            props.setProperty("log4j.logger.org.hibernate.cache.EhCacheProvider","ERROR");
            props.setProperty("log4j.logger.org.unitime.commons.hibernate","INFO");
            props.setProperty("log4j.logger.net","INFO");
            PropertyConfigurator.configure(props);
            
            HibernateUtil.configureHibernate(ApplicationProperties.getProperties());

            FileInputStream in = new FileInputStream(args[0]);
            
            SessionRestore restore = new SessionRestore(in, Progress.getInstance());
            
            PrintWriter debug = null;
            if (args.length >= 2) {
            	debug = new PrintWriter(args[1]);
            	restore.debug(debug);
            }

            restore.getProgress().addProgressListener(new ProgressWriter(System.out));

            restore.restore();
            
            in.close();
            if (debug != null) debug.close();
            
            HibernateUtil.closeHibernate();
            
		} catch (Exception e) {
			sLog.fatal("Backup failed: " + e.getMessage(), e);
		}
	}

}
