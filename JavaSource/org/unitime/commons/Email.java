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
package org.unitime.commons;

import java.io.File;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

import org.unitime.timetable.defaults.ApplicationProperty;

/**
 * @author Tomas Muller
 */
public abstract class Email {

    public static Email createEmail() throws Exception {
        return (Email) Class.forName(ApplicationProperty.EmailProvider.value()).getDeclaredConstructor().newInstance();
    }

    public abstract void setSubject(String subject) throws Exception;

    public abstract void setFrom(String email, String name) throws Exception;

    public abstract void setReplyTo(String email, String name) throws Exception;

    public abstract void addReplyTo(String email, String name) throws Exception;

    public abstract void addRecipient(String email, String name) throws Exception;

    public abstract void addRecipientCC(String email, String name) throws Exception;

    public abstract void addRecipientBCC(String email, String name) throws Exception;

    public abstract void setBody(String message, String type) throws Exception;

    public void setText(String message) throws Exception {
        setBody(message, "text/plain; charset=UTF-8");
    }

    public void setHTML(String message) throws Exception {
        setBody(message, "text/html; charset=UTF-8");
    }

    public void addNotify() throws Exception {
        addRecipient(ApplicationProperty.EmailNotificationAddress.value(), ApplicationProperty.EmailNotificationAddressName.value());
    }

    public void addNotifyCC() throws Exception {
        addRecipientCC(ApplicationProperty.EmailNotificationAddress.value(), ApplicationProperty.EmailNotificationAddressName.value());
    }

    public abstract void addAttachment(String name, DataHandler data) throws Exception;

    public void addAttachment(File file, String name) throws Exception {
        addAttachment(name == null ? file.getName() : name, new DataHandler(new FileDataSource(file)));
    }

    public void addAttachment(DataSource source) throws Exception {
        addAttachment(source.getName(), new DataHandler(source));
    }

    public abstract void send() throws Exception;

    public abstract void setInReplyTo(String messageId) throws Exception;

    public abstract String getMessageId() throws Exception;

    public void setRich(String message) throws Exception {
        setBody(message, "text/richtext; charset=UTF-8");
    }

    public void setImage(String image) throws Exception {
        setBody(image, "image/jpeg;");
    }

    public void setPriority(int priority)  {
        if (priority < 1 || priority > 5) {
            throw new IllegalArgumentException("Priority must be between 1 and 5");
        }

    }


}
