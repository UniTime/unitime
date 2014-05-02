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
package org.unitime.timetable.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.CallableStatement;
import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.hibernate.engine.spi.SessionImplementor;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.model.dao._RootDAO;

/**
 * @author Tomas Muller
 */
public class BlobRoomAvailabilityService extends RoomAvailabilityService {
    private static Log sLog = LogFactory.getLog(RoomAvailabilityInterface.class);
    private String iRequestSql = ApplicationProperty.BlobRoomAvailabilityRequestSQL.value();
    private String iResponseSql = ApplicationProperty.BlobRoomAvailabilityResponseSQL.value();

    protected void sendRequest(Document request) throws IOException {
        try {
            StringWriter writer = new StringWriter();
            (new XMLWriter(writer,OutputFormat.createPrettyPrint())).write(request);
            writer.flush(); writer.close();
            SessionImplementor session = (SessionImplementor)new _RootDAO().getSession();
            Connection connection = session.getJdbcConnectionAccess().obtainConnection();
            try {
                CallableStatement call = connection.prepareCall(iRequestSql);
                call.setString(1, writer.getBuffer().toString());
                call.execute();
                call.close();
            } finally {
            	session.getJdbcConnectionAccess().releaseConnection(connection);
            }
        } catch (Exception e) {
            sLog.error("Unable to send request: "+e.getMessage(),e);
        } finally {
            _RootDAO.closeCurrentThreadSessions();
        }
    }
    
    protected Document receiveResponse() throws IOException, DocumentException {
        try {
            SessionImplementor session = (SessionImplementor)new _RootDAO().getSession();
            Connection connection = session.getJdbcConnectionAccess().obtainConnection();
            String response = null;
            try {
                CallableStatement call = connection.prepareCall(iResponseSql);
                call.registerOutParameter(1, java.sql.Types.CLOB);
                call.execute();
                response = call.getString(1);
                call.close();
            } finally {
            	session.getJdbcConnectionAccess().releaseConnection(connection);
            }
            if (response==null || response.length()==0) return null;
            StringReader reader = new StringReader(response);
            Document document = (new SAXReader()).read(reader);
            reader.close();
            return document;
        } catch (Exception e) {
            sLog.error("Unable to receive response: "+e.getMessage(),e);
            return null;
        } finally {
            _RootDAO.closeCurrentThreadSessions();
        }
    }
}
