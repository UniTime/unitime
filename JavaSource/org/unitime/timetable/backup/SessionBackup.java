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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.ifs.util.Progress;
import org.cpsolver.ifs.util.ProgressWriter;
import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.CacheMode;
import org.hibernate.UnknownEntityTypeException;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.AssignmentInfo;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ConstraintInfo;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.LastLikeCourseDemand;
import org.unitime.timetable.model.OnlineSectioningLog;
import org.unitime.timetable.model.PitCourseOffering;
import org.unitime.timetable.model.PitDepartmentalInstructor;
import org.unitime.timetable.model.PitStudentAcadAreaMajorClassification;
import org.unitime.timetable.model.PitStudentAcadAreaMinorClassification;
import org.unitime.timetable.model.PointInTimeData;
import org.unitime.timetable.model.SectioningSolutionLog;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolutionInfo;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao._RootDAO;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedOutputStream;

/**
 * @author Tomas Muller
 */
public class SessionBackup implements SessionBackupInterface {
    private static Log sLog = LogFactory.getLog(SessionBackup.class);
	private org.hibernate.Session iHibSession = null;
	
	private CodedOutputStream iOut = null;
	private PrintWriter iDebug = null;
	private Long iSessionId = null;
	private BackupProgress iProgress = null;
	private Metamodel iMetamodel = null;
	private SimpleDateFormat iDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	public BackupProgress getProgress() {
		return iProgress;
	}
	
	private void add(TableData.Table table) throws IOException {
		iProgress.info("Writing " + table.getName().substring(table.getName().lastIndexOf('.') + 1) + " [" + table.getRecordCount() + " records, " + table.getSerializedSize() + " bytes]");
		iOut.writeInt32NoTag(table.getSerializedSize());
		table.writeTo(iOut);
		iOut.flush();
		if (iDebug != null) {
			iDebug.println("## " + table.getName() + " ##");
			iDebug.print(table.toString());
			iDebug.flush();
		}
	}
	
	public void debug(PrintWriter pw) {
		iDebug = pw;
	}
	
	private boolean hasSubclasses(EntityType type) {
		for (EntityType et: iMetamodel.getEntities()) {
			if (et.equals(type)) continue;
			if (type.getJavaType().isAssignableFrom(et.getJavaType())) return true;
		}
		return false;
	}
	
	private boolean isNullable(Attribute attribute) {
		return attribute instanceof SingularAttribute && ((SingularAttribute)attribute).isOptional(); 
	}
	
