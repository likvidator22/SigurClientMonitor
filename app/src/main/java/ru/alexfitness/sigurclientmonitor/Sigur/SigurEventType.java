package ru.alexfitness.sigurclientmonitor.Sigur;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public enum SigurEventType implements Serializable{
    SUCCESS_ENTER("24", "Доступ разрешен"),
    FAIL_FACE_SCAN("67", "Доступ запрещен. Лицо не опознано"),
    FAIL_WRONG_CODE("10", "Доступ запрещен. Неизвестный код пропуска"),
    FAIL_EXPIRED("15", "Доступ запрещен. Срок действия ключа истек"),
    FAIL_TIME_LIMIT("13", "Доступ запрещен. Нет допуска в это время");

    private final String code;
    private final String description;

    SigurEventType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static final SigurEventType getByCode(String code){
        SigurEventType[] allEvents = SigurEventType.values();
        for(SigurEventType sigurEventType:allEvents){
            if(sigurEventType.code.equals(code)){
                return sigurEventType;
            }
        }
        return null;    }

    public static final ArrayList<String> getDescriptions(){
        ArrayList<String> result = new ArrayList<String>();
        for(SigurEventType sigurEventType:values()){
            result.add(sigurEventType.description);
        }
        return result;
    }

    public static final HashMap<SigurEventType, String> getEventTypeDescriptionsMap(){
        HashMap<SigurEventType, String> result = new HashMap<>();
        for(SigurEventType sigurEventType:values()){
            result.put(sigurEventType, sigurEventType.description);
        }
        return result;
    }

    @Override
    public String toString() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }
}
