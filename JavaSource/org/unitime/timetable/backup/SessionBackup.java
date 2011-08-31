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
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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
import org.hibernate.EntityMode;
import org.hibernate.SessionFactory;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.BinaryType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.CustomType;
import org.hibernate.type.DateType;
import org.hibernate.type.EmbeddedComponentType;
import org.hibernate.type.EntityType;
import org.hibernate.type.ManyToOneType;
import org.hibernate.type.PrimitiveType;
import org.hibernate.type.StringType;
import org.hibernate.type.TimestampType;
import org.hibernate.type.Type;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao._RootDAO;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedOutputStream;

public class SessionBackup {
    private static Log sLog = LogFactory.getLog(SessionBackup.class);
    private SessionFactory iHibSessionFactory = null;
	private org.hibernate.Session iHibSession = null;
	
	private Set<Entity> iObjects = new HashSet<Entity>();
	
	private CodedOutputStream iOut = null;
	private Long iSessionId = null;
	private Progress iProgress = null;
	
	public SessionBackup(OutputStream out) {
        iHibSession = new _RootDAO().getSession(); 
        iHibSessionFactory = iHibSession.getSessionFactory();
        iOut = CodedOutputStream.newInstance(out);
        iProgress = Progress.getInstance();
	}
	
	public Progress getProgress() {
		return iProgress;
	}
	
	private void add(TableData.Table table) throws IOException {
		iOut.writeInt32NoTag(table.getSerializedSize());
		table.writeTo(iOut);
		iOut.flush();
	}
	
	private <T> void export(ClassMetadata metadata, List<Entity> objects) throws IOException {
		iProgress.setPhase(metadata.getEntityName().substring(metadata.getEntityName().lastIndexOf('.') + 1) + " [" + objects.size() + "]", objects.size());
		TableData.Table.Builder table = TableData.Table.newBuilder();
		table.setName(metadata.getEntityName());
		for (Entity entity: objects) {
			iProgress.incProgress();
			TableData.Record.Builder record = TableData.Record.newBuilder();
			record.setId(metadata.getIdentifier(entity.getObject(), (SessionImplementor)iHibSession).toString());
			properties: for (String property: metadata.getPropertyNames()) {
				Type type = metadata.getPropertyType(property);
				Object value = metadata.getPropertyValue(entity.getObject(), property, EntityMode.POJO);
				if (value == null) continue;
				TableData.Element.Builder element = TableData.Element.newBuilder();
				element.setName(property);
				if (type instanceof PrimitiveType) {
					element.addValue(((PrimitiveType)type).toString(value));
				} else if (type instanceof StringType) {	
					element.addValue(((StringType)type).toString(value));
				} else if (type instanceof BinaryType) {	
					element.addValue(ByteString.copyFrom((byte[])value));
				} else if (type instanceof TimestampType) {
					element.addValue(((TimestampType)type).toString(value));
				} else if (type instanceof DateType) {
					element.addValue(((DateType)type).toString(value));
				} else if (type instanceof EntityType) {
					ClassMetadata m = iHibSessionFactory.getClassMetadata(((EntityType)type).getReturnedClass());
					element.addValue(m.getIdentifier(value, (SessionImplementor)iHibSession).toString());
					iObjects.add(new Entity(m, value, entity));
				} else if (type instanceof CustomType && value instanceof Document) {
					StringWriter w = new StringWriter();
					XMLWriter x = new XMLWriter(w, OutputFormat.createCompactFormat());
					x.write((Document)value);
					x.flush(); x.close();
					element.addValue(w.toString());
				} else if (type instanceof CollectionType) {
					Collection<?> values = (Collection<?>)value;
					if (values.isEmpty()) continue properties;
					ClassMetadata m = iHibSessionFactory.getClassMetadata(((CollectionType)type).getElementType((SessionFactoryImplementor)iHibSessionFactory).getReturnedClass());
					for (Object v: values) {
						element.addValue(m.getIdentifier(v, (SessionImplementor)iHibSession).toString());
						iObjects.add(new Entity(m, v, entity));
					}
				} else if (type instanceof EmbeddedComponentType && property.equalsIgnoreCase("uniqueCourseNbr")) {
					continue;
				} else {
					sLog.warn("Unknown data type: " + type + " (property " + metadata.getEntityName() + "." + property + ", class " + value.getClass() + ")");
					continue;
				}
				record.addElement(element.build());
			}
			table.addRecord(record.build());
			entity.setExported(true);
		}
		add(table.build());
	}
	
