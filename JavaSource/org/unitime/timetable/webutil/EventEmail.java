package org.unitime.timetable.webutil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.mail.Authenticator;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.Event.MultiMeeting;
import org.unitime.timetable.util.Constants;

public class EventEmail {
    private static Log sLog = LogFactory.getLog(EventEmail.class);
    private Event iEvent = null;
    private TreeSet<MultiMeeting> iMeetings = null;
    private String iNote = null;
    private int iAction = sActionCreate;
    
    public static final int sActionCreate = 0;
    public static final int sActionApprove = 1;
    public static final int sActionReject = 2;
    public static final int sActionAddMeeting = 3;
    public static final int sActionUpdate = 4;
    public static final int sActionDelete = 5;
    
    public EventEmail(Event event, int action, TreeSet<MultiMeeting> meetings, String note) {
        iEvent = event;
        iAction = action;
        iMeetings = meetings;
        iNote = note;
    }
    
    public void send(HttpServletRequest request) {
        String subject = null;
        try {
            if (!"true".equals(ApplicationProperties.getProperty("tmtbl.event.confirmationEmail","true"))) {
                request.getSession().setAttribute(Constants.REQUEST_MSSG, "Confirmation emails are disabled.");
                return;
            }
            
            InternetAddress from = 
                        new InternetAddress(
                                ApplicationProperties.getProperty("tmtbl.inquiry.sender",ApplicationProperties.getProperty("tmtbl.contact.email")),
                                ApplicationProperties.getProperty("tmtbl.inquiry.sender.name"));
            Properties p = ApplicationProperties.getProperties();
            if (p.getProperty("mail.smtp.host")==null && p.getProperty("tmtbl.smtp.host")!=null)
                p.setProperty("mail.smtp.host", p.getProperty("tmtbl.smtp.host"));
            
            Authenticator a = null;
            if (ApplicationProperties.getProperty("tmtbl.mail.user")!=null && ApplicationProperties.getProperty("tmtbl.mail.pwd")!=null) {
                p.setProperty("mail.smtp.user", ApplicationProperties.getProperty("tmtbl.mail.user"));
                p.setProperty("mail.smtp.auth", "true");
                a = new Authenticator() {
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                ApplicationProperties.getProperty("tmtbl.mail.user"),
                                ApplicationProperties.getProperty("tmtbl.mail.pwd"));
                    }
                };
            }
            javax.mail.Session mailSession = javax.mail.Session.getDefaultInstance(p, a);
            MimeMessage mail = new MimeMessage(mailSession);
            mail.setFrom(from);
            
            switch (iAction) {
            case sActionCreate : 
                subject = "Event "+iEvent.getEventName()+" created.";
                break;
            case sActionApprove :
                subject = "Event "+iEvent.getEventName()+" approved.";
                break;
            case sActionReject :
                subject = "Event "+iEvent.getEventName()+" rejected.";
                break;
            case sActionUpdate :
                subject = "Event "+iEvent.getEventName()+" updated.";
                break;
            case sActionAddMeeting :
                subject = "Event "+iEvent.getEventName()+" updated (one or more meetings added).";
                break;
            case sActionDelete : 
                subject = "Event "+iEvent.getEventName()+" updated (one or more meetings deleted).";
                break;
            }
            mail.setSubject(subject);

            String message = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">";
            message += "<html><head>";
            message += "<title>"+subject+"</title>";
            message += "<meta http-equiv='Content-Type' content='text/html; charset=windows-1250'>";
            message += "<meta name='Author' content='UniTime LLC'>";
            message += "<style type='text/css'>";
            message += "<!--" +
            		"A:link     { color: blue; text-decoration: none; }" +
            		"A:visited  { color: blue; text-decoration: none; }" +
            		"A:active   { color: blue; text-decoration: none; }" +
            		"A:hover    { color: blue; text-decoration: none; }" +
            		"-->";
            message += "</style></head><body bgcolor='#ffffff' style='font-size: 10pt; font-family: arial;'>";
            message += "<table border='0' width='800' align='center' cellspacing='10'>";
            
