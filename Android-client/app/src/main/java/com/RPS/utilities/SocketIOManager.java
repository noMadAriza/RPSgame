package com.RPS.utilities;


import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketIOManager implements SocketManager {
    private static volatile SocketIOManager instance;
    private static final String URI = "http://35.158.32.95:3000";
    private final Socket socket;
    private SocketIOManager(){
        this.socket = connect(URI);
        instance = this;
    }

    public static synchronized SocketIOManager getInstance(){
        if (instance == null) {
            synchronized (SocketIOManager.class) {
                if(instance == null)
                    instance = new SocketIOManager();
            }
        }
        return instance;
    }

    private Socket connect(String URI){
        Socket socket = null;
        try {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            IO.Options options = IO.Options.builder()
                    .setReconnection(true)
                    .setReconnectionDelay(400)
                    .setReconnectionDelayMax(1500)
                    .setRandomizationFactor(0)
                    .build();
            options.query = "user_id=" + mAuth.getUid();
            socket = IO.socket(URI,options);
            socket.connect();
        }catch (Exception e){
            e.printStackTrace();
        }
        return socket;
    }

    @Override
    public void disconnect() {
        socket.disconnect();
    }

    @Override
    public Socket getSocket() {
        return socket;
    }


    public void ClientToServer(JSONObject json){

    }

}
