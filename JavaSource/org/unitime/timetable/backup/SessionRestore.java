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
package org.unitime.timetable.backup;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.cpsolver.ifs.util.Progress;
import org.cpsolver.ifs.util.ProgressWriter;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.hibernate.CacheMode;
import org.hibernate.EntityMode;
import org.hibernate.FlushMode;
import org.hibernate.NonUniqueResultException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.BinaryType;
import org.hibernate.type.BooleanType;
import org.hibernate.type.ByteType;
import org.hibernate.type.CharacterType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ComponentType;
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
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequestOption;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.EventServiceProvider;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.model.OnlineSectioningLog;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RefTableEntry;
import org.unitime.timetable.model.RelatedCourseInfo;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.SolverInfo;
import org.unitime.timetable.model.SolverInfoDef;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAreaClassificationMajor;
import org.unitime.timetable.model.StudentAreaClassificationMinor;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.TravelTime;
import org.unitime.timetable.model.dao._RootDAO;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * @author Tomas Muller
 */
public class SessionRestore implements SessionRestoreInterface {
    private static Log sLog = LogFactory.getLog(SessionBackup.class);
    private SessionFactory iHibSessionFactory = null;
	private org.hibernate.Session iHibSession = null;
	private BackupProgress iProgress = null;
	private boolean iIsClone = false;

	private Map<String, Map<String, Entity>> iEntities = new Hashtable<String, Map<String, Entity>>();
	private List<Entity> iAllEntitites = new ArrayList<Entity>();
	private Map<String, Student> iStudents = new Hashtable<String, Student>();
	private PrintWriter iDebug = null;
	private Map<String, TableData.Table> iSkippedTables = new Hashtable<String, TableData.Table>();

	private InputStream iIn;

