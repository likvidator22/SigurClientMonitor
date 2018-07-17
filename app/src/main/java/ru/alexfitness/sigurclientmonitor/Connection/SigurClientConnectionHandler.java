package ru.alexfitness.sigurclientmonitor.Connection;

import ru.alexfitness.sigurclientmonitor.Sigur.SigurEvent;

public interface SigurClientConnectionHandler {

    public void handleNewEvent(SigurEvent sigurEvent);
    public void handleClientShutDown();
    public void handleClientStartUp();

}
