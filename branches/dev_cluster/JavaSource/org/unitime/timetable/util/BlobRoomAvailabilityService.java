/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
import org.hibernate.engine.SessionFactoryImplementor;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.model.dao._RootDAO;

public class BlobRoomAvailabilityService extends RoomAvailabilityService {
    private static Log sLog = LogFactory.getLog(RoomAvailabilityInterface.class);
    private String iRequestSql =
        ApplicationProperties.getProperty("tmtbl.room.availability.request","{ call room_avail_interface.request(?) }");
    private String iResponseSql =
        ApplicationProperties.getProperty("tmtbl.room.availability.response","{? = call room_avail_interface.response()}");

    protected void sendRequest(Document request) throws IOException {
        try {
            StringWriter writer = new StringWriter();
            (new XMLWriter(writer,OutputFormat.createPrettyPrint())).write(request);
            writer.flush(); writer.close();
            SessionFactoryImplementor hibSessionFactory = (SessionFactoryImplementor)new _RootDAO().getSession().getSessionFactory();
            Connection connection = hibSessionFactory.getConnectionProvider().getConnection();
            try {
                CallableStatement call = connection.prepareCall(iRequestSql);
                call.setString(1, writer.getBuffer().toString());
                call.execute();
                call.close();
            } finally {
                hibSessionFactory.getConnectionProvider().closeConnection(connection);
            }
        } catch (Exception e) {
            sLog.error("Unable to send request: "+e.getMessage(),e);
        } finally {
            _RootDAO.closeCurrentThreadSessions();
        }
    }
    
    protected Document receiveResponse() throws IOException, DocumentException {
        try {
            SessionFactoryImplementor hibSessionFactory = (SessionFactoryImplementor)new _RootDAO().getSession().getSessionFactory();
            Connection connection = hibSessionFactory.getConnectionProvider().getConnection();
            String response = null;
            try {
                CallableStatement call = connection.prepareCall(iResponseSql);
                call.registerOutParameter(1, java.sql.Types.CLOB);
                call.execute();
                response = call.getString(1);
                call.close();
            } finally {
                hibSessionFactory.getConnectionProvider().closeConnection(connection);
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
