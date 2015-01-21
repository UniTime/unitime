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