            message += "<tr><td colspan='2' style='border-bottom: 2px #2020FF solid;'><font size='+2'>";
            message += iEvent.getEventName();
            message += "</td></tr>";
            message += "<tr><td>Event Type</td><td>"+iEvent.getEventTypeLabel()+"</td></tr>";
            if (iEvent.getMinCapacity()!=null) {
                message += "<tr><td>"+(iEvent.getEventType()==Event.sEventTypeSpecial?"Expected Size":"Event Capacity")+":</td>";
                if (iEvent.getMaxCapacity()==null || iEvent.getMinCapacity().equals(iEvent.getMaxCapacity())) {
                    message += "<td>"+iEvent.getMinCapacity()+"</td>";
                } else {
                    message += "<td>"+iEvent.getMinCapacity()+" - "+iEvent.getMaxCapacity()+"</td>";
                }
            }
            if (iEvent.getSponsoringOrganization()!=null) {
                message += "<tr><td>Sponsoring Organization</td><td>"+iEvent.getSponsoringOrganization().getName()+"</td></tr>";
            }
            if (iEvent.getMainContact()!=null) {
                message += "<tr><td>Main Contact</td><td>";
                message += "<a href='mailto:"+iEvent.getMainContact().getEmailAddress()+"'>";
                if (iEvent.getMainContact().getLastName()!=null)
                    message += iEvent.getMainContact().getLastName();
                if (iEvent.getMainContact().getFirstName()!=null)
                    message += ", "+iEvent.getMainContact().getFirstName();
                if (iEvent.getMainContact().getMiddleName()!=null)
                    message += ", "+iEvent.getMainContact().getMiddleName();
                message += "</a></td></tr>";
            }
            
            if (!iMeetings.isEmpty()) {
                message += "<tr><td colspan='2' style='border-bottom: 1px #2020FF solid; font-variant:small-caps;'>";
                message += "<br><font size='+1'>";
                switch (iAction) {
                case sActionCreate : 
                    message += "Following meetings were created by you or on your behalf";
                    break;
                case sActionApprove :
                    message += "Following meetings were approved";
                    break;
                case sActionReject :
                    message += "Following meetings were rejected";
                    if (iNote!=null && iNote.length()>0) message += " (see the note bellow for more details)";
                    break;
                case sActionAddMeeting :
                    message += "Following meetings were added by you or on your behalf";
                    break;
                case sActionDelete :
                    message += "Following meetings were deleted by you or on your behalf";
                    break;
                }
                message += "</font>";
                message += "</td></tr><tr><td colspan='2'>";
                message += "<table border='0' width='100%'>";
                message += "<tr><td><i>Date</i></td><td><i>Time</i></td><td><i>Location</i></td></tr>";
                for (MultiMeeting m : iMeetings) {
                    message += "<tr><td>";
                    message += m.getDays()+" "+new SimpleDateFormat("MM/dd").format(m.getMeetings().first().getMeetingDate());
                    message += (m.getMeetings().size()>1?" - "+new SimpleDateFormat("MM/dd").format(m.getMeetings().last().getMeetingDate()):"");
                    message += "</td><td>";
                    message += m.getMeetings().first().startTime()+" - "+m.getMeetings().first().stopTime();
                    message += "</td><td>";
                    message += (m.getMeetings().first().getLocation()==null?"":" "+m.getMeetings().first().getLocation().getLabel());
                    message += "</td></tr>";
                }
                message += "</table></td></tr>";
            }
            
            if (iNote!=null && iNote.length()>0) {
                message += "<tr><td colspan='2' style='border-bottom: 1px #2020FF solid; font-variant:small-caps;'>";
                message += "<br><font size='+1'>Notes</font>";
                message += "</td></tr><tr><td colspan='2' >";
                message += iNote.replaceAll("\n", "<br>");
                message += "</td></tr>";
            }
            
