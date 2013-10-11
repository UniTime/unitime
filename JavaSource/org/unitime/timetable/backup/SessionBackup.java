/*
 * UniTime 3.3 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import net.sf.cpsolver.ifs.util.Progress;
import net.sf.cpsolver.ifs.util.ProgressWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.BinaryType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.CustomType;
import org.hibernate.type.DateType;
import org.hibernate.type.EmbeddedComponentType;
import org.hibernate.type.EntityType;
import org.hibernate.type.PrimitiveType;
import org.hibernate.type.StringType;
import org.hibernate.type.TimestampType;
import org.hibernate.type.Type;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.AssignmentInfo;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ConstraintInfo;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.LastLikeCourseDemand;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolutionInfo;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao._RootDAO;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedOutputStream;

public class SessionBackup {
    private static Log sLog = LogFactory.getLog(SessionBackup.class);
    private SessionFactory iHibSessionFactory = null;
	private org.hibernate.Session iHibSession = null;
	
	private CodedOutputStream iOut = null;
	private PrintWriter iDebug = null;
	private Long iSessionId = null;
	private Progress iProgress = null;
	
	public SessionBackup(OutputStream out, Progress progress) {
        iOut = CodedOutputStream.newInstance(out);
        iProgress = progress;
	}
	
	public Progress getProgress() {
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
	
	public void backup(Long sessionId) throws IOException {
		iSessionId = sessionId;
        iHibSession = new _RootDAO().createNewSession(); 
        iHibSessionFactory = iHibSession.getSessionFactory();
        try {
    		iProgress.setStatus("Exporting Session");
    		iProgress.setPhase("Loading Model", 3);
    		TreeSet<ClassMetadata> allMeta = new TreeSet<ClassMetadata>(new Comparator<ClassMetadata>() {
    			@Override
    			public int compare(ClassMetadata m1, ClassMetadata m2) {
    				return m1.getEntityName().compareTo(m2.getEntityName());
    			}
    		});
    		allMeta.addAll(iHibSessionFactory.getAllClassMetadata().values());
    		iProgress.incProgress();
    		
    		Queue<QueueItem> queue = new LinkedList<QueueItem>();
    		
    		queue.add(new QueueItem(iHibSessionFactory.getClassMetadata(Session.class), null, "uniqueId", Relation.None));

    		Set<String> avoid = new HashSet<String>();
    		// avoid following relations
    		avoid.add(TimetableManager.class.getName() + ".departments");
    		avoid.add(TimetableManager.class.getName() + ".solverGroups");
    		avoid.add(DistributionType.class.getName() + ".departments");
    		avoid.add(LastLikeCourseDemand.class.getName() + ".student");
    		avoid.add(Student.class.getName() + ".lastLikeCourseDemands");
    		
    		Set<String> disallowedNotNullRelations = new HashSet<String>();
    		disallowedNotNullRelations.add(Assignment.class.getName() + ".datePattern");
    		disallowedNotNullRelations.add(Assignment.class.getName() + ".timePattern");
    		disallowedNotNullRelations.add(LastLikeCourseDemand.class.getName() + ".student");
    		
    		Map<String, List<QueueItem>> data = new HashMap<String, List<QueueItem>>();
    		List<QueueItem> sessions = new ArrayList<QueueItem>();
    		sessions.add(queue.peek());
    		data.put(queue.peek().name(), sessions);
    		
    		QueueItem item = null;
            while ((item = queue.poll()) != null) {
            	if (item.size() == 0) continue;
            	for (ClassMetadata meta: allMeta) {
                	if (meta.hasSubclasses()) continue;
                    for (int i = 0; i < meta.getPropertyNames().length; i++) {
                    	String property = meta.getPropertyNames()[i];
                    	if (disallowedNotNullRelations.contains(meta.getEntityName() + "." + property) || meta.getPropertyNullability()[i]) continue;
                    	Type type = meta.getPropertyTypes()[i];
                    	if (type instanceof EntityType && type.getReturnedClass().equals(item.clazz())) {
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
            	QueueItem qi = new QueueItem(iHibSessionFactory.getClassMetadata(DistributionPref.class), instructor, "owner", Relation.Parent);
            	distributions.add(qi);
            	queue.add(qi);
            	if (qi.size() > 0)
            		iProgress.info("Extra: " + qi);
            }
            data.put(DistributionPref.class.getName(), distributions);
            
            while ((item = queue.poll()) != null) {
            	if (item.size() == 0) continue;
            	for (int i = 0; i < item.meta().getPropertyNames().length; i++) {
                	String property = item.meta().getPropertyNames()[i];
                	Type type = item.meta().getPropertyTypes()[i];
                	if (type instanceof EntityType) {
                		if (avoid.contains(item.name() + "." + property)) continue;

                		ClassMetadata meta = iHibSessionFactory.getClassMetadata(type.getReturnedClass());
                		if (item.contains(meta.getEntityName())) continue;
                		
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
                	if (type instanceof CollectionType) {
                		if (avoid.contains(item.name() + "." + property)) continue;
                		
                		ClassMetadata meta = iHibSessionFactory.getClassMetadata(((CollectionType)type).getElementType((SessionFactoryImplementor)iHibSessionFactory).getReturnedClass());
                		if (meta == null || item.contains(meta.getEntityName())) continue;

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
            			ClassMetadata meta = iHibSessionFactory.getClassMetadata(object.getClass());
            			if (meta == null) meta = current.meta();
            			if (meta.hasSubclasses()) {
            	            for (Iterator i=iHibSessionFactory.getAllClassMetadata().entrySet().iterator();i.hasNext();) {
            	                Map.Entry entry = (Map.Entry)i.next();
            	                ClassMetadata classMetadata = (ClassMetadata)entry.getValue();
            	                if (classMetadata.getMappedClass().isInstance(object) && !classMetadata.hasSubclasses()) {
            	                	meta = classMetadata; break;
            	                }
            	            }
            			}
            			
            			// Get unique identifier
            			Serializable id = meta.getIdentifier(object, (SessionImplementor)iHibSession);
            			
            			// Check if already exported
            			Set<Serializable> exportedIds = allExportedIds.get(meta.getEntityName());
            			if (exportedIds == null) {
            				exportedIds = new HashSet<Serializable>();
            				allExportedIds.put(meta.getEntityName(), exportedIds);
            			}
            			if (!exportedIds.add(id)) continue;
            			
            			// Check relation to an academic session (if exists)
            			for (String property: meta.getPropertyNames()) {
            				Type type = meta.getPropertyType(property);
                        	if (type instanceof EntityType && type.getReturnedClass().equals(Session.class)) {
                        		Session s = (Session)meta.getPropertyValue(object, property);
                        		if (s != null && !s.getUniqueId().equals(iSessionId)) {
                        			iProgress.warn(meta.getEntityName().substring(meta.getEntityName().lastIndexOf('.') + 1) + "@" + id + " belongs to a different academic session (" + s + ")");
                        			continue objects; // wrong session
                        		}
                        	}
            			}

            			// Get appropriate table
            			TableData.Table.Builder table = tables.get(meta.getEntityName());
            			if (table == null) {
            				table = TableData.Table.newBuilder();
            				tables.put(meta.getEntityName(), table);
            				table.setName(meta.getEntityName());
            			}

            			// Export object
            			TableData.Record.Builder record = TableData.Record.newBuilder();
            			record.setId(id.toString());
            			for (String property: meta.getPropertyNames()) {
            				Type type = meta.getPropertyType(property);
            				Object value = meta.getPropertyValue(object, property);
            				if (value == null) continue;
            				TableData.Element.Builder element = TableData.Element.newBuilder();
            				element.setName(property);
            				if (type instanceof PrimitiveType) {
            					element.addValue(((PrimitiveType)type).toString(value));
            				} else if (type instanceof StringType) {	
            					element.addValue(((StringType)type).toString((String)value));
            				} else if (type instanceof BinaryType) {	
            					element.addValueBytes(ByteString.copyFrom((byte[])value));
            				} else if (type instanceof TimestampType) {
            					element.addValue(((TimestampType)type).toString((Date)value));
            				} else if (type instanceof DateType) {
            					element.addValue(((DateType)type).toString((Date)value));
            				} else if (type instanceof EntityType) {
    							List<Object> ids = current.relation(property, id, false);
    							if (ids != null)
    								for (Object i: ids)
    									element.addValue(i.toString());
    							iHibSession.evict(value);
            				} else if (type instanceof CustomType && value instanceof Document) {
            					if (object instanceof CurriculumClassification && property.equals("students")) continue;
            					StringWriter w = new StringWriter();
            					XMLWriter x = new XMLWriter(w, OutputFormat.createCompactFormat());
            					x.write((Document)value);
            					x.flush(); x.close();
            					element.addValue(w.toString());
            				} else if (type instanceof CollectionType) {
    							List<Object> ids = current.relation(property, id, false);
    							if (ids != null)
    								for (Object i: ids)
    									element.addValue(i.toString());
            				} else if (type instanceof EmbeddedComponentType && property.equalsIgnoreCase("uniqueCourseNbr")) {
            					continue;
            				} else {
            					iProgress.warn("Unknown data type: " + type + " (property " + meta.getEntityName() + "." + property + ", class " + value.getClass() + ")");
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
            
            /*
            // Skip ConstraintInfo
            if (!iData.containsKey(ConstraintInfo.class.getName()))
            	iData.put(ConstraintInfo.class.getName(), new QueueItem(iHibSessionFactory.getClassMetadata(ConstraintInfo.class), null, null, Relation.Empty));

            for (String name: items)
            	export(iData.get(name));
                    
    		while (true) {
    			List<Object> objects = new ArrayList<Object>();
    			ClassMetadata meta = null;
    			for (Entity e: iObjects) {
    				if (e.exported()) continue;
    				if (objects.isEmpty() || meta.getEntityName().equals(e.name())) {
    					meta = e.meta();
    					objects.add(e.object());
    					e.notifyExported();
    				}
    			}
    			if (objects.isEmpty()) break;
    			export(meta, objects, null);
    		}
    		*/
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
		ClassMetadata iMeta;
		String iProperty;
		int iQueueItemId = iQueueItemCoutner++;
		Relation iRelation;
		int size = -1;
		
		QueueItem(ClassMetadata meta, QueueItem parent, String property, Relation relation) {
			iMeta = meta;
			iParent = parent;
			iProperty = property;
			iRelation = relation;
		}
		
		String property() { return iProperty; }
		QueueItem parent() { return iParent; }
		ClassMetadata meta() { return iMeta; }
		String name() { return meta().getEntityName(); }
		String abbv() { return meta().getEntityName().substring(meta().getEntityName().lastIndexOf('.') + 1); }
		Class clazz() { return meta().getMappedClass(); }
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
				for (Serializable id: (List<Serializable>)iHibSession.createQuery(
						"select distinct " + hqlName() + "." + meta().getIdentifierPropertyName() + " from " + hqlFrom() + " where " + hqlWhere()
						).setLong("sessionId", iSessionId).list()) {
					if (ids.add(id)) size++;
				}				
			}
			return size;
		}
		
		boolean hasBlob() {
			for (String property: iMeta.getPropertyNames()) {
				if (iMeta.getPropertyType(property) instanceof BinaryType) return true;
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
					"select " + (distinct() ? "" : "distinct ") + hqlName() + " from " + hqlFrom() + " where " + hqlWhere()
					).setLong("sessionId", iSessionId).list();
		}
		
		Map<String, Map<Serializable, List<Object>>> iRelationCache = new HashMap<String, Map<Serializable,List<Object>>>();
		
		List<Object> relation(String property, Serializable id, boolean data) {
			Map<Serializable, List<Object>> relation = iRelationCache.get(property);
			if (relation == null) {
				Type type = meta().getPropertyType(property);
				String idProperty = null;
				if (!data) {
					ClassMetadata meta = null;
					if (type instanceof CollectionType)
						meta = iHibSessionFactory.getClassMetadata(((CollectionType)type).getElementType((SessionFactoryImplementor)iHibSessionFactory).getReturnedClass());
					else
						meta = iHibSessionFactory.getClassMetadata(type.getReturnedClass());
					if (meta == null) {
						data = true;
					} else {
						idProperty = meta.getIdentifierPropertyName();
						if (name().equals(LastLikeCourseDemand.class.getName()) && "student".equals(property))
							idProperty = "externalUniqueId";
					}
				}
				relation = new HashMap<Serializable, List<Object>>();
				for (Object[] o: (List<Object[]>)iHibSession.createQuery(
						"select distinct " + hqlName() + "." + meta().getIdentifierPropertyName() + (data ? ", p" : ", p." + idProperty) + 
						" from " + hqlFrom() + " inner join " + hqlName() + "." + property + " p where " + hqlWhere()
						).setLong("sessionId", iSessionId).list()) {
					List<Object> list = relation.get((Serializable)o[0]);
					if (list == null) {
						list = new ArrayList<Object>();
						relation.put((Serializable)o[0], list);
					}
					list.add(o[1]);
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
            
            SessionBackup backup = new SessionBackup(out, Progress.getInstance());
            
            PrintWriter debug = null;
            if (args.length >= 2) {
            	debug = new PrintWriter(args[1]);
            	backup.debug(debug);
            }
            
            backup.getProgress().addProgressListener(new ProgressWriter(System.out));
            
            backup.backup(session.getUniqueId());
            
            out.close();
            if (debug != null) debug.close();
            
		} catch (Exception e) {
			sLog.fatal("Backup failed: " + e.getMessage(), e);
		}
	}

}
