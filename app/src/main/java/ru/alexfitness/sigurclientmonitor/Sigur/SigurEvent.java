package ru.alexfitness.sigurclientmonitor.Sigur;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SigurEvent {

    //EVENT_CE "2018-06-08 14:42:12" 24 1 675 1 W26 013 62523
    private Date date;
    private SigurEventType eventType;
    private String senderID;
    private int objectID;
    private int direction;
    private String key;
    private String presentation;

    public SigurEvent(String eventString) {

        this.presentation = eventString;

        String[] stringSplit = eventString.split(" ");

        SimpleDateFormat dateFormat = new SimpleDateFormat("\"yyyy-MM-dd HH:mm:ss\"", Locale.ENGLISH);
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(stringSplit[1]).append(" ").append(stringSplit[2]);
            setDate(dateFormat.parse(sb.toString()));
        } catch (ParseException e) {
            //
        }

        String eventTypeString = stringSplit[3];
        setEventTypeByString(eventTypeString);

        setSenderID(stringSplit[4]);
        setObjectID(Integer.parseInt(stringSplit[5]));
        setDirection(Integer.parseInt(stringSplit[6]));

        switch(stringSplit[7]) {
            case "UNKNOWN":
                setKey(null);
                break;
            case "W26":
                StringBuilder sb = new StringBuilder();
                sb.append(stringSplit[8]).append(",").append(stringSplit[9]);
                setKey(sb.toString());
                break;
            default:
                setKey(null);
        }
    }

    private void setEventTypeByString(String typeString){
        this.setEventType(SigurEventType.getByCode(typeString));
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public SigurEventType getEventType() {
        return eventType;
    }

    public void setEventType(SigurEventType eventType) {
        this.eventType = eventType;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public int getObjectID() {
        return objectID;
    }

    public void setObjectID(int objectID) {
        this.objectID = objectID;
    }

    @Override
    public String toString() {
        return this.presentation;
    }

}