	public void backup(Session session) throws IOException {
		iProgress.setStatus("Exporting " + session.getLabel());
		Entity sessionEntity = new Entity(iHibSessionFactory.getClassMetadata(Session.class), session, null);
		iSessionId = session.getUniqueId();
		iObjects.add(sessionEntity);
		TreeSet<ClassMetadata> allMeta = new TreeSet<ClassMetadata>(new Comparator<ClassMetadata>() {
			@Override
			public int compare(ClassMetadata m1, ClassMetadata m2) {
				return m1.getEntityName().compareTo(m2.getEntityName());
			}
		});
		allMeta.addAll(iHibSessionFactory.getAllClassMetadata().values());
		iProgress.setPhase("Loading data", allMeta.size());
		Queue<QueueItem> queue = new LinkedList<QueueItem>();
        for (ClassMetadata meta: allMeta) {
        	iProgress.incProgress();
            if (meta.hasSubclasses()) continue;
            for (String property: meta.getPropertyNames()) {
            	Type type = meta.getPropertyType(property);
            	if (type instanceof ManyToOneType && type.getReturnedClass().equals(Session.class)) {
            		List<Object> list = iHibSession.createQuery(
            				"from " + meta.getEntityName() + " where " + property + ".uniqueId = :sessionId")
            				.setLong("sessionId", session.getUniqueId()).list();
            		if (!list.isEmpty())
            			sLog.info("Found " + list.size() + " x " + meta.getEntityName());
            		boolean added = false;
            		for (Object obj: list) {
            			Entity entity = new Entity(meta, obj, sessionEntity);
            			if (iObjects.add(entity) && !entity.isExported())
            				added = true;
            		}
            		if (added) queue.add(new QueueItem(meta.getMappedClass(EntityMode.POJO), property));
            	}
            }
        }
        QueueItem item = null;
        iProgress.setPhase("Loading data", queue.size()); int qSize = queue.size();
        while ((item = queue.poll()) != null) {
        	iProgress.incProgress();
        	if (iProgress.getProgress() >= qSize) {
        		iProgress.setPhase("Loading data", queue.size());
        		qSize = queue.size();
        	}
            for (ClassMetadata meta: allMeta) {
                if (meta.hasSubclasses()) continue;
                for (String property: meta.getPropertyNames()) {
                	Type type = meta.getPropertyType(property);
                	if (type instanceof ManyToOneType && type.getReturnedClass().equals(item.getType())) {
                		List<Object> list = iHibSession.createQuery(
                				"from " + meta.getEntityName() + " where " + property + "." + item.getProperty() + ".uniqueId = :sessionId")
                				.setLong("sessionId", session.getUniqueId()).list();
                		boolean added = false;
                		if (!list.isEmpty())
                			sLog.info("Found " + list.size() + " x " + meta.getEntityName());
                		for (Object obj: list) {
                			Entity entity = new Entity(meta, obj, sessionEntity);
                			if (iObjects.add(entity) && !entity.isExported())
                				added = true;
                		}
                		if (added) queue.add(new QueueItem(meta.getMappedClass(EntityMode.POJO), property + "." + item.getProperty()));
                	}
                }
            }
        }
        
		while (true) {
			List<Entity> objects = new ArrayList<Entity>();
			for (Entity e: iObjects) {
				if (e.isExported()) continue;
				if (objects.isEmpty() || objects.get(0).getName().equals(e.getName())) {
					objects.add(e);
				} else if (objects.get(0).compareTo(e) >= 0) {
					objects.clear();
					objects.add(e);
				}
			}
			if (objects.isEmpty()) break;
			export(objects.get(0).getMetaData(), objects);
		}
		iProgress.setStatus("All done.");
	}
	
