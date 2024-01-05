package com.example.firebaselogin.utilities;

import java.net.URISyntaxException;

import io.socket.client.Socket;

public interface SocketManager {

    void disconnect();

    Socket getSocket();
}

