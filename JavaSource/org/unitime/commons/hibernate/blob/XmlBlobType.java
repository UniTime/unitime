/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.commons.hibernate.blob;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

/**
 * @author Tomas Muller
 */
public class XmlBlobType implements UserType {
	protected static Log sLog = LogFactory.getLog(XmlBlobType.class);
	
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws SQLException {
        Blob blob = rs.getBlob(names[0]);
        if (blob==null) return null;
		try {
			SAXReader reader = new SAXReader();
			GZIPInputStream gzipInput = new GZIPInputStream(blob.getBinaryStream());
			Document document = reader.read(gzipInput);
			gzipInput.close();
			return document;
		} catch (IOException e) {
			throw new HibernateException(e.getMessage(),e);
		} catch (DocumentException e) {
			throw new HibernateException(e.getMessage(),e);
		}
    }

    public void nullSafeSet(PreparedStatement ps, Object value, int index, SessionImplementor session) throws SQLException, HibernateException {
        if (value == null) {
            ps.setNull(index, sqlTypes()[0]);
        } else {
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                XMLWriter writer = new XMLWriter(new GZIPOutputStream(bytes),OutputFormat.createCompactFormat());
                writer.write((Document)value);
                writer.flush(); writer.close();
                ps.setBinaryStream(index, new ByteArrayInputStream(bytes.toByteArray(),0,bytes.size()), bytes.size());
            } catch (IOException e) {
                throw new HibernateException(e.getMessage(),e);
            }
        }
    }


    public Object deepCopy(Object value) {
        if (value == null) return null;
        return ((Document)value).clone();
    }
    public boolean isMutable() {
        return false;
    }
    public int[] sqlTypes() {
        return new int[] { Types.BLOB };
    }
    public Class returnedClass() {
        return Document.class;
    }
    
    public boolean equals(Object x, Object y) {
    	if (x==null) return (y==null);
    	if (y==null) return false;
    	if (!x.getClass().getName().equals(y.getClass().getName())) return false;
    	if (x instanceof Document) {
    		Document a = (Document)x;
    		Document b = (Document)y;
    		return equals(a.getName(),b.getName()) && equals(a.getRootElement(),b.getRootElement());
    	} else if (x instanceof Element) {
    		Element a = (Element)x;
    		Element b = (Element)y;
    		return equals(a.getName(),b.getName()) && equals(a.getText(),b.getText()) && equals(a.attributes(),b.attributes()) && equals(a.elements(),b.elements()); 
    	} else if (x instanceof Attribute) {
    		Attribute a = (Attribute)x;
    		Attribute b = (Attribute)y;
    		return equals(a.getName(),b.getName()) && equals(a.getValue(),b.getValue());
    	} else if (x instanceof List) {
    		List a = (List)x;
    		List b = (List)y;
    		if (a.size()!=b.size()) return false;
    		for (int i=0;i<a.size();i++)
    			if (!equals(a.get(i),b.get(i))) return false;
    		return true;
    	} else return (x.equals(y));
    }
    
    public Serializable disassemble(Object value) throws HibernateException {
    	try {
            if (value==null) return null;
    		ByteArrayOutputStream out = new ByteArrayOutputStream(); 
    		XMLWriter writer = new XMLWriter(new GZIPOutputStream(out),OutputFormat.createCompactFormat());
    		writer.write((Document)value);
    		writer.flush(); writer.close();
    		return out.toByteArray();
    	} catch (UnsupportedEncodingException e) {
    		throw new HibernateException(e.getMessage(),e);
    	} catch (IOException e) {
    		throw new HibernateException(e.getMessage(),e);
    	}
    }
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
    	try {
            if (cached==null) return null;
    		ByteArrayInputStream in = new ByteArrayInputStream((byte[])cached); 
			SAXReader reader = new SAXReader();
			GZIPInputStream gzipInput = new GZIPInputStream(in);
			Document document = reader.read(gzipInput);
			gzipInput.close();
    		return document;
		} catch (DocumentException e) {
			throw new HibernateException(e.getMessage(),e);
    	} catch (IOException e) {
    		throw new HibernateException(e.getMessage(),e);
    	}
    }
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
    	return original;
    }
    public int hashCode(Object value) throws HibernateException {
    	return ((Document)value).hashCode();
    }
}
