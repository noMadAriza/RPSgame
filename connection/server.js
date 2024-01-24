var Mutex = require('async-mutex').Mutex;
const express = require('express'); //requires express module
const socket = require('socket.io'); //requires socket.io module
const fs = require('fs');
const { json } = require('express');
const { parse } = require('path');
const { stringify } = require('querystring');
const { availableParallelism } = require('os');
const app = express();
var PORT = process.env.PORT || 3000;
const server = app.listen(PORT); //tells to host server on localhost:3000

//Playing variables:
app.use(express.static('public')); //show static files in 'public' directory
console.log('Server is running');

const io = socket(server, {
  connectionStateRecovery: {
    maxDisconnectionDuration: 2 * 60 * 1000,
    skipMiddlewares: true,
  }
});
const BOARD_SIZE = 7;
const Player = {
  EMPTY_CELL: 0,
  ROCK: 1,
  SCISSORS: 2,
  PAPER: 3,
  FLAG: 4,
  TRAP: 5,
  UNKNOWN: 6
}
const MAX_PLAYERS_LOBBY = 2;
const Color = {
  BLUE: 'BLUE',
  RED: 'RED'
}
var lobbyCnt = 0;
const MAX_LOBBIES = 100000;
const mapMutex = new Mutex();  //mutex for accesing the lobbiesMutex map
const lobbiesMutex = new Map(); //all lobies mutex
const clients = new Map();  //for each user_id it will have lobbyId associated 
const clientsMutex = new Mutex(); //mutex for the map of all users

