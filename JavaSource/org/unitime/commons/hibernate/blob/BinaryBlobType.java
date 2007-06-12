/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.commons.hibernate.blob;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import org.unitime.commons.hibernate.interceptors.LobCleanUpInterceptor;

/**
 * @author Tomas Muller
 */

public class BinaryBlobType implements UserType {
	protected static Log sLog = LogFactory.getLog(BinaryBlobType.class);

    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws SQLException {
        //Get the blob field we are interested in from the result set 
        Blob blob = rs.getBlob(names[0]);
        if (blob==null) return null;
        return blob.getBytes(1,(int)blob.length());
    }

    public void nullSafeSet(PreparedStatement ps, Object value, int index) throws SQLException, HibernateException {
        DatabaseMetaData dbMetaData = ps.getConnection().getMetaData();

        if (value == null) {
            ps.setNull(index, sqlTypes()[0]);
        } else {
        	try {
        		Class oracleBlobClass = Class.forName("oracle.sql.BLOB");
        		Class oracleConnectionClass = Class.forName("oracle.jdbc.OracleConnection");

        		// now get the static factory method
        		Class[] partypes = new Class[3];
        		partypes[0] = Connection.class;
        		partypes[1] = Boolean.TYPE;
        		partypes[2] = Integer.TYPE;

        		Method createTemporaryMethod = oracleBlobClass.getDeclaredMethod("createTemporary", partypes);
        		
        		Field durationSessionField = oracleBlobClass.getField("DURATION_SESSION");
        		Object[] arglist = new Object[3];

        		Connection conn = dbMetaData.getConnection();
                try {
                    conn = (Connection)conn.getClass().getMethod("getConnection", new Class[]{}).invoke(conn, new Object[]{});
                } catch (NoSuchMethodException ex) {}
        		
        		// Make sure connection object is right type
        		if (!oracleConnectionClass.isAssignableFrom(conn.getClass())) {
        			throw new HibernateException("JDBC connection object must be a oracle.jdbc.OracleConnection. " +
        					"Connection class is " + conn.getClass().getName());
        		}
        		
        		arglist[0] = conn;
        		arglist[1] = Boolean.TRUE;
        		arglist[2] = durationSessionField.get(null); //null is valid because of static field
        		
        		// Create our BLOB
        		Object tempBlob = createTemporaryMethod.invoke(null, arglist); //null is valid because of static method
        		
        		// get the open method
        		partypes = new Class[1];
        		partypes[0] = Integer.TYPE;
        		
        		Method openMethod = oracleBlobClass.getDeclaredMethod("open", partypes);
        		
        		// prepare to call the method
        		Field modeReadWriteField = oracleBlobClass.getField("MODE_READWRITE");
        		arglist = new Object[1];
        		arglist[0] = modeReadWriteField.get(null); //null is valid because of static field
        		
        		// call open(BLOB.MODE_READWRITE);
        		openMethod.invoke(tempBlob, arglist);
        		
        		// get the getCharacterOutputStream method
        		Method getBinaryOutputStreamMethod = oracleBlobClass.getDeclaredMethod("getBinaryOutputStream", new Class[]{});
        		
        		OutputStream out = (OutputStream)getBinaryOutputStreamMethod.invoke(tempBlob,new Object[]{});
        		
        		// write the bytes to the blob 
        		out.write((byte[])value);
        		out.flush();
        		out.close();
        		
        		Method closeMethod = oracleBlobClass.getDeclaredMethod("close", new Class[]{});
        		
        		// call the close method 
                closeMethod.invoke(tempBlob, new Object[]{});
                
                // add the blob to the statement
                ps.setBlob(index, (Blob) tempBlob);
                
                LobCleanUpInterceptor.registerTempLobs(tempBlob);
        	} catch (ClassNotFoundException e) {
        		// could not find the class with reflection
        		throw new HibernateException("Unable to find a required class, reason: " + e.getMessage(),e);
        	} catch (NoSuchMethodException e) {
        		// could not find the metho with reflection
        		throw new HibernateException("Unable to find a required method, reason: " + e.getMessage(),e);
        	} catch (NoSuchFieldException e) {
        		// could not find the field with reflection
        		throw new HibernateException("Unable to find a required field, reason: " + e.getMessage(),e);
        	} catch (IllegalAccessException e) {
        		throw new HibernateException("Unable to access a required method or field, reason: " + e.getMessage(),e);
        	} catch (InvocationTargetException e) {
        		throw new HibernateException(e.getMessage(),e);
        	} catch (IOException e) {
        		throw new HibernateException(e.getMessage(),e);
        	}
        }
    }

    public Object deepCopy(Object value) {
        if (value == null) {
            return null;
        }
        byte[] src = (byte[])value;
        byte[] dest = new byte[src.length];
        System.arraycopy(src,0,dest,0,src.length);
        return dest;
    }
    public boolean isMutable() {
        return false;
    }
    public int[] sqlTypes() {
        return new int[] { Types.BLOB };
    }
    public Class returnedClass() {
        return byte[].class;
    }
    public boolean equals(Object x, Object y) {
    	return Arrays.equals((byte[])x,(byte[])y);
    }
    public Serializable disassemble(Object value) throws HibernateException {
    	return (byte[])value;
    }
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
    	return cached;
    }
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
    	return original;
    }
    public int hashCode(Object value) throws HibernateException {
    	return ((byte[])value).hashCode();
    }
}
