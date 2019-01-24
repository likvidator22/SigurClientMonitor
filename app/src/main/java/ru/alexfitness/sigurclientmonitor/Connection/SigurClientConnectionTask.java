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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import ru.alexfitness.sigurclientmonitor.Sigur.SigurEvent;
import ru.alexfitness.sigurclientmonitor.Sigur.SigurTextProtocol;

public class SigurClientConnectionTask extends AsyncTask<Void, String, Void> {

    private static final String RECONNECT = "r";

    private SharedPreferences properties;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private SigurClientConnectionHandler handler;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

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
        Collections.addAll(controllers, controllersStringSplit);

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

        isRunning.set(true);

        String response;
        while (isRunning.get() && !isCancelled()) {
            try {
                response = reader.readLine();
            } catch (IOException e) {
                //
                logError(e.toString());
                response = null;
                if (!reconnect()) {
                    freeResources();
                    isRunning.set(false);
                    break;
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
        if(values[0].equals(RECONNECT)){
            handler.handleConnectionProblem();
        } else {
            logInfo(values[0]);
            SigurEvent sigurEvent = new SigurEvent(values[0]);
            if (checkHandlePreferences(sigurEvent)) {
                handler.handleNewEvent(sigurEvent);
            }
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        handler.handleClientShutDown();
    }

    private boolean checkHandlePreferences(SigurEvent event){
        boolean result;
        result = controllers.contains(event.getSenderID()) & (direction==0 || event.getDirection()==direction);
        return result;
    }

    private boolean initResources() {
        try {
            String serverAddress = properties.getString("host_pref","");
            int serverPort = Integer.parseInt((properties.getString("port_pref","")));

            if (serverIsReachable(serverAddress, serverPort, 10)) {
                socket = new Socket(InetAddress.getByName(serverAddress), serverPort);
                socket.setSoTimeout(connectionTimeout * 1000);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            } else {
                return false;
            }
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
            //logInfo(response);
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
            //logInfo(response);
        } catch (IOException e) {
            logError(e.getMessage());
            return false;
        } // read OK
        return true;
    }

    private boolean reconnect() {
        int attempts = reconnectAttempts;
        while (attempts > 0 || reconnectAttempts==0) {
            publishProgress(RECONNECT);
            if(initResources() & loginToServer()){
                    return true;
            }
            if(attempts>0){attempts--;}
        }
        return false;
    }

    private boolean serverIsReachable(String server,int port, int connectionTimeout){

        try {
            Socket s = new Socket();
            s.connect(new InetSocketAddress(server, port), connectionTimeout * 1000);
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void setProperties(SharedPreferences properties) {
        this.properties = properties;
    }

    private void logInfo(String logString){
        if(logString == null){
            logString = "";
        }
        Log.i("Connection",logString);
    }

    private void logError(String logString){
        if(logString == null){
            logString = "";
        }
        Log.e("Connection", logString);

    }

    private Socket getSocket() {
        return socket;
    }

    private PrintWriter getWriter() {
        return writer;
    }

    private BufferedReader getReader() {
        return reader;
    }

    public void setHandler(SigurClientConnectionHandler handler) {
        this.handler = handler;
    }
}