            if (iAction!=sActionCreate) {
                message += "<tr><td colspan='2' style='border-bottom: 1px #2020FF solid; font-variant:small-caps;'>";
                message += "<br><font size='+1'>All Meetings of "+iEvent.getEventName()+"</font>";
                message += "</td></tr>";
                if (iEvent.getMeetings().isEmpty()) {
                    message += "<tr><td colspan='2' style='background-color:';>";
                    message += "No meeting left, the event "+iEvent.getEventName()+" was deleted as well.";
                    message += "</td></tr>";
                } else {
                    message += "<tr><td colspan='2'>";
                    message += "<table border='0' width='100%'>";
                    message += "<tr><td><i>Date</i></td><td><i>Time</i></td><td><i>Location</i></td><td><i>Capacity</i></td><td><i>Approved</i></td></tr>";
                    for (MultiMeeting m : iEvent.getMultiMeetings()) {
                        message += "<tr><td>";
                        message += m.getDays()+" "+new SimpleDateFormat("MM/dd").format(m.getMeetings().first().getMeetingDate());
                        message += (m.getMeetings().size()>1?" - "+new SimpleDateFormat("MM/dd").format(m.getMeetings().last().getMeetingDate()):"");
                        message += "</td><td>";
                        message += m.getMeetings().first().startTime()+" - "+m.getMeetings().first().stopTime();
                        message += "</td><td>";
                        message += (m.getMeetings().first().getLocation()==null?"":" "+m.getMeetings().first().getLocation().getLabel());
                        message += "</td><td>";
                        message += (m.getMeetings().first().getLocation()==null?"":" "+m.getMeetings().first().getLocation().getCapacity());
                        message += "</td><td>";
                        if (m.isPast()) {
                            message += "";
                        } else if (m.getMeetings().first().getApprovedDate()==null) {
                            message += "<i>Waiting Approval</i>";
                        } else {
                            message += new SimpleDateFormat("MM/dd").format(m.getMeetings().first().getApprovedDate());
                        }
                        message += "</td></tr>";
                    }
                    message += "</table></td></tr>";
                }
            
                message += "<tr><td colspan='2' style='border-bottom: 1px #2020FF solid; font-variant:small-caps;'>";
                message += "<br><font size='+1'>All Notes of "+iEvent.getEventName()+"</font>";
                message += "</td></tr><tr><td colspan='2'>";
                message += "<table border='0' width='100%' cellspacing='0' cellpadding='3'>";
                message += "<tr><td><i>Date</i></td><td><i>Action</i></td><td><i>Meetings</i></td><td><i>Note</i></td></tr>";
                for (EventNote note : new TreeSet<EventNote>(iEvent.getNotes())) {
                    message += "<tr style=\"background-color:"+EventNote.sEventNoteTypeBgColor[note.getNoteType()]+";\" valign='top'>";
                    message += "<td>"+new SimpleDateFormat("MM/dd hh:mmaa").format(note.getTimeStamp())+"</td>";
                    message += "<td>"+EventNote.sEventNoteTypeName[note.getNoteType()]+"</td>";
                    message += "<td>"+note.getMeetingsHtml()+"</td>";
                    message += "<td>"+(note.getTextNote()==null?"":note.getTextNote().replaceAll("\n", "<br>"))+"</td>";
                    message += "</tr>";
                }
                message += "</table></td></tr>";
            }
            
            message += "<tr><td colspan='2'>&nbsp;</td></tr>";
            message += "<tr><td colspan='2' style='border-top: 1px #2020FF solid;' align='center'>";
            message += "This email was automatically generated at ";
            message += request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
            message += ",<br>by UniTime "+Constants.VERSION+"."+Constants.BLD_NUMBER.replaceAll("@build.number@", "?");
            message += " (Univesity Timetabling Application, http://www.unitime.org).";
            message += "</td></tr></table>";
            
            MimeBodyPart text = new MimeBodyPart(); text.setContent(message, "text/html");
            Multipart body = new MimeMultipart(); body.addBodyPart(text);
            
            String to = "";
            if (iEvent.getMainContact()!=null && iEvent.getMainContact().getEmailAddress()!=null) {
                mail.addRecipient(RecipientType.TO, new InternetAddress(iEvent.getMainContact().getEmailAddress(),iEvent.getMainContact().getName()));
                to = "<a href='mailto:"+iEvent.getMainContact().getEmailAddress()+"'>"+iEvent.getMainContact().getShortName()+"</a>";
            }
            if (iEvent.getEmail()!=null && iEvent.getEmail().length()>0) {
                for (StringTokenizer stk = new StringTokenizer(iEvent.getEmail(),";:,\n\r\t");stk.hasMoreTokens();) {
                    String email = stk.nextToken();
                    mail.addRecipient(RecipientType.CC, new InternetAddress(email));
                    if (to.length()>0) to+=", ";
                    to += email;
                }
            }
            if (iEvent.getSponsoringOrganization()!=null && iEvent.getSponsoringOrganization().getEmail()!=null && iEvent.getSponsoringOrganization().getEmail().length()>0) {
                mail.addRecipient(RecipientType.TO, new InternetAddress(iEvent.getSponsoringOrganization().getEmail(),iEvent.getSponsoringOrganization().getName()));
                if (to.length()>0) to+=", ";
                to += "<a href='mailto:"+iEvent.getSponsoringOrganization().getEmail()+"'>"+iEvent.getSponsoringOrganization().getName()+"</a>";
            }
            
            mail.setSentDate(new Date());
            mail.setContent(body);
            
            Transport.send(mail);
            
            request.getSession().setAttribute(Constants.REQUEST_MSSG, subject+" Confirmation email sent to "+to+".");
        } catch (Exception e) {
            sLog.error(e.getMessage(),e);
            request.getSession().setAttribute(Constants.REQUEST_WARN, (subject==null?"":subject+" ")+"Unable to send confirmation email, reason: "+e.getMessage());
        }
    }
}