//Socket.io Connection------------------
io.on("connection", async (socket) => {
  const release = await clientsMutex.acquire();
  var userID = socket.handshake.query.user_id;
  if(clients.has(userID) && clients.get(userID) != undefined){ //meaning the player is in a game!
    console.log("here");
    socket.join(clients.get(userID));
    let colorToHide;
    if(io.sockets.adapter.rooms[lobbyID].redPlayer == userID)
      colorToHide = Color.BLUE;
    else
      colorToHide = Color.RED;
    let data = {
      board : hidePlayers(io.sockets.adapter.rooms[lobbyID].board,colorToHide)
    }
    socket.emit("updateFromServer",data);
  }
  else
    clients.set(clients.set(userID,undefined));
  release();
  console.log("connected user_id:" + userID);
  //creates a new lobby 
  socket.on("newLobby", async(callback) => {
    const release = await mapMutex.acquire();
    try{
      lobbyCnt++;
      if(lobbyCnt >= MAX_LOBBIES)
        lobbyCnt = 1;
      lobbyID = lobbyCnt;
      lobbiesMutex.set(lobbyID,new Mutex());
    }finally {(release()) }
    console.log("created a new lobby named: ",lobbyID);
    callback({
      lobbyID: lobbyID
    });
  });
  /* gets a lobbyID and joins it and returning the color of the player*/
  socket.on("joinLobby", async (lobbyID,callback) =>{
    var color = null;
    await clientsMutex.acquire();
    clients.set(userID,lobbyID);
    clientsMutex.release();
    if (io.sockets.adapter.rooms[lobbyID] == undefined) {
      board = [];
      makeBoard(board,BOARD_SIZE);
      io.sockets.adapter.rooms[lobbyID] = {
        creator: null,
        count: 0,
        redPlayer: null,
        bluePlayer: null,
        redChoice: undefined,
        blueChoice : undefined,
        board: board
      };
    }
      if(io.sockets.adapter.rooms[lobbyID].redPlayer == null){
        color = Color.RED;
        io.sockets.adapter.rooms[lobbyID].creator = userID;
        io.sockets.adapter.rooms[lobbyID].redPlayer = userID;
        socket.join(lobbyID);
      }
      else if(io.sockets.adapter.rooms[lobbyID].bluePlayer == null){
        color = Color.BLUE;
        io.sockets.adapter.rooms[lobbyID].bluePlayer = userID;
        socket.join(lobbyID);
      }
      callback({
        color: color
      });
  });
  /* update server */
  socket.on("updateFromClient",async (args,lobbyID,color) => {
    var matrixJson = JSON.parse(args);
    console.log("before hiding " + color);
    await mapMutex.acquire(); //acquires the mutex for the map of lobbies
    if(lobbiesMutex.has(lobbyID))
      await lobbiesMutex.get(lobbyID).acquire(); //acquires the mutex of the lobby
    mapMutex.release();
    if(lobbiesMutex.has(lobbyID)){
      //if the lobby really exist
      board = io.sockets.adapter.rooms[lobbyID].board;
      for (let i = 0; i < BOARD_SIZE; i++) {
        let rowJson = matrixJson.values[i];
        for (let j = 0; j < BOARD_SIZE; j++) {
          if(rowJson.values[j].nameValuePairs.value == 0)
            board[i][j] = {value: 0, visible: true, color: "default"};
          else if(rowJson.values[j].nameValuePairs.value != Player.UNKNOWN){
            let value = rowJson.values[j].nameValuePairs.value;
            let visible = rowJson.values[j].nameValuePairs.visible;
            let color = rowJson.values[j].nameValuePairs.color;
            board[i][j] = {value,visible,color};
          }
        } 
      }
      console.log(board);
      console.log("-------------------------------------------------");
      console.log("after hiding")
      console.log(hidePlayers(board,color));
      var data = {
        board: hidePlayers(board,color)
      };
      socket.to(lobbyID).emit("updateFromServer", data);
      lobbiesMutex.get(lobbyID).release()
    }
  });
  socket.on("getPlayer",(lobbyID,row,column,callback) => {
    let player = io.sockets.adapter.rooms[lobbyID].board[row][column];
    console.log("here!");
    console.log("row :" + row + "column: " + column);
    console.log(player);
    callback({
      value: player.value,
      visible: player.visible,
      color: player.color
    });
  });
  /* in case a war occurs */
  socket.on("tie", async (lobbyID,callback) => {
    io.to(lobbyID).emit("showWarMenu");
    const res = await war(lobbyID,null,null);
    io.sockets.adapter.rooms[lobbyID].redChoice = undefined;
    io.sockets.adapter.rooms[lobbyID].blueChoice = undefined;
    console.log("afterTie");
    callback({
      redPlayer: res.redType,
      bluePlayer: res.blueType
    });
  });
  // gets the player choice from the war menu
  socket.on("clickedOnMenu",(lobbyID,color,type) => {
    war(lobbyID,color,type);
  });
  /*every time a player is in the lobby it asks for the game to start.
  when MAX_PLAYERS_LOBBY players asks it in the same server it will start 
  so it must that every player that joins the game will send startGame to server*/
  socket.on("startGame",async(lobbyID) => {
    await lobbiesMutex.get(lobbyID).acquire();
    io.sockets.adapter.rooms[lobbyID].count++;
    if(io.sockets.adapter.rooms[lobbyID].count == MAX_PLAYERS_LOBBY)
      io.to(lobbyID).emit("startGame");
    lobbiesMutex.get(lobbyID).release();
  });
  //the game in lobbyID ends and gets color of the winner
  socket.on("endGame", async(lobbyID,color) => {
    console.log("sends finish");
    await sleep(500);
    await mapMutex.acquire();
    io.to(lobbyID).emit("winner",color);
    mapMutex.release();
    closeLobby(socket,lobbyID);
  });
  //gets list of users_id and has a callback returning 2 arrays of connected and offline users from the list given (user_id,socket_id)
  socket.on("getConnectedClients", async(list,callback) => {
    let connected = [];
    let offline = [];
    for(let i = 0; i < list.length; i++){
      console.log(list[i]);
      if(clients.has(list[i].user_id)){
        connected.push(list[i]);
      }
      else
        offline.push(list[i]);
    }
    callback({
      connected: connected,
      offline: offline
    });
  });

  //return all the lobbies that are avaliable
  socket.on("getAllLobbiesAvailable", async (callback) => {
    const release = await mapMutex.acquire();
    avaliableLobbies = [];
    creators = [];
    try {
      for (const lobbyID of lobbiesMutex.keys()) {
        if(io.sockets.adapter.rooms[lobbyID].bluePlayer == null){ //means there's only one player
          avaliableLobbies.push(lobbyID);
          creators.push(io.sockets.adapter.rooms[lobbyID].creator);
        }
      }
    } finally {
    release();
    }
    console.log(avaliableLobbies);
    callback({
      lobbies: avaliableLobbies,
      creators: creators
    });
  });
  /*  before a player disconnects from the server:
  closing the lobby he was in and consider the other player in the lobby as the winner
  making the client offline */
  socket.on("disconnected",() => {
    const itarator = io.sockets.adapter.sids.get(socket.id).keys();
    const size = io.sockets.adapter.sids.get(socket.id).size;
    for (let i = 0; i < size; i++) {
      let lobbyID = itarator.next().value;
      if (lobbyID != socket.id) {// Ignore the default room
        socket.to(lobbyID).emit("winner","disconnected");
        closeLobby(socket,lobbyID);
      }
    }
    deleteUser(userID);
  });

});

