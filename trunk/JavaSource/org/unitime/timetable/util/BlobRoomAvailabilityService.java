package org.unitime.timetable.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.CallableStatement;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.dao._RootDAO;

public class BlobRoomAvailabilityService extends RoomAvailabilityService {
    private String iRequestSql =
        ApplicationProperties.getProperty("tmtbl.room.availability.request","{ call room_avail_interface.request(?) }");
    private String iResponseSql =
        ApplicationProperties.getProperty("tmtbl.room.availability.response","{? = call room_avail_interface.response()}");

    protected void sendRequest(Document request) throws IOException {
        try {
            StringWriter writer = new StringWriter();
            (new XMLWriter(writer,OutputFormat.createPrettyPrint())).write(request);
            writer.flush(); writer.close();
            CallableStatement call = new _RootDAO().getSession().connection().prepareCall(iRequestSql);
            call.setString(1, writer.getBuffer().toString());
            call.execute();
            call.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    protected Document recieveResponse() throws IOException, DocumentException {
        try {
            CallableStatement call = new _RootDAO().getSession().connection().prepareCall(iResponseSql);
            call.registerOutParameter(1, java.sql.Types.CLOB);
            call.execute();
            String response = call.getString(1);
            call.close();
            if (response==null || response.length()==0) return null;
            StringReader reader = new StringReader(response);
            Document document = (new SAXReader()).read(reader);
            reader.close();
            return document;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
