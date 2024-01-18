package com.RPS.firebaselogin.utilities;

import io.socket.client.Socket;

public interface SocketManager {

    void disconnect();

    Socket getSocket();
}