	private long iSerialId = 0;
	class Entity implements Comparable<Entity> {
		private ClassMetadata iMetaData;
		private Serializable iId;
		private Object iObject;
		private Entity iParent;
		private boolean iExported = false;
		private long iSID = iSerialId++;
		
		Entity(ClassMetadata metadata, Object object, Entity parent) {
			iMetaData = iHibSessionFactory.getClassMetadata(object.getClass());
			if (iMetaData == null)
				iMetaData = metadata;
			if (iMetaData.hasSubclasses()) {
	            for (Iterator i=iHibSessionFactory.getAllClassMetadata().entrySet().iterator();i.hasNext();) {
	                Map.Entry entry = (Map.Entry)i.next();
	                ClassMetadata classMetadata = (ClassMetadata)entry.getValue();
	                if (classMetadata.getMappedClass(EntityMode.POJO).isInstance(object) && !classMetadata.hasSubclasses()) {
	                	iMetaData = classMetadata; break;
	                }
	            }
			}
			iObject = object;
			iParent = parent;
			iId = iMetaData.getIdentifier(iObject, (SessionImplementor)iHibSession);
			for (String property: iMetaData.getPropertyNames()) {
            	Type type = iMetaData.getPropertyType(property);
            	if (type instanceof ManyToOneType && type.getReturnedClass().equals(Session.class)) {
            		Session session = (Session)iMetaData.getPropertyValue(iObject, property, EntityMode.POJO);
            		if (session != null && !session.getUniqueId().equals(iSessionId)) {
            			// sLog.info("Skipping " + iObject + " (" + property + " refers to a wrong academic session)");
            			iExported = true; break;
            		}
            	}
			}
		}
		
		public ClassMetadata getMetaData() { return iMetaData; }
		public String getName() { return getMetaData().getEntityName(); }
		public Object getObject() { return iObject; }
		public Serializable getId() { return iId; }
		public boolean isExported() { return iExported; }
		public void setExported(boolean exported) { iExported = exported; }
		public Entity getParent() { return iParent; }
		
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Entity)) return false;
			Entity e = (Entity)o;
			return getName().equals(e.getName()) && getId().equals(e.getId());
		}
		
		public int hashCode() {
			return getId().hashCode();
		}
		
		public String toString() {
			return getId().toString();
		}
		
		public int compareTo(Entity e) {
			return iSID < e.iSID ? -1 : 1;
		}
	}
	
	class QueueItem {
		private Class iType;
		private String iProperty;
		
		public QueueItem(Class type, String property) {
			iType = type;
			iProperty = property;
		}
		
		public Class getType() { return iType; }
		public String getProperty() { return iProperty; }
	}
	
	public static void main(String[] args) {
		try {
            Properties props = new Properties();
            props.setProperty("log4j.rootLogger", "DEBUG, A1");
            props.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
            props.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
            props.setProperty("log4j.appender.A1.layout.ConversionPattern","%-5p %c{2}: %m%n");
            props.setProperty("log4j.logger.org.hibernate","INFO");
            props.setProperty("log4j.logger.org.hibernate.cfg","WARN");
            props.setProperty("log4j.logger.org.hibernate.cache.EhCacheProvider","ERROR");
            props.setProperty("log4j.logger.org.unitime.commons.hibernate","INFO");
            props.setProperty("log4j.logger.net","INFO");
            PropertyConfigurator.configure(props);
            
            HibernateUtil.configureHibernate(ApplicationProperties.getProperties());

            Session session = Session.getSessionUsingInitiativeYearTerm(
                    ApplicationProperties.getProperty("initiative", "PWL"),
                    ApplicationProperties.getProperty("year","2010"),
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
            
            SessionBackup backup = new SessionBackup(out);
            
            backup.getProgress().addProgressListener(new ProgressWriter(System.out));
            
            backup.backup(session);
            
            out.close();
            
            
		} catch (Exception e) {
			sLog.fatal("Backup failed: " + e.getMessage(), e);
		}
	}
	
}