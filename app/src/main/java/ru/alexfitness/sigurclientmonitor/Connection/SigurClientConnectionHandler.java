package ru.alexfitness.sigurclientmonitor.Connection;

import ru.alexfitness.sigurclientmonitor.Sigur.SigurEvent;

public interface SigurClientConnectionHandler {

    void handleNewEvent(SigurEvent sigurEvent);
    void handleClientShutDown();
    void handleClientStartUp();
    void handleConnectionProblem();

}