	public BackupProgress getProgress() {
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
	
	protected void add(Entity entity) {
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
		if (entity.getObject() instanceof EventServiceProvider && entity.getElement("session") == null && lookup(entity, "reference", ((EventServiceProvider)entity.getObject()).getReference())) save = false;
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
	
	protected Entity lookupSkippedRecord(String tableName, String id) {
		TableData.Table table = iSkippedTables.get(tableName);
		if (table == null) return null;
		for (TableData.Record record: table.getRecordList()) {
			if (id.equals(record.getId()) && record.getElementCount() > 0) return new Entity(null, record, null, id);
		}
		return null;
	}
	
	public void create(TableData.Table table) throws InstantiationException, IllegalAccessException, DocumentException {
		ClassMetadata metadata = iHibSessionFactory.getClassMetadata(table.getName());
		if (metadata == null) {
			iSkippedTables.put(table.getName(), table);
			return;
		}
		PersistentClass mapping = _RootDAO.getConfiguration().getClassMapping(table.getName());
		Map<String, Integer> lengths = new HashMap<String, Integer>();
		for (String property: metadata.getPropertyNames()) {
			if ("org.unitime.timetable.model.CurriculumClassification.students".equals(metadata.getEntityName() + "." + property)) continue;
			Type type = metadata.getPropertyType(property);
			if (type instanceof StringType)
				for (Iterator<?> i = mapping.getProperty(property).getColumnIterator(); i.hasNext(); ) {
					Object o = i.next();
					if (o instanceof Column) {
						Column column = (Column)o;
						lengths.put(property, column.getLength());
					}
					break;
				}
		}
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
					try {
						value = new SimpleDateFormat("dd MMMM yyyy", Localization.getJavaLocale()).parse(element.getValue(0));
					} catch (ParseException e) {
						value  = new DateType().fromStringValue(element.getValue(0));
					}
				} else if (type instanceof TimestampType) {
					value = new TimestampType().fromStringValue(element.getValue(0));
				} else if (type instanceof StringType) {
					value = element.getValue(0);
					Integer len = lengths.get(property);
					if (len != null && value.toString().length() > len) {
						message("Value is  too long, truncated (property " + metadata.getEntityName() + "." + property +", length " + len +")", record.getId());
						value = value.toString().substring(0, len);
					}
				} else if (type instanceof BinaryType) {
					value = element.getValueBytes(0).toByteArray();
				} else if (type instanceof CustomType && type.getReturnedClass().equals(Document.class)) {
					value = new SAXReader().read(new StringReader(element.getValue(0)));
				} else if (type instanceof EntityType) {
				} else if (type instanceof CollectionType) {
				} else {
					message("Unknown type " + type.getClass().getName() + " (property " + metadata.getEntityName() + "." + property + ", class " + type.getReturnedClass() + ")", record.getId());
				}
				if (value != null)
					metadata.setPropertyValue(object, property, value);
			}
			add(new Entity(metadata, record, object, record.getId()));
		}
	}
	
	Map<String, Set<String>> iMessages = new HashMap<String, Set<String>>();
	protected void message(String message, String id) {
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
	
	protected Entity getEntity(Class clazz, String id) {
		Map<String, Entity> entities = iEntities.get(clazz.getName());
		if (entities != null) {
			return entities.get(id);
		}
		return null;
	}
	
	protected Object get(Class clazz, String id) {
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
		if (entity.getObject() instanceof Department) {
			Department department = (Department)entity.getObject();
			if (department.isInheritInstructorPreferences() == null)
				department.setInheritInstructorPreferences(!department.isExternalManager());
		}
		if (entity.getObject() instanceof ItypeDesc) {
			ItypeDesc itype = (ItypeDesc)entity.getObject();
			itype.setItype(new Integer(entity.getId()));
		}
		return true;
	}
	
	
	public void restore(InputStream input, BackupProgress progress) throws IOException, InstantiationException, IllegalAccessException, DocumentException {
		iIn = input;
        iProgress = progress;
        iHibSession = new _RootDAO().createNewSession();
        iHibSession.setCacheMode(CacheMode.IGNORE);
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
    		List<Object> otherObjectsToSave = new ArrayList<Object>();
    		boolean saved = true;
    		while (!save.isEmpty() && saved) {
    			saved = false;
    			for (Iterator<Entity> i = save.iterator(); i.hasNext(); ) {
    				Entity e = i.next();
    				if (e.canSave() == null) {
    					iProgress.incProgress();
    					e.fixRelationsNullOnly(otherObjectsToSave);
    					iHibSession.save(e.getObject());
    					i.remove();
    					saved = true;
    				}
    			}
    			iHibSession.flush();
    		}
    		for (Object object: otherObjectsToSave)
    			iHibSession.save(object);
    		otherObjectsToSave.clear();
    		iHibSession.flush();

    		iProgress.setPhase("Saving (all)", iAllEntitites.size());
    		for (Entity e: iAllEntitites) {
    			iProgress.incProgress();
    			String property = e.canSave();
    			if (property == null) {
    				e.fixRelations(otherObjectsToSave);
    				iHibSession.update(e.getObject());
    			} else {
    				message("Skipping " + e.getAbbv() + " (missing not-null relation " + property + ")", e.getId());
    				continue;
    			}
    		}
    		for (Object object: otherObjectsToSave)
    			iHibSession.save(object);
    		otherObjectsToSave.clear();
    		
    		iProgress.setPhase("Flush", 1);
    		iHibSession.flush();
    		iProgress.incProgress();
    		
    		printMessages();
    		
    		iProgress.setStatus("All done.");
        } finally {
        	iHibSession.close();
        }
	}
	
	protected class Entity {
		private ClassMetadata iMetaData;
		private TableData.Record iRecord;
		private Object iObject;
		private String iId;
		
		protected Entity(ClassMetadata metadata, TableData.Record record, Object object, String id) {
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
			if (getMetaData().getIdentifierType().isComponentType()) {
				ComponentType cid = (ComponentType)getMetaData().getIdentifierType();
				for (int i = 0; i < cid.getPropertyNames().length; i++) {
					Type type = cid.getSubtypes()[i];
					if (type instanceof EntityType) {
						TableData.Element element = getElement(cid.getPropertyNames()[i]);
						if (element == null) continue;
						Object value = get(type.getReturnedClass(), element.getValue(0));
						if (value == null || !iHibSession.contains(value)) return cid.getPropertyNames()[i];
					}
				}
			}
			return null;
		}
		
		public void fixRelationsNullOnly(List<Object> otherObjectsToSave) {
			fixRelationsNullOnly();
			if (getObject() instanceof InstructionalOffering) {
				TableData.Element element = getElement("coordinators");
				if (element != null) {
					InstructionalOffering io = (InstructionalOffering)getObject();
					for (int i = 0; i < element.getValueCount(); i++) {
						DepartmentalInstructor instructor = (DepartmentalInstructor)get(DepartmentalInstructor.class, element.getValue(i));
						if (instructor != null) {
							OfferingCoordinator oc = new OfferingCoordinator();
							oc.setInstructor(instructor); oc.setOffering(io); oc.setPercentShare(0);
							io.addToofferingCoordinators(oc);
							otherObjectsToSave.add(oc);
						}
					}
				}
			}
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
			if (getMetaData().getIdentifierType().isComponentType()) {
				ComponentType cid = (ComponentType)getMetaData().getIdentifierType();
				Object[] id = new Object[cid.getPropertyNames().length];
				for (int i = 0; i < cid.getPropertyNames().length; i++) {
					String property = cid.getPropertyNames()[i];
					Type type = cid.getSubtypes()[i];
					TableData.Element element = getElement(cid.getPropertyNames()[i]);
					if (element == null) continue;
					Object value = null;
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
						try {
							value = new SimpleDateFormat("dd MMMM yyyy", Localization.getJavaLocale()).parse(element.getValue(0));
						} catch (ParseException e) {
							value  = new DateType().fromStringValue(element.getValue(0));
						}
					} else if (type instanceof TimestampType) {
						value = new TimestampType().fromStringValue(element.getValue(0));
					} else if (type instanceof StringType) {
						value = element.getValue(0);
					} else if (type instanceof BinaryType) {
						value = element.getValueBytes(0).toByteArray();
					} else if (type instanceof EntityType) {
						value = get(type.getReturnedClass(), element.getValue(0));
						if (value != null && !iHibSession.contains(value))
							message("Required " + getAbbv() + "." + property + " has transient value", getId() + "-" + element.getValue(0));
					} else {
						message("Not-supported composite key type " + type.getClass().getName() + " (property " + getMetaData().getEntityName() + "." + property + ", class " + type.getReturnedClass() + ")", getId());
					}
					id[i] = value;
				}
				cid.setPropertyValues(getObject(), id, EntityMode.POJO);
			}
			if (getObject() instanceof Class_) {
				Class_ clazz = (Class_)getObject();
				if (clazz.getCancelled() == null) clazz.setCancelled(false);
			}
			if (getObject() instanceof Curriculum) {
				Curriculum curriculum = (Curriculum)getObject();
				if (curriculum.isMultipleMajors() == null) curriculum.setMultipleMajors(false);
			}
			if (getObject() instanceof ExamType) {
				ExamType type = (ExamType)getObject();
				if (type.getHighlightInEvents() == null) type.setHighlightInEvents(type.getType() == ExamType.sExamTypeFinal);
			}
			if (getObject() instanceof SolverInfo) {
				SolverInfo info = (SolverInfo)getObject();
				TableData.Element element = getElement("data");
				if (element != null) { // use byte[] type
					info.setData(element.getValueBytes(0).toByteArray());
				} else { // fall back to XML
					element = getElement("value");
					try {
						Document value = new SAXReader().read(new StringReader(element.getValue(0)));
						info.setValue(value);
					} catch (DocumentException e) {
						sLog.warn("Failed to parse solver info for " + getId() + ": " + e.getMessage());
					}
				}
			}
			if (getObject() instanceof Department) {
				Department dept = (Department)getObject();
				if (dept.isAllowStudentScheduling() == null) dept.setAllowStudentScheduling(true);
			}
		}
		
		public void fixRelations(List<Object> otherObjectsToSave) {
			fixRelations();
			if (getObject() instanceof Student) {
				TableData.Element ac = getElement("academicAreaClassifications");
				TableData.Element mj = getElement("posMajors");
				TableData.Element mn = getElement("posMinors");
				if (ac != null && mj != null) {
					Student student = (Student)getObject();
					for (String majorId: mj.getValueList()) {
						Entity majorEntity = getEntity(PosMajor.class, majorId);
						if (majorEntity == null) continue;
						List<String> areaIds = majorEntity.getElement("academicAreas").getValueList();
						PosMajor major = (PosMajor)majorEntity.getObject();
						for (String aacId: ac.getValueList()) {
							Entity aac = lookupSkippedRecord("org.unitime.timetable.model.AcademicAreaClassification", aacId);
							if (aac == null) continue;
							String areaId = aac.getElement("academicArea").getValue(0);
							String clasfId = aac.getElement("academicClassification").getValue(0);
							if (!areaIds.contains(areaId)) continue;
							AcademicArea area = (AcademicArea)get(AcademicArea.class, areaId);
							AcademicClassification clasf = (AcademicClassification)get(AcademicClassification.class, clasfId);
							if (area != null && clasf != null) {
								StudentAreaClassificationMajor acm = new StudentAreaClassificationMajor();
								acm.setAcademicArea(area);
								acm.setStudent(student);
								acm.setAcademicClassification(clasf);
								acm.setMajor(major);
								student.addToareaClasfMajors(acm);
								otherObjectsToSave.add(acm);
							}
						}
					}
				}
				if (ac != null && mn != null) {
					Student student = (Student)getObject();
					for (String minorId: mn.getValueList()) {
						Entity minorEntity = getEntity(PosMinor.class, minorId);
						if (minorEntity == null) continue;
						List<String> areaIds = minorEntity.getElement("academicAreas").getValueList();
						PosMinor minor = (PosMinor)minorEntity.getObject();
						for (String aacId: ac.getValueList()) {
							Entity aac = lookupSkippedRecord("org.unitime.timetable.model.AcademicAreaClassification", aacId);
							if (aac == null) continue;
							String areaId = aac.getElement("academicArea").getValue(0);
							String clasfId = aac.getElement("academicClassification").getValue(0);
							if (!areaIds.contains(areaId)) continue;
							AcademicArea area = (AcademicArea)get(AcademicArea.class, areaId);
							AcademicClassification clasf = (AcademicClassification)get(AcademicClassification.class, clasfId);
							if (area != null && clasf != null) {
								StudentAreaClassificationMinor acm = new StudentAreaClassificationMinor();
								acm.setAcademicArea(area);
								acm.setStudent(student);
								acm.setAcademicClassification(clasf);
								acm.setMinor(minor);
								student.addToareaClasfMinors(acm);
								otherObjectsToSave.add(acm);
							}
						}
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
					if (element == null || element.getValueCount() == 0) continue;
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
					Type elementType =((CollectionType)type).getElementType((SessionFactoryImplementor)iHibSessionFactory);
					Class clazz = elementType.getReturnedClass();
					if (type instanceof SetType) {
						Set<Object> set = new HashSet<Object>();
						for (String id: element.getValueList()) {
							Object v = get(clazz, id);
							if (v != null) {
								if (elementType instanceof EntityType && !iHibSession.contains(v)) 
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
								if (elementType instanceof EntityType && !iHibSession.contains(v))
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
			if (getObject() instanceof TravelTime) {
				TravelTime tt = (TravelTime)getObject();
				Location l1 = (Location)get(Location.class, tt.getLocation1Id().toString());
				if (l1 != null)
					tt.setLocation1Id(l1.getUniqueId());
				Location l2 = (Location)get(Location.class, tt.getLocation2Id().toString());
				if (l2 != null)
					tt.setLocation2Id(l2.getUniqueId());
			}
			if (getObject() instanceof Location) {
				Location loc = (Location)getObject();
				if (loc.getManagerIds() != null) {
					String managerIds = null;
					for (StringTokenizer stk = new StringTokenizer(loc.getManagerIds(), ","); stk.hasMoreTokens();) {
						Department dept = (Department)get(Department.class, stk.nextToken());
						if (dept != null) {
							if (managerIds == null)
								managerIds = dept.getUniqueId().toString();
							else
								managerIds += "," + dept.getUniqueId();
						}
					}
					loc.setManagerIds(managerIds);
				}
			}
			if (getObject() instanceof CourseRequestOption) {
				CourseRequestOption o = (CourseRequestOption)getObject();
				try {
					org.unitime.timetable.onlinesectioning.OnlineSectioningLog.CourseRequestOption.Builder option = org.unitime.timetable.onlinesectioning.OnlineSectioningLog.CourseRequestOption.parseFrom(o.getValue()).toBuilder();
					if (option.getInstructionalMethodCount() > 0)
						for (org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Entity.Builder e: option.getInstructionalMethodBuilderList()) {
							InstructionalMethod im = InstructionalMethod.findByReference(e.getExternalId(), iHibSession);
							if (im != null) e.setUniqueId(im.getUniqueId());
						}
					if (option.getSectionCount() > 0)
						for (org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Section.Builder e: option.getSectionBuilderList()) {
							Class_ clazz = (Class_)get(Class_.class, String.valueOf(e.getClazzBuilder().getUniqueId()));
							if (clazz != null) {
								e.getClazzBuilder().setUniqueId(clazz.getUniqueId());
								e.getSubpartBuilder().setUniqueId(clazz.getSchedulingSubpart().getUniqueId());
							}
							if (e.hasCourse()) {
								CourseOffering course = (CourseOffering)get(CourseOffering.class, String.valueOf(e.getCourseBuilder().getUniqueId()));
								if (course != null)
									e.getCourseBuilder().setUniqueId(course.getUniqueId());
							}
							if (e.getInstructorCount() > 0) {
								for (org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Entity.Builder f: e.getInstructorBuilderList()) {
									DepartmentalInstructor instructor = (DepartmentalInstructor)get(DepartmentalInstructor.class, String.valueOf(f.getUniqueId()));
									if (instructor != null)
										f.setUniqueId(instructor.getUniqueId());
								}
							}
							if (e.getLocationCount() > 0) {
								for (org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Entity.Builder f: e.getLocationBuilderList()) {
									Location location = (Location)get(Location.class, String.valueOf(f.getUniqueId()));
									if (location != null)
										f.setUniqueId(location.getUniqueId());
								}
							}
						}
					o.setValue(option.build().toByteArray());
				} catch (InvalidProtocolBufferException e) {}
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
            
            sLog.info("Using " + ApplicationProperty.SessionRestoreInterface.value());
            SessionRestore restore = (SessionRestore)Class.forName(ApplicationProperty.SessionRestoreInterface.value()).newInstance();
            
            PrintWriter debug = null;
            if (args.length >= 2) {
            	debug = new PrintWriter(args[1]);
            	restore.debug(debug);
            }

            final Progress progress = Progress.getInstance();
            progress.addProgressListener(new ProgressWriter(System.out));

            restore.restore(in, new BackupProgress() {
            	@Override
				public void setStatus(String status) {
					progress.setStatus(status);
				}
				
				@Override
				public void setPhase(String phase, double max) {
					progress.setPhase(phase, Math.round(max));
				}
				
				@Override
				public void incProgress() {
					progress.incProgress();
				}
				
				@Override
				public void info(String message) {
					progress.info(message);
				}
				
				@Override
				public void warn(String message) {
					progress.warn(message);
				}

				@Override
				public void error(String message) {
					progress.error(message);
				}
			});
            
            in.close();
            if (debug != null) debug.close();
            
            HibernateUtil.closeHibernate();
            
		} catch (Exception e) {
			sLog.fatal("Backup failed: " + e.getMessage(), e);
		}
	}

}
