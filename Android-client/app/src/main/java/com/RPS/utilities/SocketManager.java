package com.RPS.utilities;

import io.socket.client.Socket;

public interface SocketManager {

    void disconnect();

    Socket getSocket();
}

