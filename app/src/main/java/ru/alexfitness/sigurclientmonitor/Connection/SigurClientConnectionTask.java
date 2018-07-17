package ru.alexfitness.sigurclientmonitor.Connection;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import ru.alexfitness.sigurclientmonitor.Sigur.SigurEvent;
import ru.alexfitness.sigurclientmonitor.Sigur.SigurTextProtocol;

public class SigurClientConnectionTask extends AsyncTask<Void, String, Void> {

    private SharedPreferences properties;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private SigurClientConnectionHandler handler;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    private ArrayList<String> controllers;
    private int direction;
    private int connectionTimeout;
    private int reconnectAttempts;

    public SigurClientConnectionTask(SharedPreferences prop){
        super();
        setProperties(prop);
    }

    @Override
    protected void onPreExecute() {
        String controllersString = properties.getString("controller_pref", "");
        String[] controllersStringSplit = controllersString.split(";");
        controllers = new ArrayList<>();
        for(String controllerString:controllersStringSplit){
            controllers.add(controllerString);
        }

        direction = Integer.parseInt(properties.getString("direction_pref", "0"));
        connectionTimeout = Integer.parseInt(properties.getString("connection_timeout_pref", "0"));
        reconnectAttempts = Integer.parseInt(properties.getString("reconnect_attempts_pref", "0"));

        handler.handleClientStartUp();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (!initResources()) {
            return null;
        }
        if (!loginToServer()) {
            return null;
        }
        String response = null;
        while (isRunning.get() & !isCancelled()) {
            try {
                response = reader.readLine();
            } catch (Exception e) {
                response = null;
                if(isCancelled()){
                    break;
                }
                if (!reconnect()) {
                    freeResources();
                    isRunning.set(false);
                    continue;
                }
            }
            if (response != null) {
                publishProgress(response);
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        Log.i("", values[0]);

        SigurEvent sigurEvent = new SigurEvent(values[0]);

        if (checkHandlePreferences(sigurEvent)) {
            handler.handleNewEvent(sigurEvent);
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        handler.handleClientShutDown();
    }


    public boolean checkHandlePreferences(SigurEvent event){
        boolean result;
        result = controllers.contains(event.getSenderID()) & (direction==0 || event.getDirection()==direction);
        return result;
    }

    private boolean initResources() {
        try {
            String serverAddress = properties.getString("host_pref","");
            int serverPort = Integer.parseInt((properties.getString("port_pref","")));
            socket = new Socket(InetAddress.getByName(serverAddress), serverPort);
            socket.setSoTimeout(30*1000);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (Exception e) {
            logError(e.getMessage());
            return false;
        }
        return true;
    }

    private void freeResources() {
        try {
            if (getReader() != null) {
                getReader().close();
            }
        } catch (IOException e) {
            logError(e.getMessage());
        }
        if (getWriter() != null) {
            getWriter().close();
        }
        if (getSocket() != null && !getSocket().isClosed()) {
            try {
                getSocket().close();
            } catch (IOException e) {
                logError(e.getMessage());
            }
        }
    }

    private boolean loginToServer() {
        String loginCommand = SigurTextProtocol.getSigurLoginCommand(properties.getString("version_pref",""), properties.getString("login_pref",""), properties.getString("pwd_pref", ""));
        writer.println(loginCommand);
        writer.flush();
        String response;
        try {
            response = reader.readLine();
            if(!SigurTextProtocol.succesResponse(properties.getString("version_pref",""), response)){
                return false;
            }
            logInfo(response);
        } catch (IOException e) {
            logError(e.getMessage());
            return false;
        } // read OK
        String subscribeCommand = SigurTextProtocol.getSubscribeCommand(properties.getString("version_pref",""));
        writer.println(subscribeCommand);
        writer.flush();
        try {
            response = reader.readLine();
            if(!SigurTextProtocol.succesResponse(properties.getString("version_pref",""), response)){
                return false;
            }
            logInfo(response);
        } catch (IOException e) {
            logError(e.getMessage());
            return false;
        } // read OK
        return true;
    }

    private boolean reconnect() {
        int attempts = reconnectAttempts;
        boolean result = false;
        boolean connectionState;
        try {
            result = !socket.getInetAddress().isReachable(1000);
        } catch (IOException e) {
            result = false;
        }
        while (attempts > 0 || reconnectAttempts==0) {
            try {
                logInfo(String.format("Check reachable: %d", attempts));
                connectionState = !socket.getInetAddress().isReachable(connectionTimeout);
                if (!connectionState | !result) {
                    logInfo("Connection lost");
                    freeResources();
                    result = initResources() & loginToServer();
                } else {
                    logInfo("Server is reachable");
                    break;
                }
            } catch (IOException e) {
                logError(e.getMessage());
                result = false;
            }
            attempts--;
        }
        return result;
    }

    public void setProperties(SharedPreferences properties) {
        this.properties = properties;
    }

    public void logInfo(String logString){
        Log.i("Connection",logString);
    }

    public void logError(String logString){
        Log.e("Connection", logString);
    }

    public Socket getSocket() {
        return socket;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public void setHandler(SigurClientConnectionHandler handler) {
        this.handler = handler;
    }
}