	private boolean isEntity(Attribute attribute) {
		if (attribute.isCollection()) return false;
		try {
			return iMetamodel.entity(attribute.getJavaType()) != null;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
	
	private boolean hasCompositeId(EntityType meta) {
		return !meta.hasSingleIdAttribute();
	}
	
	private SingularAttribute getIdAttribute(EntityType meta) {
		if (meta.hasSingleIdAttribute()) {
			return meta.getId(meta.getIdType().getJavaType());
		} else {
			return null;
		}
	}
	
	private List<SingularAttribute> getIdAttributes(EntityType meta) {
		if (meta.hasSingleIdAttribute()) {
			List<SingularAttribute> ret = new ArrayList<SingularAttribute>(1);
			ret.add(meta.getId(meta.getIdType().getJavaType()));
			return ret;
		} else {
			List<SingularAttribute> ret = new ArrayList<SingularAttribute>(meta.getIdClassAttributes().size());
			ret.addAll(meta.getIdClassAttributes());
			return ret;
		}
	}
	
	private Object getProperty(Object object, Attribute attribute) {
		try {
			return ((Method)attribute.getJavaMember()).invoke(object);
		} catch (Exception e) {
			iProgress.warn("Failed to get " + attribute + ": " + e.getMessage());
			return null;
		}
	}
	
	@Override
	public void backup(OutputStream out, BackupProgress progress, Long sessionId) throws IOException {
        iOut = CodedOutputStream.newInstance(out);
        iProgress = progress;
		iSessionId = sessionId;
        iHibSession = new _RootDAO().createNewSession(); 
        iHibSession.setCacheMode(CacheMode.IGNORE);
        iMetamodel = iHibSession.getMetamodel();
        try {
    		iProgress.setStatus("Exporting Session");
    		iProgress.setPhase("Loading Model", 3);
    		TreeSet<EntityType> allMeta = new TreeSet<EntityType>(new Comparator<EntityType>() {
    			@Override
    			public int compare(EntityType m1, EntityType m2) {
    				return m1.getName().compareTo(m2.getName());
    			}
    		});
    		allMeta.addAll(iMetamodel.getEntities());

    		iProgress.incProgress();
    		
    		Queue<QueueItem> queue = new LinkedList<QueueItem>();
    		
    		queue.add(new QueueItem(iMetamodel.entity(Session.class), null, "uniqueId", Relation.None));

    		Set<String> avoid = new HashSet<String>();
    		// avoid following relations
    		avoid.add(TimetableManager.class.getName() + ".departments");
    		avoid.add(TimetableManager.class.getName() + ".solverGroups");
    		avoid.add(TimetableManager.class.getName() + ".settings");
    		avoid.add(DistributionType.class.getName() + ".departments");
    		avoid.add(LastLikeCourseDemand.class.getName() + ".student");
    		avoid.add(Student.class.getName() + ".lastLikeCourseDemands");
    		String pAvoid = ApplicationProperty.SessionBackupAvoid.value();
    		if (pAvoid != null && !pAvoid.isEmpty())
    			for (String av: pAvoid.split("[\n,;]"))
    				if (!av.isEmpty()) avoid.add(av.trim());
    		
    		Set<String> disallowedNotNullRelations = new HashSet<String>();
    		disallowedNotNullRelations.add(Assignment.class.getName() + ".datePattern");
    		disallowedNotNullRelations.add(Assignment.class.getName() + ".timePattern");
    		disallowedNotNullRelations.add(LastLikeCourseDemand.class.getName() + ".student");
    		disallowedNotNullRelations.add(OnlineSectioningLog.class.getName() + ".session");
    		disallowedNotNullRelations.add(PointInTimeData.class.getName() + ".session");
    		disallowedNotNullRelations.add(SectioningSolutionLog.class.getName() + ".session");
    		if (ApplicationProperty.SessionBackupPointInTime.isFalse()) {
        		disallowedNotNullRelations.add(PitStudentAcadAreaMajorClassification.class.getName() + ".academicArea");
        		disallowedNotNullRelations.add(PitStudentAcadAreaMajorClassification.class.getName() + ".academicClassification");
        		disallowedNotNullRelations.add(PitStudentAcadAreaMajorClassification.class.getName() + ".major");
        		disallowedNotNullRelations.add(PitStudentAcadAreaMinorClassification.class.getName() + ".academicArea");
        		disallowedNotNullRelations.add(PitStudentAcadAreaMinorClassification.class.getName() + ".academicClassification");
        		disallowedNotNullRelations.add(PitStudentAcadAreaMinorClassification.class.getName() + ".minor");
        		disallowedNotNullRelations.add(PitDepartmentalInstructor.class.getName() + ".department");
        		disallowedNotNullRelations.add(PitCourseOffering.class.getName() + ".subjectArea");
    		}
    		String pDisallowed = ApplicationProperty.SessionBackupDisallowed.value();
    		if (pDisallowed != null && !pDisallowed.isEmpty())
    			for (String dis: pDisallowed.split("[\n,;]"))
    				if (!dis.isEmpty()) disallowedNotNullRelations.add(dis.trim());
    		
    		Map<String, List<QueueItem>> data = new HashMap<String, List<QueueItem>>();
    		List<QueueItem> sessions = new ArrayList<QueueItem>();
    		sessions.add(queue.peek());
    		data.put(queue.peek().name(), sessions);
    		
    		QueueItem item = null;
            while ((item = queue.poll()) != null) {
            	if (item.size() == 0) continue;
            	for (EntityType meta: allMeta) {
                	if (hasSubclasses(meta)) continue;
                	for (Attribute attribute: (Set<Attribute>)meta.getAttributes()) {
                		String property = attribute.getName();
                    	if (disallowedNotNullRelations.contains(meta.getJavaType().getName() + "." + property) || isNullable(attribute)) continue;
                    	if (isEntity(attribute) && attribute.getJavaType().equals(item.clazz())) {
                    		QueueItem qi = new QueueItem(meta, item, property, Relation.Parent);
                    		if (!data.containsKey(qi.name())) {
                    			List<QueueItem> items = new ArrayList<QueueItem>();
                    			data.put(qi.name(), items);
                    			queue.add(qi);
                    			items.add(qi);
                        		if (qi.size() > 0)
                        			iProgress.info("Parent: " + qi);
                    		}
                    	}
                    }
                }
            }
            iProgress.incProgress();
    		
            for (List<QueueItem> list: data.values())
            	queue.addAll(list);
            
            // The following part is needed to ensure that instructor distribution preferences are saved including their distribution types 
            List<QueueItem> distributions = new ArrayList<QueueItem>();
            for (QueueItem instructor: data.get(DepartmentalInstructor.class.getName())) {
            	QueueItem qi = new QueueItem(iMetamodel.entity(DistributionPref.class), instructor, "owner", Relation.Parent);
            	distributions.add(qi);
            	queue.add(qi);
            	if (qi.size() > 0)
            		iProgress.info("Extra: " + qi);
            }
            data.put(DistributionPref.class.getName(), distributions);
            
            while ((item = queue.poll()) != null) {
            	if (item.size() == 0) continue;
            	for (Attribute attribute: (Set<Attribute>)item.meta().getAttributes()) {
            		String property = attribute.getName();
            		if (isEntity(attribute)) {
                		if (avoid.contains(item.name() + "." + property)) continue;

                		EntityType meta = null;
                		try {
                			meta = iMetamodel.entity(attribute.getJavaType());
                		} catch (IllegalArgumentException e) {}
                		if (meta == null || item.contains(meta.getJavaType().getName())) continue;
                		
                		QueueItem qi = new QueueItem(meta, item, property, Relation.One);
                		List<QueueItem> items = data.get(qi.name());
                		if (items == null) {
                			items = new ArrayList<QueueItem>();
                			data.put(qi.name(), items);
                		}
                		queue.add(qi);
                		items.add(qi);
                		
                		if (qi.size() > 0)
                			iProgress.info("One: " + qi);
                	}
                	if (attribute.isCollection()) {
                		if (avoid.contains(item.name() + "." + property)) continue;
                		
                		EntityType meta = null;
                		try {
                			meta = iMetamodel.entity(((PluralAttribute)attribute).getElementType().getJavaType());
                		} catch (IllegalArgumentException e) {}
                		if (meta == null || item.contains(meta.getJavaType().getName())) continue;

                		QueueItem qi = new QueueItem(meta, item, property, Relation.Many);
                		List<QueueItem> items = data.get(qi.name());
                		if (items == null) {
                			items = new ArrayList<QueueItem>();
                			data.put(qi.name(), items);
                		}
                		queue.add(qi);
                		items.add(qi);
                		
                		if (qi.size() > 0)
                			iProgress.info("Many: " + qi);
                	}
                }
            }
            iProgress.incProgress();
            
            Map<String, Set<Serializable>> allExportedIds = new HashMap<String, Set<Serializable>>();
            for (String name: new TreeSet<String>(data.keySet())) {
            	List<QueueItem> list = data.get(name);
            	Map<String, TableData.Table.Builder> tables = new HashMap<String, TableData.Table.Builder>();
            	for (QueueItem current: list) {
            		if (current.size() == 0) continue;
            		iProgress.info("Loading " + current);
            		List<Object> objects = current.list();
            		if (objects == null || objects.isEmpty()) continue;
            		iProgress.setPhase(current.abbv() + " [" + objects.size() + "]", objects.size());
            		objects: for (Object object: objects) {
            			iProgress.incProgress();
            			
            			// Get meta data (check for sub-classes)
            			EntityType meta = null;
            			try {
            				meta = iMetamodel.entity(object.getClass());
            			} catch (IllegalArgumentException e) {}
            			if (meta == null) meta = current.meta();
            			if (hasSubclasses(meta)) {
            				for (EntityType t: iMetamodel.getEntities()) {
            					if (t.getJavaType().isInstance(object) && !hasSubclasses(t)) {
            	                	meta = t; break;
            	                }
            	    		}
            			}
            			
            			// Get unique identifier
            			Serializable id = null;
            			if (hasCompositeId(meta)) {
            				List<SingularAttribute> idAttributes = getIdAttributes(meta);
            				Object[] ids = new Object[idAttributes.size()];
            				for (int i = 0; i < idAttributes.size(); i++) {
            					Object value = getProperty(object, idAttributes.get(i));
            					if (value == null) continue;
            					if (isEntity(idAttributes.get(i))) {
            						ids[i] = getProperty(value, getIdAttribute(iMetamodel.entity(idAttributes.get(i).getJavaType())));
            					} else {
            						ids[i] = value;
            					}
            				}
            				id = new CompositeId(ids);
            			} else {
            				id = (Serializable)getProperty(object, getIdAttribute(meta));
            			}
            			
            			// Check if already exported
            			Set<Serializable> exportedIds = allExportedIds.get(meta.getJavaType().getName());
            			if (exportedIds == null) {
            				exportedIds = new HashSet<Serializable>();
            				allExportedIds.put(meta.getJavaType().getName(), exportedIds);
            			}
            			if (!exportedIds.add(id)) continue;
            			
            			// Check relation to an academic session (if exists)
            			for (Attribute attribute: (Set<Attribute>)meta.getAttributes()) {
                        	if (attribute.getJavaType().equals(Session.class)) {
                        		Session s = (Session)getProperty(object, attribute);
                        		if (s != null && !s.getUniqueId().equals(iSessionId)) {
                        			iProgress.warn(meta.getName() + "@" + id + " belongs to a different academic session (" + s + ")");
                        			continue objects; // wrong session
                        		}
                        	}
            			}

            			// Get appropriate table
            			TableData.Table.Builder table = tables.get(meta.getJavaType().getName());
            			if (table == null) {
            				table = TableData.Table.newBuilder();
            				tables.put(meta.getJavaType().getName(), table);
            				table.setName(meta.getJavaType().getName());
            			}

            			// Export object
            			TableData.Record.Builder record = TableData.Record.newBuilder();
            			record.setId(id.toString());
            			List<Attribute> attributes = new ArrayList<Attribute>();
            			if (hasCompositeId(meta))
            				attributes.addAll(getIdAttributes(meta));
            			for (Attribute attribute: (Set<Attribute>)meta.getAttributes()) {
            				if (attribute.isCollection() || !((SingularAttribute)attribute).isId())
            					attributes.add(attribute);
            			}
            			for (Attribute attribute: attributes) {
                    		String property = attribute.getName();
            				Object value = getProperty(object, attribute);
            				if (value == null) continue;
            				TableData.Element.Builder element = TableData.Element.newBuilder();
            				element.setName(property);
            				if (value instanceof Boolean) {
            					element.addValue(value.toString());
            				} else if (value instanceof Byte) {
            					element.addValue(value.toString());
            				} else if (value instanceof Short) {
            					element.addValue(value.toString());
            				} else if (value instanceof Integer) {
            					element.addValue(value.toString());
            				} else if (value instanceof Long) {
            					element.addValue(value.toString());
            				} else if (value instanceof Float) {
            					element.addValue(value.toString());
            				} else if (value instanceof Double) {
            					element.addValue(value.toString());
            				} else if (value instanceof String) {
            					element.addValue(value.toString());
            				} else if (value instanceof Date) {
            					element.addValue(iDateFormat.format((Date)value));
            				} else if (value instanceof byte[]) {
            					element.addValueBytes(ByteString.copyFrom((byte[])value));
            				} else if (isEntity(attribute)) {
    							List<Object> ids = current.relation(property, meta, id, false);
    							if (ids != null)
    								for (Object i: ids)
    									element.addValue(i.toString());
    							iHibSession.evict(value);
            				} else if (attribute.isCollection()) {
    							List<Object> ids = current.relation(property, meta, id, false);
    							if (ids != null)
    								for (Object i: ids)
    									element.addValue(i.toString());
            				} else {
            					iProgress.warn("Unknown data type: " + attribute.getJavaType() + " (property " + meta.getName() + "." + property + ", " + value.getClass() + ")");
            					continue;
            				}
            				record.addElement(element.build());
            			}
            			table.addRecord(record.build());
            			iHibSession.evict(object);
            		}
            		current.clearCache();
            	}
            	
            	for (TableData.Table.Builder table: tables.values()) {
            		add(table.build());
            	}
            }
            
    		iProgress.setStatus("All done.");
        } finally {
        	iHibSession.close();
        }
	}
	
	enum Relation {
		None, Parent, One, Many, Empty
	}
	
	private int iQueueItemCoutner = 0;
	private Map<String, Set<Serializable>> iExclude = new HashMap<String, Set<Serializable>>();

	class QueueItem {
		QueueItem iParent;
		EntityType iMeta;
		String iProperty;
		int iQueueItemId = iQueueItemCoutner++;
		Relation iRelation;
		int size = -1;
		
		QueueItem(EntityType meta, QueueItem parent, String property, Relation relation) {
			iMeta = meta;
			iParent = parent;
			iProperty = property;
			iRelation = relation;
		}
		
		String property() { return iProperty; }
		QueueItem parent() { return iParent; }
		EntityType meta() { return iMeta; }
		String name() { return meta().getJavaType().getName(); }
		String abbv() { return meta().getName(); }
		Class clazz() { return meta().getJavaType(); }
		int qid() { return iQueueItemId; }
		Relation relation() { return iRelation; }
		boolean contains(String name) {
			if (name.equals(name())) return true;
			if (parent() == null) return false;
			return parent().contains(name);
		}
		int depth() { return parent() == null ? 0 : 1 + parent().depth(); }
		
		public String toString() {  return abbv() + ": " + chain() + " [" + size() + "]"; }
		
		public String chain() {
			switch (relation()) {
			case None:
				return property();
			case Parent:
				return property() + "." + parent().chain();
			default:
				switch (parent().relation()) {
				case Parent:
					return "(" + parent().abbv() + "." + parent().chain() + ")." + property();
				case None:
					return parent().abbv() + "." + property();
				default:
					return parent().chain() + "." + property();
				}
			}
		}
		
		String hqlName() { return "q" + qid(); }
		
		String hqlFrom() {
			switch (relation()) {
			case One:
			case Many:
				return parent().hqlFrom() + " inner join " + parent().hqlName() + "." + property() + " " + hqlName();
			default:
				return (parent() == null ? "" : parent().hqlFrom() + ", ") + name() + " " + hqlName();
			}
		}
		
		String hqlWhere() {
			String where = null;
			switch (relation()) {
			case None:
				where = hqlName() + "." + property() + " = :sessionId";
				break;
			case Parent:
				where = hqlName() + "." + property() + " = " + parent().hqlName() + " and " + parent().hqlWhere();
				break;
			default:
				where = parent().hqlWhere();
				break;
			}
			if (Solution.class.getName().equals(name()))
				where += " and " + hqlName() + ".commited = true";
			if (Assignment.class.getName().equals(name()))
				where += " and " + hqlName() + ".solution.commited = true";
			if (SolutionInfo.class.getName().equals(name()))
				where += " and " + hqlName() + ".definition.name = 'GlobalInfo'";
			return where;
		}
		
		int size() {
			if (relation() == Relation.Empty) return 0;
			if (AssignmentInfo.class.getName().equals(name())) return 0;
			if (ConstraintInfo.class.getName().equals(name())) return 0;
			if (ChangeLog.class.getName().equals(name())) return 0;
			if (size < 0) {
				Set<Serializable> ids = iExclude.get(name());
				if (ids == null) {
					ids = new HashSet<Serializable>();
					iExclude.put(name(), ids);
				}
				size = 0;
				if (hasCompositeId(meta())) {
					List<SingularAttribute> idAttributes = getIdAttributes(meta());
					String select = "";
					int i = 0;
					for (SingularAttribute id: idAttributes) {
						EntityType meta = null;
						try {
							meta = iMetamodel.entity(id.getJavaType());
						} catch (IllegalArgumentException e) {}
						if (meta == null)
							select += (i > 0 ? ", " : "") + hqlName() + "." + id.getName();
						else
							select += (i > 0 ? ", " : "") + hqlName() + "." + id.getName() + "." + getIdAttribute(meta).getName();
						i++;
					}
					for (Object[] id: iHibSession.createQuery(
							"select distinct " + select +  " from " + hqlFrom() + " where " + hqlWhere(),
							Object[].class
							).setParameter("sessionId", iSessionId, Long.class).list()) {
						if (ids.add(new CompositeId(id))) size++;
					}
				} else {
					for (Serializable id: iHibSession.createQuery(
							"select distinct " + hqlName() + "." + getIdAttribute(meta()).getName() + " from " + hqlFrom() + " where " + hqlWhere(),
							Serializable.class
							).setParameter("sessionId", iSessionId, Long.class).list()) {
						if (ids.add(id)) size++;
					}
				}
			}
			return size;
		}
		
		boolean hasBlob() {
			for (Attribute attribute: (Set<Attribute>)meta().getAttributes()) {
				if (attribute.getJavaType().equals(byte[].class))
					return true;
			}
			return false;
		}
		
		boolean distinct() {
			if (hasBlob())
				return true;
			switch (relation()) {
			case Many:
				return false;
			case One:
				return parent().distinct();
			default:
				return true;
			}
		}
		
		List<Object> list() {
			if (relation() == Relation.Empty) return null;
			if (AssignmentInfo.class.getName().equals(name())) return null;
			if (ConstraintInfo.class.getName().equals(name())) return null;
			if (ChangeLog.class.getName().equals(name())) return null;
			return iHibSession.createQuery(
					"select " + (distinct() ? "" : "distinct ") + hqlName() + " from " + hqlFrom() + " where " + hqlWhere(),
					Object.class
					).setParameter("sessionId", iSessionId, Long.class).list();
		}
		
		Map<String, Map<Serializable, List<Object>>> iRelationCache = new HashMap<String, Map<Serializable,List<Object>>>();
		
		List<Object> relation(String property, EntityType m, Serializable id, boolean data) {
			Map<Serializable, List<Object>> relation = iRelationCache.get(property);
			if (relation == null) {
				String idProperty = null;
				boolean join = false;
				Attribute attribute = null;
				try {
					attribute = meta().getAttribute(property);
				} catch (IllegalArgumentException e) {
					attribute = m.getAttribute(property);
					join = true;
				} catch (UnknownEntityTypeException e) {
					attribute = m.getAttribute(property);
					join = true;
				}
				int composite = 0;
				if (!data) {
					EntityType meta = null;
					try {
						if (attribute.isCollection())
							meta = (EntityType)((PluralAttribute)attribute).getElementType();
						else
							meta = (EntityType)((SingularAttribute)attribute).getType();
					} catch (ClassCastException e) {}
					if (meta == null) {
						data = true;
					} else if (hasCompositeId(meta)) {
						idProperty = "";
						for (SingularAttribute idatt: getIdAttributes(meta)) {
							EntityType idmeta = null;
							try {
								idmeta = iMetamodel.entity(idatt.getJavaType());
							} catch (IllegalArgumentException e) {}
							if (idmeta == null)
								idProperty += (composite > 0 ? ", p." : "") + idatt.getName();
							else
								idProperty += (composite > 0 ? ", p." : "") + idatt.getName() + "." + getIdAttribute(idmeta).getName();
							composite++;
						}
					} else {
						idProperty = getIdAttribute(meta).getName();
						if (name().equals(LastLikeCourseDemand.class.getName()) && "student".equals(property))
							idProperty = "externalUniqueId";
					}
				}
				relation = new HashMap<Serializable, List<Object>>();
				if (hasCompositeId(meta())) {
					List<SingularAttribute> idatts = getIdAttributes(meta());
					String select = "";
					for (int i = 0; i < idatts.size(); i++) {
						EntityType meta = null;
						try {
							meta = iMetamodel.entity(idatts.get(i).getJavaType());
						} catch (IllegalArgumentException e) {}
						if (meta == null)
							select += (i > 0 ? ", " : "") + hqlName() + "." + idatts.get(i).getName();
						else
							select += (i > 0 ? ", " : "") + hqlName() + "." + idatts.get(i).getName() + "." + getIdAttribute(meta).getName();
					}
					String q = "select distinct " + select + (data ? ", p" : ", p." + idProperty) + " from " + hqlFrom() + " inner join " + hqlName() + "." + property + " p where " + hqlWhere();
					if (join) {
						q = "select distinct " + select + (data ? ", p" : ", p." + idProperty) + " from " + hqlFrom() + ", " + m.getName() + " x inner join x." + property + " p where " + hqlWhere();
						for (int i = 0; i < idatts.size(); i++) {
							EntityType meta = null;
							try {
								meta = iMetamodel.entity(idatts.get(i).getJavaType());
							} catch (IllegalArgumentException e) {}
							if (meta == null)
								q += " and " + hqlName() + "." + idatts.get(i).getName() + " = x." + idatts.get(i).getName();
							else
								q += " and " + hqlName() + "." + idatts.get(i).getName() + "." + getIdAttribute(meta).getName() + " = x." + idatts.get(i).getName() + "." + getIdAttribute(meta).getName();
						}	
					}
					for (Object[] o: (List<Object[]>)iHibSession.createQuery(q, Object[].class).setParameter("sessionId", iSessionId, Long.class).list()) {
						Object[] cid = new Object[idatts.size()];
						for (int i = 0; i < idatts.size(); i++)
							cid[i] = o[i];
						Object obj = o[idatts.size()];
						List<Object> list = relation.get(new CompositeId(cid));
						if (list == null) {
							list = new ArrayList<Object>();
							relation.put(new CompositeId(cid), list);
						}
						if (composite > 1) {
							Object[] oid = new Object[composite];
							for (int i = 0; i < composite; i++) oid[i] = o[idatts.size() + i];
							list.add(new CompositeId(oid));
						} else {
							list.add(obj);
						}
					}
				} else {
					SingularAttribute idattr = getIdAttribute(meta());
					String q = "select distinct " + hqlName() + "." + idattr.getName() + (data ? ", p" : ", p." + idProperty) + 
							" from " + hqlFrom() + " inner join " + hqlName() + "." + property + " p where " + hqlWhere();
					if (join) {
							q = "select distinct " + hqlName() + "." + idattr.getName() + (data ? ", p" : ", p." + idProperty) + 
								" from " + hqlFrom() + ", " + m.getName() + " x inner join x." + property + " p where " + hqlWhere() + 
								" and " + hqlName() + "." + idattr.getName() + " = x." + idattr.getName();
					}
					for (Object[] o: (List<Object[]>)iHibSession.createQuery(q, Object[].class).setParameter("sessionId", iSessionId, Long.class).list()) {
						List<Object> list = relation.get((Serializable)o[0]);
						if (list == null) {
							list = new ArrayList<Object>();
							relation.put((Serializable)o[0], list);
						}
						if (composite > 1) {
							Object[] oid = new Object[composite];
							for (int i = 0; i < composite; i++) oid[i] = o[1 + i];
							list.add(new CompositeId(oid));
						} else {
							list.add(o[1]);
						}
					}
				}
				iRelationCache.put(property, relation);
				// iProgress.info("Fetched " + property + " (" + cnt + (data ? " items" : " ids") + ")");
			}
			return relation.get(id);
		}
		
		private void clearCache() { iRelationCache.clear(); }
	}
	
	public static void main(String[] args) {
		try {
            HibernateUtil.configureHibernate(ApplicationProperties.getProperties());

			Session session = Session.getSessionUsingInitiativeYearTerm(
                    ApplicationProperties.getProperty("initiative", "PWL"),
                    ApplicationProperties.getProperty("year","2012"),
                    ApplicationProperties.getProperty("term","Spring")
                    );
            
            if (session==null) {
                sLog.error("Academic session not found, use properties initiative, year, and term to set academic session.");
                System.exit(0);
            } else {
                sLog.info("Session: "+session);
            }
            
            
            FileOutputStream out = new FileOutputStream(args.length == 0
            		? session.getAcademicTerm() + session.getAcademicYear() + session.getAcademicInitiative() + ".dat"
            		: args[0]);
            
            final Progress progress = Progress.getInstance();
            sLog.info("Using " + ApplicationProperty.SessionBackupInterface.value());
            SessionBackup backup = (SessionBackup)Class.forName(ApplicationProperty.SessionBackupInterface.value()).getDeclaredConstructor().newInstance();

            PrintWriter debug = null;
            if (args.length >= 2) {
            	debug = new PrintWriter(args[1]);
            	backup.debug(debug);
            }
            
            progress.addProgressListener(new ProgressWriter(System.out));
            
            backup.backup(out, new BackupProgress() {
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
			}, session.getUniqueId());
            
            out.close();
            if (debug != null) debug.close();
            
		} catch (Exception e) {
			sLog.fatal("Backup failed: " + e.getMessage(), e);
		}
	}
	
	public static class CompositeId implements Serializable {
		private static final long serialVersionUID = 1L;
		private Serializable[] iId;
		
		public CompositeId(Object... id) {
			iId = new Serializable[id.length];
			for (int i = 0; i < iId.length; i++)
				iId[i] = (Serializable)id[i];
		}
		
		@Override
		public int hashCode() {
			int hashCode = (iId[0] == null ? 0 : iId[0].hashCode());
			for (int i = 1; i < iId.length; i++)
				if (iId[i] != null) hashCode = hashCode ^ iId[i].hashCode();
			return hashCode;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof CompositeId)) return false;
			CompositeId id = (CompositeId)o;
			if (id.iId.length != iId.length) return false;
			for (int i = 0; i < iId.length; i++)
				if (!ToolBox.equals(iId[i], id.iId[i])) return false;
			return true;
		}
		
		@Override
		public String toString() {
			String ret = "";
			for (int i = 0; i < iId.length; i++)
				ret += (i > 0 ? "|" : "") + (iId[i] == null ? "" : iId[i].toString());
			return ret;
		}
	}

}
