package ru.alexfitness.sigurclientmonitor.Sigur;

public abstract class SigurTextProtocol {

    public static final String VERSION_1_8 = "1.8";
    public static final String LOGIN_COMMAND_1_8 = "LOGIN";
    public static final String SUBSCRIBE_COMMAND_1_8 = "SUBSCRIBE CE";
    public static final String RESPONSE_SUCCESS_TEXT_1_8 = "OK";

    public static String getSigurLoginCommand(String version, String login, String password){
        StringBuilder result = new StringBuilder();
        if(version.equals(VERSION_1_8)){
            result.append(LOGIN_COMMAND_1_8).append(" ").append(version).append(" ").append(login).append(" ").append(password);
        }
        return result.toString();
    }

    public static String getSubscribeCommand(String version){
        StringBuilder result = new StringBuilder();
        if(version.equals(VERSION_1_8)){
            result.append(SUBSCRIBE_COMMAND_1_8);
        }
        return result.toString();
    }

    public static boolean succesResponse(String version, String response){
        if(version.equals(VERSION_1_8)){
            return response.equals(RESPONSE_SUCCESS_TEXT_1_8);
        }
        return false;
    }
}
