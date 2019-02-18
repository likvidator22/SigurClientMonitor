package ru.alexfitness.sigurclientmonitor.Sigur;

import java.io.Serializable;

public enum SigurEventType implements Serializable{
    SUCCESS_ENTER("24", "Доступ разрешен"),
    FAIL_FACE_SCAN("67", "Доступ запрещен. Лицо не опознано"),
    FAIL_WRONG_CODE("10", "Доступ запрещен. Неизвестный код пропуска"),
    FAIL_EXPIRED("15", "Доступ запрещен. Срок действия ключа истек"),
    FAIL_TIME_LIMIT("13", "Доступ запрещен. Нет допуска в это время"),
    UNKNOWN("","");

    private final String code;
    private final String description;

    SigurEventType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static SigurEventType getByCode(String code){
        SigurEventType[] allEvents = SigurEventType.values();
        for(SigurEventType sigurEventType:allEvents){
            if(sigurEventType.code.equals(code)){
                return sigurEventType;
            }
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }
}