//gets a whole number between min and max given
function getRandomArbitrary(min, max) {
  return Math.floor(Math.random() * (max - min) + min);
}

//intializing the board
function makeBoard(board,BOARD_SIZE){
  for (let i = 0; i < BOARD_SIZE; i++) {
    board[i] = [];
    for (let j = 0; j < BOARD_SIZE; j++) {
      board[i][j] = {value: 0,visible: true, color: "default"};
    }
  }
}
//hide all the players of the color given and with no visibility
function hidePlayers(matrix,color){
  var res = [];
  for (let row = 0; row < matrix.length;row++){
    let innerArray = [];
    for (let column = 0; column < matrix.length;column++){
      if(matrix[row][column].visible == false && matrix[row][column].color == color){
        innerArray.push({value: Player.UNKNOWN, visible: true, color: color});
      }
      else{
        innerArray.push(matrix[row][column]);
      }
    }
    res.push(innerArray);
  }
  return res;
}
// returns a promise that will resolve only after 2 players clicked the warMenu!
async function war(lobbyID,color,type){
  let mutex = lobbiesMutex.get(lobbyID);
  await mutex.acquire();
  if(color == Color.BLUE)
    io.sockets.adapter.rooms[lobbyID].blueChoice = type;
  else if (color == Color.RED)
    io.sockets.adapter.rooms[lobbyID].redChoice = type;
  mutex.release();
  if(color != null)
    return;
  return new Promise((resolve, reject) => {
    try{
      const interval = setInterval(async() => {
        await mutex.acquire();
        let redType = io.sockets.adapter.rooms[lobbyID].redChoice;
        let blueType = io.sockets.adapter.rooms[lobbyID].blueChoice;
        if (redType != undefined && blueType != undefined) {
          clearInterval(interval);
          resolve({redType,blueType})
        }
        mutex.release();
      }, 3000); // Check every 3 seconds
    }catch{
      reject("fail");
    }finally{
      mutex.release();
    }
  });
}
function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

// closes lobby number lobbyID
async function closeLobby(socket,lobbyID){
  console.log("delete lobby " + lobbyID);
  socket.leave(lobbyID);
  await mapMutex.acquire();
  io.sockets.adapter.rooms[lobbyID] = undefined;
  lobbiesMutex.delete(lobbyID);
  mapMutex.release();
}

//deletes the user from being an active user
async function deleteUser(userID){
  if(userID != undefined && userID != null){
    await clientsMutex.acquire();
    clients.delete(userID);
    clientsMutex.release();
  }
}