var ws;
var name;
var log;

var MAX_PLAYERS;
var host;
var cardSelectedRigGame = [];
var clicked = 0;
var playerCardsRigged = {};
var currentRigPlayer;
var sentRequestServerRig = false;
var allCardsToRig;
var cardsToRemoveAfterUsed = [];
var indexToBeRemoved = [];
var cardsSelectedToBePlayed = [];
var cardsSelectedQuest = {};
var selectedCards = false;
var myTimer;
var questSponsorTimer;
var hostForRound;
var currentStoryCard = null;
var totalStages = 1;
var currentNumStages = 1; // needs to be reset between rounds
var serverCurrentStage = 0;
var stringToCompareQuest = "";
var sound;

// function connect() {
// 	ws = new WebSocket('ws://localhost:8080/game');
// 	ws.onmessage = function(data){
// 	    console.log("this is what is coming from server unparsed")
// 	    for(var i in data){
// 	        console.log(data[i]);
//         }
// 	    var parsedData = JSON.parse(data.data);
// 	    console.log(parsedData);
// 	    if(parsedData.method == "alertingUser"){
// 	        alertUser(parsedData.body);
//         } else if(parsedData.method == "greeting"){
//             showGreeting(parsedData.body);
//         }
// 	}
// 	 setConnected(true);

function alertUser(message){
    alert(message);
}

// function disconnect() {
//     if (ws != null) {
//         ws.close();
//     }
//     setConnected(false);
//     console.log("Disconnected");
// }

function sendName() {
	var data = JSON.stringify({'method':'newPlayer','body': $("#name").val()})
    ws.send(data);
}

function showGreeting(message) {
    $("#individualPlayer").append("<tr><td> " + message + "</td></tr>");
}

function sound(src) {
    this.sound = document.createElement("audio");
    this.sound.src = src;
    this.sound.setAttribute("preload", "auto");
    this.sound.setAttribute("controls", "none");
    this.sound.style.display = "none";
    document.body.appendChild(this.sound);
    this.sound.loop = true;
    this.play = function () {
        this.sound.play();
    }
    this.stop = function () {
        this.sound.pause();
    }
}

function pageLoaded(){
    sound = new sound("opening.mp3");
    sound.play();
    name = prompt("Please enter your name", "");
    while(name == "" || name == null){
        name = prompt("Please enter your name", "");
    }
    showToast("success", "welcome "+ name);
    if(gameCreated == true){

    }
    var data = JSON.stringify({'method':'newPlayer','body': name})
    ws = new WebSocket('ws://localhost:8091/game');
    ws.onmessage = function(data){
        selectedCards = false;
        cardsSelectedToBePlayed = [];
        var parsedData = JSON.parse(data.data);
        for(var i in parsedData){
            console.log("Incoming data from server : "+parsedData[i]);
        }
        if(parsedData.method == "alertingUser"){
            alertUser(parsedData.body);
        } else if(parsedData.method == "greeting"){
            showGreeting(parsedData.body);
        } else if(parsedData.method == "gameCreated"){
            gameCreated(parsedData);
        } else if(parsedData.method == "gameCreatedSuccessfully"){
            gameCreatedSuccessfully(parsedData);
        } else if(parsedData.method == "playerJoined"){
            playerJoined(parsedData.numPlayers);
        } else if(parsedData.method == "updateUsers"){
            update(message);
        } else if(parsedData.method == "noGame"){
            noGame();
        } else if(parsedData.method == "startTheGame"){
            startTheGame(parsedData);
        } else if(parsedData.method == "addRigCards"){
            allCardsToRig = parsedData.body;
            sentRequestServerRig = true;
            addRigCards(parsedData.body);
        } else if(parsedData.method == "startedTheActualGame"){
            startedTheRealGame(parsedData.body, parsedData.scoreboard);
        } else if(parsedData.method == "updateCards"){
            displayCards(parsedData.body, parsedData.method);
        } else if(parsedData.method == "tournamentStarted"){
            startTournament(parsedData);
        } else if(parsedData.method == "tournamentWinner"){
            startTournamentTie(parsedData);
        } else if(parsedData.method == "questStarted"){
            startQuest(parsedData);
        } else if(parsedData.method == "canSponsorQuest"){
            setUpQuest(parsedData);
        } else if(parsedData.method == "sponsorFound"){
            sponsorFound(parsedData);
        } else if(parsedData.method == "participateInQuest"){
            askPlayerToParticipateInQuest(parsedData.body, parsedData.questHost);
        } else if(parsedData.method == "askingOtherPlayersToParcipateInQuest"){
            askingOtherPlayersToParticipateInQuest(parsedData);
        }else if(parsedData.method == "updateCardsForQuest"){
            updateCardsForQuest(parsedData);
        } else if(parsedData.method == "questInProgress"){
            console.log("parsedData.isTest is ", parsedData.isTest);
            if(parsedData.isTest === true){
                bidder(parsedData);
            } else if(parsedData.isTest === false){
                playStage(parsedData);
            }
        }else if(parsedData.method == "biddingCenter"){
            bidder(parsedData)
        }else if(parsedData.method == "notify"){
            showToast(parsedData.type, parsedData.message);
        } else if(parsedData.method == "events"){
            showEvent(parsedData);
        }
        else {
            console.log("Error no method handler for incoming method");
        }

    }
    ws.onopen = function(e) {
        ws.send(data);
    };
}

function gameSettings(){
    $("#createGameButtons").empty();
    $("#createGameButtons").append(
        "<div align=\"center\">\n" +
        "    Game Name: <input type=\"text\" id=\"gameName\" value='abcd' onfocus=\"this.value=''>\n" +
        "    </br>\n" +
        "    <input type=\"submit\" value=\"Submit\">\n" +
        "    </br>\n" +
        "    Number of Human Players:\n" +
        "    <select id=\"humanPlayers\">\n" +
        "        <option value=\"4\">4</option>\n" +
        "        <option value=\"1\">1</option>\n" +
        "        <option value=\"2\">2</option>\n" +
        "        <option value=\"3\">3</option>\n" +
        // "        <option value=\"4\">4</option>\n" + uncomment this after wards
        "    </select>\n" +
        "    </br>\n" +
        "    Number of AI Players:\n" +
        "    <select id=\"aiPlayers\">\n" +
        "        <option value=\"0\">0</option>\n" +
        "        <option value=\"1\">1</option>\n" +
        "        <option value=\"2\">2</option>\n" +
        "        <option value=\"3\">3</option>\n" +
        "    </select>\n" +
        "    </br>\n" +
        "    <button type=\"button\" onclick=\"createGame()\">Create Game</button>\n" +
        "</div>");
}

function createGame(){
    MAX_PLAYERS = $("#humanPlayers").find(":selected").text();
    var bodyOfJson =
        {
            'gameName':$("#gameName").val(),
            'humanPlayers':$("#humanPlayers").find(":selected").text(),
            'aiPlayers':$("#aiPlayers").find(":selected").text(),
            'host':name
        };
    var jsonObject = JSON.stringify({'method':'createGame', 'body': JSON.stringify(bodyOfJson)});
    ws.send(jsonObject);

}

function gameCreated(message){
    var gameRoomInfo = message.body;
    //console.log("game room info is" + gameRoomInfo);
    var numPlayers = message.numPlayers;
    //console.log("game room info is" + numPlayers);
   // gameCreated = true;
    $("#individualGameRoom").append(
        "<tr id='rowPlayers'><td><button id = 'joinBtn' align='right' class='btn btn-default' onclick='joinGameRoom()'>Join</button>"+
        " Gameroom :" + gameRoomInfo[0].gameName + ", max players:  " + gameRoomInfo[1].humanPlayers + " , A.I players is "
        + gameRoomInfo[2].aiPlayers + ", host is " + gameRoomInfo[3].host +"</td><td>" +
        numPlayers+
        "</td></tr>");
        $("#createGame").hide();
}

function gameCreatedSuccessfully(message){
    host = true;
    $("#individualGameRoom").append("<tr id='rowPlayers'><td> Gameroom created successfully </td><td>"+message.numPlayers+"</td></tr>");
    $("#createGameButtons").empty();
}

function joinGameRoom(){
    var jsonObject = JSON.stringify({'method':'joinGameRoom','body':name});
    ws.send(jsonObject);
    $("#joinBtn").hide();
}

function playerJoined(message){
    //window.alert(message + " the game room");
    $("#rowPlayers td:nth-child(2)").text(message);
    if(host && (MAX_PLAYERS==message)){
        setTimeout(function(){
            $("#gameRoomsHtml").empty();
            $("#gameRoomsHtml").append(
                "<div class='alert alert-primary' role='alert'>" +
                " Ready to Start Game" +
                "<button id='start' onclick='startGameRequest()'>Start Game</button></div>"
            );
        }, 2500);
    }
}

function startGameRequest(){
    var data = JSON.stringify({
        'method':'startGameRequested'
    });
    //sendConsoleData("start game requested");
    ws.send(data);
}



function sendConsoleData(s){
    var jsonObject = JSON.stringify({'method':'sendConsoleData', 'body': s});
    ws.send(jsonObject);
}



function noGame(){
    alert("Max players reached, can't join game");
}

function startTheGame(message){
    if(message.body === "host"){
        $("#main-content").empty();
        $("#main-content").append(
            "<div align='left'>" +
                "<table id = 'setupScreen' class='table table-striped'> " +
                    "<thead> " +
                        "<tr>" +
                            "<th scope='col'></th>" +
                            "<th scope='col'>Name</th>" +
                            "<th scope='col'>Type</th>" +
                            "<th scope='col'></th>" +
                            "<th scope='col'>Rig Game</th>" +
                        "</tr>" +
                    "</thead>" +
                    "<tr> " +
                        "<th scope='row'>Player 1</th>" +
                        "<td><input id='p1Name' type='textbox'></td>" +
                        "<td><input id='p1Type' type='textbox'></td>" +
                        "<td><button id='p1Add' type='button' class='btn btn-primary' onclick='addCards(this.id)'>+</button><button id='p1minus' type='button' class='btn btn-primary' onclick='removeCards()'>-</button></td>" +
                        "<td><input type='checkbox' id='rigGame' data-toggle='toggle'></td>" +

                    "</tr>" +

                    "<tr>" +
                        "<td> <button id='p1Squire' value='true' style=\"background-color:greenyellow;\" onclick='rankSelect1()'>Squire </button> <button id='p1Knight' value='false' style=\"background-color:indianred;\" onclick='rankSelect2()'>Knight </button> <button id='p1CKnight' value='false' style=\"background-color:indianred;\" onclick='rankSelect3()'>C.Knight </button> </td>" +
                        "<td><input id='numShields' type='textbox' value='0' onfocus=\"this.value=''\"></td>" +
                    "</tr>" +

                    "<tr> " +
                        "<th scope='row'>Player 2</th>" +
                        "<td><input id='p2Name' type='textbox'></td>" +
                        "<td><input id='p2Type' type='textbox'></td>" +
                        "<td><button id='p2Add' type='button' class='btn btn-primary' onclick='addCards(this.id)'>+</button><button id='p2minus' type='button' class='btn btn-primary'>-</button></td>" +
                    "</tr>" +
                    "<tr>" +
                        "<td> <button id='p2Squire' value='true' style=\"background-color:greenyellow;\" onclick='rankSelect4()'>Squire </button> <button id='p2Knight' value='false' style=\"background-color:indianred;\" onclick='rankSelect5()'>Knight </button> <button id='p2CKnight' value='false' style=\"background-color:indianred;\" onclick='rankSelect6()'>C.Knight </button> </td>" +
                        "<td><input id='numShields2' type='textbox' value='0' onfocus=\"this.value=''\"></td>" +
                    "</tr>" +

                    "<tr> " +
                        "<th scope='row'>Player 3</th>" +
                        "<td><input id='p3Name' type='textbox'></td>" +
                        "<td><input id='p3Type' type='textbox'></td>" +
                        "<td><button id='p3Add' type='button' class='btn btn-primary' onclick='addCards(this.id)'>+</button><button id='p3minus' type='button' class='btn btn-primary'>-</button></td>" +

                    "</tr>" +

                    "<tr>" +
                        "<td> <button id='p3Squire' value='true' style=\"background-color:greenyellow;\" onclick='rankSelect7()'>Squire </button> <button id='p3Knight' value='false' style=\"background-color:indianred;\" onclick='rankSelect8()'>Knight </button> <button id='p3CKnight' value='false' style=\"background-color:indianred;\" onclick='rankSelect9()'>C.Knight </button> </td>" +
                        "<td><input id='numShields3' type='textbox' value='0' onfocus=\"this.value=''\"></td>" +
                    "</tr>" +

                    "<tr> " +
                        "<th scope='row'>Player 4</th>" +
                        "<td><input id='p4Name' type='textbox'></td>" +
                        "<td><input id='p4Type' type='textbox'></td>" +
                        "<td><button id='p4Add' type='button' class='btn btn-primary' onclick='addCards(this.id)'>+</button><button id='p4minus' type='button' class='btn btn-primary'>-</button></td>" +

                    "</tr>" +

                    "<tr>" +
                        "<td> <button id='p4Squire' value='true' style=\"background-color:greenyellow;\" onclick='rankSelect10()'>Squire </button> <button id='p4Knight' value='false' style=\"background-color:indianred;\" onclick='rankSelect11()'>Knight </button> <button id='p4CKnight' value='false' style=\"background-color:indianred;\" onclick='rankSelect12()'>C.Knight </button> </td>" +
                        "<td><input id='numShields4' type='textbox' value='0' onfocus=\"this.value=''\"></td>" +
                    "</tr>" +
                "</table>" +
            "</div><button id='start' onclick='setUpGame()'>Start Game<button></div>");
        $('#p1Name').val(message.playera);
        $('#p2Name').val(message.playerb);
        $('#p3Name').val(message.playerc);
        $('#p4Name').val(message.playerd);
        // $("#main-content").append(
        //     "<h1>Started Game As Host</h1>"
        // );
        // $("body").addClass("loading");
    } else {
        $("#main-content").empty();
        $("#main-content").append(
            "<h1>Waiting for host to set up game</h1>"
        );
        $("body").addClass("loading");
    }
}

function addCards(message){
    if(!sentRequestServerRig) {
        currentRigPlayer = message;
        console.log("addCards called");
        var returnObj = JSON.stringify({
            'method': 'addCards',
        });
        ws.send(returnObj);
    } else {
        currentRigPlayer = message;
        addRigCards(allCardsToRig);
    }
}    

function rankSelect1() {
    // console.log("p1 squire value" + $('#p1Squire').val());
    // console.log("p1 knight value" + $('#p1Knight').val());
    // console.log("p1 cknight value" + $('#p1CKnight').val());

    $('#p1Squire').css('background-color','greenyellow');
    $('#p1Squire').val('true');
    $('#p1Knight').css('background-color','indianred');
    $('#p1Knight').val('true');
    $('#p1CKnight').css('background-color','indianred');
    $('#p1CKnight').val('true');

    // console.log("p1 squire value" + $('#p1Squire').val());
    // console.log("p1 knight value" + $('#p1Knight').val());
    // console.log("p1 cknight value" + $('#p1CKnight').val());
}
function rankSelect2() {
    $('#p1Squire').css('background-color','indianred');
    $('#p1Squire').val('false');
    $('#p1Knight').css('background-color','greenyellow');
    $('#p1Knight').val('true');
    $('#p1CKnight').css('background-color','indianred');
    $('#p1CKnight').val('false');
}
function rankSelect3() {
    $('#p1Squire').css('background-color','indianred');
    $('#p1Squire').val('false');
    $('#p1Knight').css('background-color','indianred');
    $('#p1Knight').val('false');
    $('#p1CKnight').css('background-color','greenyellow');
    $('#p1CKnight').val('true');
}
function rankSelect4() {
    $('#p2Squire').css('background-color','greenyellow');
    $('#p2Squire').val('true');
    $('#p2Knight').css('background-color','indianred');
    $('#p2Knight').val('false');
    $('#p2CKnight').css('background-color','indianred');
    $('#p2CKnight').val('false');
}
function rankSelect5() {
    $('#p2Squire').css('background-color','indianred');
    $('#p2Squire').val('false');
    $('#p2Knight').css('background-color','greenyellow');
    $('#p2Knight').val('true');
    $('#p2CKnight').css('background-color','indianred');
    $('#p2CKnight').val('false');
}
function rankSelect6() {
    $('#p2Squire').css('background-color','indianred');
    $('#p2Squire').val('false');
    $('#p2Knight').css('background-color','indianred');
    $('#p2Knight').val('false');
    $('#p2CKnight').css('background-color','greenyellow');
    $('#p2CKnight').val('true');
}
function rankSelect7() {
    $('#p3Squire').css('background-color','greenyellow');
    $('#p3Squire').val('true');
    $('#p3Knight').css('background-color','indianred');
    $('#p3Knight').val('false');
    $('#p3CKnight').css('background-color','indianred');
    $('#p3CKnight').val('false');
}
function rankSelect8() {
    $('#p3Squire').css('background-color','indianred');
    $('#p3Squire').val('false');
    $('#p3Knight').css('background-color','greenyellow');
    $('#p3Knight').val('true');
    $('#p3CKnight').css('background-color','indianred');
    $('#p3CKnight').val('false');
}
function rankSelect9() {
    $('#p3Squire').css('background-color','indianred');
    $('#p3Squire').val('false');
    $('#p3Knight').css('background-color','indianred');
    $('#p3Knight').val('false');
    $('#p3CKnight').css('background-color','greenyellow');
    $('#p3CKnight').val('true');
}
function rankSelect10() {
    $('#p4Squire').css('background-color','greenyellow');
    $('#p4Squire').val('true');
    $('#p4Knight').css('background-color','indianred');
    $('#p4Knight').val('false');
    $('#p4CKnight').css('background-color','indianred');
    $('#p4CKnight').val('false');
}
function rankSelect11() {
    $('#p4Squire').css('background-color','indianred');
    $('#p4Squire').val('false');
    $('#p4Knight').css('background-color','greenyellow');
    $('#p4Knight').val('true');
    $('#p4CKnight').css('background-color','indianred');
    $('#p4CKnight').val('false');
}
function rankSelect12() {
    $('#p4Squire').css('background-color','indianred');
    $('#p4Squire').val('false');
    $('#p4Knight').css('background-color','indianred');
    $('#p4Knight').val('false');
    $('#p4CKnight').css('background-color','greenyellow');
    $('#p4CKnight').val('true');
}

function setUpGame(){
    //check text is an int TODO
    console.log("setUpGame called");
    var returnObj = JSON.stringify({
        'method':'hostSetUpGame',
        'p1Shields':$("#numShields").val(),
        'p2Shields':$("#numShields2").val(),
        'p3Shields':$("#numShields3").val(),
        'p4Shields':$("#numShields4").val(),
        'p1Squire':$("#p1Squire").val(),
        'p2Squire':$("#p2Squire").val(),
        'p3Squire':$("#p3Squire").val(),
        'p4Squire':$("#p4Squire").val(),
        'p1Knight':$("#p1Knight").val(),
        'p2Knight':$("#p2Knight").val(),
        'p3Knight':$("#p3Knight").val(),
        'p4Knight':$("#p4Knight").val(),
        'p1CKnight':$("#p1CKnight").val(),
        'p2CKnight':$("#p2CKnight").val(),
        'p3CKnight':$("#p3CKnight").val(),
        'p4CKnight':$("#p4CKnight").val(),
        'p1Cards':JSON.stringify(playerCardsRigged.playerone),
        'p2Cards':JSON.stringify(playerCardsRigged.playertwo),
        'p3Cards':JSON.stringify(playerCardsRigged.playerthree),
        'p4Cards':JSON.stringify(playerCardsRigged.playerfour)
    });
    console.log("(SETUP GAME)returnObj is "+returnObj);
    ws.send(returnObj);
}

function addRigCards(message){
    $("#main-content").hide();
    // console.log("message received from server " + message);
    // console.log("message size is "+ message.length);
    // console.log("parsing individual array element in javascript");
    cardSelectedRigGame = [];

    $('.imageToSelect').empty();
    $('body').append("<div class='imageToSelect'>");
    console.log("DIV for images created");
    $('.imageToSelect').empty();

    $(".imageToSelect").append("<tr>");
    // console.log("messages size is =  "+message.length);
    // //console.log("before for loop "+$("img").length)s;
    // $(".container").append("<img>s ")
    // console.log("before for loop "+$("img").length);
    for(var i=0; i<(message.length); i++){
        if(i%8==0){
            $(".imageToSelect").append("</tr>");
            $(".imageToSelect").append("<tr>");
        }
        if(typeof message[i]  === "undefined"){
            continue;
        }
        var cardName = "/images/" + message[i].name +".png";
        var cardNameForId = message[i].name + i;
        if(message[i].name.includes(" ")){
            cardName = cardName.replace(/\s/g, '_');
        } else if(message[i].name === "Battle-ax"){
            console.log("battle ax reached");
            cardName = "/images/battle_ax.png";
        }
        // console.log("middle"+$("img").length);
        $(".imageToSelect").append("<td>" + "<img name='"+i+"' id='"+(message[i].name+i)+"' class='cardImg' height='190' width='133' alt='Card image cap' src='"+cardName+"'>" + "</td>");
        // $(".container").append("<img id='"+(message[i].name+i)+"' class='gayboi' height='190' width='133' alt='Card image cap' src='"+cardName+"'>");
        //$(".container").append("")
        // console.log("The index is = " + i);
        // console.log("during for loop" +$("img").length);
    }
    // console.log("after for loop is done" + $("img").length);
    $(".imageToSelect").append("</tr>");
    var images = [].slice.call(document.getElementsByClassName('cardImg'));
    // console.log("images is "+images);
    // console.log(images.length);
    images.forEach(function (element, imgIndex){
        console.log(imgIndex);
        element.addEventListener("click", function(){
            //console.log("you clicked this image "+imgIndex);
            console.log("id of this card is :" +this.id);

            var index = cardSelectedRigGame.indexOf(this.id);
            if (index > -1){
                console.log("already in array");
            } else {
                if(clicked == 12){
                    window.alert("MAX CARDS SELECTED. You can only select 12 cards.......");
                }
                else{
                    console.log("Apparently this is the imgIndex " + imgIndex);
                    cardsToRemoveAfterUsed.push(this.name);
                    indexToBeRemoved.push(imgIndex);
                    $(this).css('border', "solid 2px red");
                    cardSelectedRigGame.push(this.id);
                    clicked++;
                }

            }
            // console.log("test "+cardsToRemoveAfterUsed[0]);
            // console.log("size of cardSelectedRigGame is : "+cardSelectedRigGame.length);
            // console.log("cards to remove = " + cardsToRemoveAfterUsed);
            // console.log("IndexOfThe cards that have been selected "+indexToBeRemoved);
            //console.log("index of card to be removed at 12: " + cardsToRemoveAfterUsed[])
        })
        element.addEventListener("dblclick", function(){
            // console.log("unclicked image" +imgIndex);
            $(this).css('border', "0px");
            var indexOfTheCardUnClicked = indexToBeRemoved.indexOf(imgIndex);
            // console.log("index to be removed variable " + indexToBeRemoved);
            indexToBeRemoved.splice(indexOfTheCardUnClicked, 1);
            // console.log("UPDATED: cards to be removed = " + indexToBeRemoved);
            // console.log(indexToBeRemoved[0]);
            clicked--;
            var index = cardSelectedRigGame.indexOf(this.id);
            // console.log(this.id)
            if( index > -1){
                cardSelectedRigGame.splice(index, 1);
            }
        })
    });
    $(".imageToSelect").append(
        "<button style='position: absolute; left: 1010px; top: 0px; height: 1080px; width: 150px' type='button' class='btn btn-success' onclick='addRiggedCards()'>Done</button>"
    );
}

function displayCards(message, method) {
    // var CardsInHand = [];
    // CardsInHand = message;
    //console.log("Display cards function: ")
    $("#cards").remove();
    $("#main-content").append("<div id='cards' class='row'>");
    //console.log("Displaying individual cards: ");
    // for(var i=0; i<message.length; i++){
    //     console.log("   message in for loop is "+message[i]);
    //     console.log("   Card name is : "+message[i].name);
    //     console.log("   Battlepoints is "+message[i].battlepoints);
    // }
    console.log("size of cards is "+message.length);
    for (var i = 0; i < (message.length); i++) {
        var cardName = "/images/" + message[i].name + ".png";
        var cardNameForId = message[i].name + i;
        if (message[i].name.includes(" ")) {
            cardName = cardName.replace(/\s/g, '_');
        } else if (message[i].name === "Battle-ax") {
            cardName = "/images/battle_ax.png";
        }
        //$("#cards").append("<td>" + "<img name='"+i+"' id='"+(message[i].name+i)+"' class='cardImg' height='190' width='133' alt='Card image cap' src='"+cardName+"'>" + "</td>");
        $("#cards").append("<td>" + "<img name='" + i + "' id='" + i + "' class='cardImg' height='190' width='133' alt='Card image cap' src='" + cardName + "'>" + "</td>");
    }
    var images = [].slice.call(document.getElementsByClassName('cardImg'));
    images.forEach(function (element, imgIndex) {
        //console.log("Index of image clicked "+imgIndex);
        element.addEventListener("click", function () {
            //var index = cardsSelectedToBePlayed.indexOf(imgIndex);
            var index = -1; //$.inArray(imgIndex, cardsSelectedToBePlayed, 0);
            var savedI = -1;
            for (var i = 0; i < cardsSelectedToBePlayed.length; i++) {
                if (cardsSelectedToBePlayed[i] == this.name) {
                    //console.log("We have a match!" + cardsSelectedToBePlayed[i] + " | " + this.name);
                    index = this.name; //pretty redundant using index to check instead of savedI but whatever
                    savedI = i;
                }
            }
            //console.log("id we are looking for " + this.id + "and the value of index is " + index + "|" + imgIndex);
            //console.log("this.name is " + this.name);
            if (index > -1) {
                //console.log("already in array");
                cardsSelectedToBePlayed.splice(savedI, 1);
                //cardsSelectedToBePlayed.delete(index);
                $(this).css({"position":"relative", "top":"60px"});
            }

            else {
                cardsSelectedToBePlayed.push(parseInt(this.name));
                //console.log("CardsSelectedTobePlayed (inside else) = " + cardsSelectedToBePlayed);
                $(this).css({"position":"relative", "top":"30px"});
            }
            // console.log('Card Selected' + this.name);
            // console.log("CardsSelectedTobePlayed = " + cardsSelectedToBePlayed);

        });
    });
    //$("#main-content").append("<button style='position: absolute; left: 50px; top: 50px; height: 100px; width:30px' type='button' id='sendCards' class='btn btn-success' onclick='sendSelectedCards()'>Done</button>");
    $("#main-content").append("<button type='button' id='sendCards' class='btn btn-success' onclick='sendSelectedCards()'>Done</button>");
    $("#main-content").append("</div>");

    if (method === "startGame") {
        startTimer(5, "readyUpAlert");
    } else if (method === "tournament") {
        startTimer(10, "pickedCardsTournament");
    } else if(method == "updateCards"){
        console.log("nothing gonna happen");
    }
    //console.log("cf here");
}

function addRiggedCards(){
        if (currentRigPlayer === "p1Add"){
            // console.log("Player 1 list of cards " + cardSelectedRigGame);
            playerCardsRigged.playerone = [];
            playerCardsRigged.playerone = cardsToRemoveAfterUsed;
            // console.log(playerCardsRigged.playerone);
            $("#main-content").show();
            $(".imageToSelect").empty();
            // console.log("lenght of div after clearing it and before deleting" + $(".imageToSelect").length)
            $(".imageToSelect").remove();
            // console.log("lenght of div after clearing it" + $(".imageToSelect").length);

            for(var i=0; i<cardsToRemoveAfterUsed.length; i++) {
                //var indexToRemove = cardsToRemoveAfterUsed[i];
                for (var j = 0; j < allCardsToRig.length; j++) {
                    // console.log("cardsToRemoveAfterUsed" + cardsToRemoveAfterUsed[i]);
                    // console.log(" j " + j);
                    if (typeof allCardsToRig[j] === "undefined") {
                        continue;
                    }
                    if (cardsToRemoveAfterUsed[i] == j) {
                        console.log("index matched = cardSelected[i] " + cardsToRemoveAfterUsed[i] + " + j " + j);
                        delete  allCardsToRig[j];

                    }
                }

            }
            cardSelectedRigGame = [];
            cardsToRemoveAfterUsed = [];
            indexToBeRemoved = [];
            clicked = 0;
        }
        else if (currentRigPlayer === "p2Add"){
            // console.log("Player 2 list of cards " + cardSelectedRigGame);
            playerCardsRigged.playertwo = [];
            playerCardsRigged.playertwo = cardsToRemoveAfterUsed;
            // console.log(playerCardsRigged.playertwo);
            $("#main-content").show();
            $(".imageToSelect").empty();
            // console.log("lenght of div after clearing it and before deleting" + $(".imageToSelect").length)
            $(".imageToSelect").remove();
            // console.log("lenght of div after clearing it" + $(".imageToSelect").length);

            for(var i=0; i<cardsToRemoveAfterUsed.length; i++) {
                //var indexToRemove = cardsToRemoveAfterUsed[i];
                for (var j = 0; j < allCardsToRig.length; j++) {
                    // console.log("indexToRemove" + cardsToRemoveAfterUsed[i]);
                    // console.log(" j " + j);
                    //console.log("indexToRemove" + indexToRemove);
                    if (typeof allCardsToRig[j] === "undefined") {
                        continue;
                    }
                    if (cardsToRemoveAfterUsed[i] == j) {
                        // console.log("index matched = cardSelected[i] " + cardsToRemoveAfterUsed[i] + " + j " + j);
                        delete  allCardsToRig[j];

                    }
                }

            }
            cardSelectedRigGame = [];
            cardsToRemoveAfterUsed = [];
            indexToBeRemoved = [];
            clicked = 0;
        }
        else if (currentRigPlayer === "p3Add"){
            // console.log("Player 3 list of cards " + cardSelectedRigGame);
            playerCardsRigged.playerthree = [];
            playerCardsRigged.playerthree = cardsToRemoveAfterUsed;
            // console.log(playerCardsRigged.playerthree);
            $("#main-content").show();
            $(".imageToSelect").empty();
            // console.log("length of div after clearing it and before deleting" + $(".imageToSelect").length)
            $(".imageToSelect").remove();
            // console.log("lenght of div after clearing it" + $(".imageToSelect").length);

            for(var i=0; i<cardsToRemoveAfterUsed.length; i++) {
                //var indexToRemove = cardsToRemoveAfterUsed[i];
                for (var j = 0; j < allCardsToRig.length; j++) {
                    // console.log("cardsToRemoveAfterUsed" + cardsToRemoveAfterUsed[i]);
                    // console.log(" j " + j);
                    //console.log("indexToRemove" + indexToRemove);
                    if (typeof allCardsToRig[j] === "undefined") {
                        continue;
                    }
                    if (cardsToRemoveAfterUsed[i] == j) {
                        // console.log("index matched = cardSelected[i] " + cardsToRemoveAfterUsed[i] + " + j " + j);
                        delete  allCardsToRig[j];

                    }
                }

            }
            cardSelectedRigGame = [];
            cardsToRemoveAfterUsed = [];
            indexToBeRemoved = [];
            clicked = 0;
        }
        else if (currentRigPlayer === "p4Add"){
            // console.log("Player 4 list of cards " + cardSelectedRigGame);
            playerCardsRigged.playerfour = [];
            playerCardsRigged.playerfour = cardsToRemoveAfterUsed;
            // console.log(playerCardsRigged.playerfour);
            $("#main-content").show();
            $(".imageToSelect").empty();
            // console.log("lenght of div after clearing it and before deleting" + $(".imageToSelect").length)
            $(".imageToSelect").remove();
            // console.log("lenght of div after clearing it" + $(".imageToSelect").length);

            for(var i=0; i<cardsToRemoveAfterUsed.length; i++) {
                //var indexToRemove = cardsToRemoveAfterUsed[i];
                // console.log("cardsToRemoveAfterUsed" + cardsToRemoveAfterUsed[i]);
                // console.log(" j " + j);
                for (var j = 0; j < allCardsToRig.length; j++) {
                    //console.log("indexToRemove" + indexToRemove);
                    if (typeof allCardsToRig[j] === "undefined") {
                        continue;
                    }
                    if (cardsToRemoveAfterUsed[i] == j) {
                        // console.log("index matched = cardSelected[i] " + cardsToRemoveAfterUsed[i] + " + j " + j);
                        delete  allCardsToRig[j];

                    }
                }

            }
            cardSelectedRigGame = [];
            cardsToRemoveAfterUsed = [];
            indexToBeRemoved = [];
            clicked = 0;
        }
        else{
             // console.log("Nothing happend");
        }
    // console.log("\n__________-------> DONE <-------____________\n");

}

function startedTheRealGame(message, playerinfo){
    console.log("----------------------------------------->");
    console.log("playerInfo is "+playerinfo);
    console.log("started the real game "+ message);
    $("body").removeClass("loading");
    $("#main-content").empty();
    $("#main-content").css({"background-color": "Transparent", "border-width": "0px"});
    console.log("main-content size is : ",$("#main-content").size());
    $("#main-content").append(
        "<div id='scoreBoard' style='position: absolute; top: 0%; left: 80%;'>" +
            "<table class='table'>" +
                "<thead>" +
                    "<th scope='col'>Players</th>" +
                    "<th scope='col'># C</th>" +
                    "<th scope='col'># S</th>" +
                    "<th scope='col'>Rank</th>" +
                "</thead>" +
            "</table>" +
        "</div>");
    for(var i=0; i<playerinfo.length; i++){
        console.log("Player info "+playerinfo[i]);
    }
    displayCards(message, "startGame");
}

function startTimer(duration, method) {
    cardsSelectedToBePlayed = [];
    $("body").prepend("<div id='timer' class='alert alert-danger' role='alert'></div>");
    //console.log("in startTimer")
    if(method != "pickCardsQuest") {
        var $timerDiv = $(".popup");
        if ($timerDiv.is("html *")) {
            $timerDiv.remove();
        }
    }
    var timer = duration, minutes, seconds;
    myTimer = setInterval(function () {
        minutes = parseInt(timer / 60, 10)
        seconds = parseInt(timer % 60, 10);

        var messageToDisplay;
        if(method === "readyUpAlert"){
            messageToDisplay = "Ready up, game starts in";
        } else if(method === "pickedCardsTournament"){
            messageToDisplay = "Pick cards for tournament";
        }

        minutes = minutes < 10 ? "0" + minutes : minutes;
        seconds = seconds < 10 ? "0" + seconds : seconds;

        //display.textContent = minutes + ":" + seconds;
        $("#timer").empty();
        $("#timer").append(messageToDisplay + " " + minutes + ":" + seconds);
        if (--timer < 0 && timer != 0) {
            timer = duration;
        } else if(timer == 0){
            clearInterval(myTimer);
            $("#timer").remove();
            if(method === "readyUpAlert"){
                var returnObj = JSON.stringify({
                    'method':'playerReady'
                });
                ws.send(returnObj);
            }else if(method === "pickedCardsTournament"){
                console.log("pickedCardsTournament in startTimer");
                if (!selectedCards) {
                    var returnObj = JSON.stringify({
                        'method': 'selectedCards',
                        'body': JSON.stringify(cardsSelectedToBePlayed)
                    });
                    $("#sendCards").prop('disabled', true);
                    ws.send(returnObj);
                }
            }else if(method === "pickCardsQuest"){
                if (!selectedCards){
                    console.log("inside pick cards quest")
                    var returnObj = JSON.stringify({
                        'method': 'selectedCardsForQuest',
                        'body' : JSON.stringify(cardsSelectedToBePlayed),
                        'currentStage':serverCurrentStage.toString()
                    });
                    $("#sendCards").prop('disabled', true);
                    ws.send(returnObj);
                }
                console.log("!selectedCards failed in method === pickCardsQuest")
            }//quest logic for the timer
        }
    }, 1000);
    // console.log("control flow here");
}


function sendSelectedCards() {
    selectedCards = true; //when do we reset this
    clearInterval(myTimer);
    var $timerDiv = $("#timer");
    if($timerDiv.is("html *")){
        $timerDiv.remove();
    }
    console.log("sendSelectedCards called "+JSON.stringify(cardsSelectedToBePlayed));
    var returnObj = JSON.stringify({
        'method':'selectedCards',
        'body':JSON.stringify(cardsSelectedToBePlayed)
    });
    //returnObj.body = playerCardsRigged;
    //console.log("(SEND SELECTED CARDS returnObj is "+returnObj);
    $("#sendCards").prop('disabled',true);
    ws.send(returnObj);
    selectedCards = false;
    $("#alertForWaiting").remove();
    $("#main-content").append(
        "<div id='alertForWaiting' class='panel panel-primary'><div  class='panel-heading'>Alert:</div><div class='panel-body'>Waiting for other players<span id='dot'></span></div></div>"
    );
    // var dots = 0;
    // if(dots < 3){
    //     $("#dot").append('.');
    //     dots++;
    // } else {
    //     $("#dot").html('');
    //     dots = 0;
    // }
}

function startTournament(message){
    currentStoryCard = "tournament";
    $("#tournamentCard").remove();
    $("#questCard").remove();
    console.log("message is : ", message.body);
    clearInterval(myTimer);
    var imageName = ((message.body).toString()).toLowerCase();
    imageName = imageName.replace(/\s+/g, '');
    imageName = "/images/"+"tournament_"+imageName+".png";
    $("#main-content").append("<td>" + "<img id='tournamentCard' style='position: relative; left: -180px; top: 200px;' class='cardImg' height='190' width='133' alt='Card image cap' src='"+imageName+"'>" + "</td>");
    confirmUser("Would you like to participate in the Tournament "+ message.body + " ?", "tournament");

}

function showEvent(message){
    $("#tournamentCard").remove();
    $("#questCard").remove();
    $("#eventCard").remove();
    clearInterval(myTimer);
    var imageName = (message.body).toString();
    imageName = imageName.replace(/ /g,"_");
    imageName = "/images/"+imageName+".png";
    $("#main-content").append("<td>" + "<img id='eventCard' style='position: relative; left: -180px; top: 200px;' class='cardImg' height='190' width='133' alt='Card image cap' src='"+imageName+"'>" + "</td>");
}

function startQuest(message){
    currentStoryCard = "quest";
    $("#tournamentCard").remove();
    $("#questCard").remove();
    console.log("message is : ", message.body);
    clearInterval(myTimer);
    var imageName = (message.body).toString();
    imageName = imageName.replace(/ /g,"_");
    console.log("image name " + imageName);
    totalStages = message.numStages;
    imageName = "/images/"+imageName+".png";
    $("#main-content").append("<td>" + "<img id='questCard' style='position: relative; left: -180px; top: 200px;' class='cardImg' height='190' width='133' alt='Card image cap' src='"+imageName+"'>" + "</td>");
    confirmUser("Would you like to sponsor the following quest: " + message.body + " ?", "quest");
    // if(message.isHost === "yes") {
    //     confirmUser("Would you like to sponsor the following quest: " + message.body + " ?", "quest");
    //     hostForRound = true;
    // } else {
    //     hostForRound = false;
    // }

}

function askPlayerToParticipateInQuest(cardName, host){
    $("#questCard").remove();
    var imageName = cardName;
    imageName = imageName.replace(/ /g,"_");
    imageName = "/images/"+imageName+".png";
    $("#main-content").append("<td>" + "<img id='questCard' style='position: relative; left: -180px; top: 200px;' class='cardImg' height='190' width='133' alt='Card image cap' src='"+imageName+"'>" + "</td>");
    $("#main-content").append(
        "<div style='position:relative' class='popup'>" +
        "<div class='upper'>Would you like to participate in the " +cardName+ "? \n Being hosted by " +host+ "</div>" +
        "<div class='stroke'></div>" +
        "<div class='lower'>" + "<button id='yesToParticipateInQuest' onclick='participatingInQuest()' class='confirmBox'><i class='icon-large icon-ok'></i>Yes</button>" +
        "<button onclick='notParticipatingInQuest()' class='confirmBox'><i class='icon-large icon-remove'></i>No</button>" +
        "</div>" +
        "</div>");
}

function participatingInQuest(){
    $(".popup").remove();
    var response = JSON.stringify({'method':'playerParticipatingInQuest',
        'body':'yes',
        'name': name});
    ws.send(response);
}

function notParticipatingInQuest(){
    $(".popup").remove();
    var response = JSON.stringify({'method':'playerParticipatingInQuest',
        'body':'no',
        'name':name});
    ws.send(response);
}

function confirmUser(message, storyCard) {
    console.log("confirm user :- message is ====  " + message);
    console.log("confirm user :- storycard is ====  " + storyCard);
    console.log("Confirm user has been created");
    $("#main-content").append(
        "<div style='position:relative' class='popup'>" +
        "<div class='upper'>" + message + "</div>" +
        "<div class='stroke'></div>" +
        // "<div class='lower'>" + "<button onclick='storyCardTimer()' class='confirmBox'><i class='icon-large icon-ok'></i>Yes</button>" +
        "<div class='lower'>" + "<button id='cBox' class='confirmBox'><i class='icon-large icon-ok'></i>Yes</button>" +
        "<button id='noButtonConfirmUser' onclick='removeConfirmUser()' class='confirmBox'>No</button>" +
        "</div>" +
        "</div>");
    console.log("storyCard is: " + storyCard);
    $("#cBox").click(function () {
        storyCardTimer(storyCard);
    });
    if(storyCard === "selectQuestCards"){
        $("#noButtonConfirmUser").remove();
        $("#cBox").text("Ok");
        startTimer(120, "pickCardsQuest");
    }
}

function removeConfirmUser(){
    $(".popup").remove();
    var responseObj = JSON.stringify({
        'method':'selectedCards',
        'body': 'no'
    });
    if(currentStoryCard == "quest"){
        var responseObj = JSON.stringify({
            'method':'hostForQuest',
            'body':'no'
        });
    }
    ws.send(responseObj);
    console.log("message sent back to server "+responseObj);
}

function storyCardTimer(storyCard){
    if(storyCard === "tournament"){
        startTimer(30,"pickedCardsTournament");
    }
    else if(storyCard === "quest"){
        //startTimer(30,"pickedCardsQuest");
        console.log("Quest has draw story card");
        $(".popup").remove();
        // if(hostForRound){
            var responseObj = JSON.stringify({
               'method': 'hostForQuest',
                'body':'yes'
            });
            ws.send(responseObj);
        // }
        console.log("add quest timer here but rn its working");
        //hostForRound =
        ///next story card will be called here
    }
    else if(storyCard == "selectQuestCards"){
        console.log("inside selectQuestCards ");
        selectedCards = true;
        var responseObj = JSON.stringify({
            'method' : 'selectedCardsForQuest',
            'body': JSON.stringify(cardsSelectedToBePlayed),
            'currentStage': serverCurrentStage.toString()
        });
        ws.send(responseObj);
        clearInterval(myTimer);
        $(".popup").remove();
    }
    else {
        console.log("Critical Error 9001");
    }
}

function startTournamentTie(message){
    cardsSelectedToBePlayed = [];
    if(message.stillInTournament === "yes"){
        displayCards(message.body, "tournament");
    } else {
        //confirmUser()
        console.log(message)
    }
}

function setUpQuest(message) {
    console.log("this message  (inside setup quest) "+ message);
    console.log("this message.body  (inside setup quest) "+ message.body);
    console.log("this message.canSponsor  (inside setup quest) "+ message.canSponsor);
    if($(".popup").length){
        $(".popup").remove();
    }
    $("#main-content").append(
        "<div style='position:relative' class='popup'>" +
        "<div class='upper'>Select the cards you'd like for stage 1</div>" +
        "<div class='stroke'></div>" +
        "<div class='lower'>" + "<button id='addQuestCards' class='confirmBox'><i class='icon-large icon-ok'></i>Yes</button>" +
        "</div>" +
        "</div>");
    $("#addQuestCards").click(function () {
        console.log("Selected cards is "+cardsSelectedToBePlayed);
        console.log("currentNumStage is "+currentNumStages);
        if(currentNumStages <= totalStages) {
            console.log("current stage right now " + currentNumStages);
            if (currentNumStages == 1) {
                console.log("currentNumStage should be 1 "+currentNumStages);
                //$(".upper").empty();
                //$(".upper").text("Select the cards you'd like for stage1 " + currentNumStages);
                console.log("in 1");
                cardsSelectedQuest.stageOne = cardsSelectedToBePlayed;
                currentNumStages++;
                $(".upper").empty();
                $(".upper").text("Select the cards you'd like for stage 2 " + currentNumStages);
            } else if (currentNumStages == 2) {

                console.log("in 2");
                currentNumStages++;
                $(".upper").empty();
                cardsSelectedQuest.stageTwo = cardsSelectedToBePlayed;
                if(currentNumStages > totalStages){
                    sendQuestCards();
                    return;
                }
                $(".upper").text("Select the cards you'd like for stage3 " + currentNumStages);
            } else if (currentNumStages == 3) {
                console.log("in 3");
                currentNumStages++;
                $(".upper").empty();
                cardsSelectedQuest.stageThree = cardsSelectedToBePlayed;
                if(currentNumStages > totalStages){
                    sendQuestCards();
                    return;
                }
                $(".upper").text("Select the cards you'd like for stage4 " + currentNumStages);
                //currentNumStages++;
            } else if (currentNumStages == 4) {
                console.log("in 4");
                currentNumStages++;
                $(".upper").empty();
                cardsSelectedQuest.stageFour = cardsSelectedToBePlayed;
                if(currentNumStages > totalStages){
                    sendQuestCards();
                    return;
                }
                $(".upper").text("Select the cards you'd like for stage5 " + currentNumStages);
                //currentNumStages++;
            } else if (currentNumStages == 5) {
                console.log("in 5");
                currentNumStages++;
                $(".upper").empty();
                cardsSelectedQuest.stageFive = cardsSelectedToBePlayed;
                if(currentNumStages > totalStages){
                    sendQuestCards();
                    return;
                }
                $(".upper").text("Should never get here ERR 9001 " + currentNumStages);
                //currentNumStages++;
            }
        }else {
            console.log("Should we be ever getting to the else? Possible error 9001")
            $(".popup").remove();
            var responseObj = JSON.stringify({
                'method':'cardsForQuest',
                'stage1':JSON.stringify(cardsSelectedQuest.stageOne),
                'stage2':JSON.stringify(cardsSelectedQuest.stageTwo),
                'stage3':JSON.stringify(cardsSelectedQuest.stageThree),
                'stage4':JSON.stringify(cardsSelectedQuest.stageFour),
                'stage5':JSON.stringify(cardsSelectedQuest.stageFive)
            });
            ws.send(responseObj);
            //don't have check to see if stage two, three, four, or five has been initialized
        }
        console.log("cards selected for quest is "+cardsSelectedQuest);
        cardsSelectedToBePlayed = [];

    });

    // if(message.body === "no"){
    //     $("#main-content").append(
    //         "<div style='position:relative' class='popup'>" +
    //         "<div class='upper'>You are not able to host this quest.</div>" +
    //         "<div class='stroke'></div>" +
    //         "</div>" +
    //         "</div>");
    // }


}

function sendQuestCards(){
    console.log("In sendQuestCards");
    $(".popup").remove();
    var responseObj = JSON.stringify({
        'method':'cardsForQuest',
        'stage1':JSON.stringify(cardsSelectedQuest.stageOne),
        'stage2':JSON.stringify(cardsSelectedQuest.stageTwo),
        'stage3':JSON.stringify(cardsSelectedQuest.stageThree),
        'stage4':JSON.stringify(cardsSelectedQuest.stageFour),
        'stage5':JSON.stringify(cardsSelectedQuest.stageFive)
    });
    console.log("responseObj being sent back to server from sendQuestCards is "+responseObj);
    ws.send(responseObj);

}


function sponsorFound(message){
    var msg;
    if(message.sponsorFound == "yes"){
        msg = message.body + " is now setting up the stages";
        $(".popup").remove();
        $("#main-content").append(
            "<div style='position:relative' class='popup'>" +
            "<div class='upper'>" + msg + "</div>" +
            "<div class='stroke'></div>" +
            "</div>" +
            "</div>");
    } else {
        hostForRound = true;
        msg = message.body + " has declined sponsoring the quest";
        $("#main-content").append(
            "<div style='position:relative' class='popup'>" +
            "<div class='upper'>" + msg + "</div>" +
            "<div class='stroke'></div>" +
            "</div>" +
            "</div>");
        questSponsorTimer = setTimeout(
          function(){
              $(".popup").remove();
              confirmUser("Would you like to sponsor this quest", "quest");
          }, 4000);
    }

}

function setUpStages(message) {

}

function playStage(message){
    console.log("currentstory card boolean for test is "+message.isTest);
    $(".popup").remove();
    $("#timer").remove();
    serverCurrentStage = message.currentStage;
    console.log("message is : " + message.currentStage + " totalstages " + message.totalStages);
    clearInterval(myTimer);
    $("#main-content").append("<div id = stageCards></div>");
    if(message.currentStage ==1) {
        for (var i = 0; i < message.totalStages; i++) {
            //$("#stageCards").append("<td>" + "<img id='backOfCard' style='position: relative; left: -50px; top: 10px;' class='cardImg' height='190' width='133' alt='Card image cap' src='/images/back.png'>" + "</td>")
            $("#stageCards").append("<td>" + "<img id='backOfCard' class='cardImg' alt='Card image cap' src='/images/back.png'>" + "</td>");
        }
    }
    var stringToSend = "Please Select the cards you would like to play for stage " + message.currentStage + " ";
    confirmUser(stringToSend, "selectQuestCards");
}

function updateCardsForQuest(message) {
    console.log("This is message in update cards ");
    displayCards(message.body, "quest");
}

// function playerCurrentStatusInTheQuest(message) {
//     $(".popup").remove();
//     if(message.body){
//         //TODO you have been eliminated using notification
//         window.alert("You suck, you have been eliminated");
//     }
//     else{
//         //TODO not eliminated
//     }
//     console.log(message);
//     console.log(message.body);
// }

function bidder(message){
    console.log("current stage is "+message.currentStage);
    cardsSelectedToBePlayed = [];
    //if(mess)
    var lastBidderForced = false;
    //TODO - add functionality for incrementBid and decrementBid soon
    console.log("inside bidder");
    $(".popup").remove();
    $("#main-content").append(
        "<div style='position:relative' class='popup'>" +
        "<div class='upper'><p id='textToChange'>Enter the number of cards you would like to enter in bid:</p>" +
        //"<button id='Increment' onclick='incrementBid()' class='confirmBox'>+</button>" + -->
        "<input id='numberIncrementer' type='number' name='bids' min='"+message.minBids+"' max='"+message.maxBids+"'>" +
        //"<button id='decrement' onclick='decrementBid()' class='confirmBox'>-</button>" +
        "<div class='stroke'></div>" +
        // "<div class='lower'>" + "<button onclick='storyCardTimer()' class='confirmBox'><i class='icon-large icon-ok'></i>Yes</button>" +
        "<div class='lower'>" +
        "<button id='confirmResponse' class='confirmBox'>Ok</button></br>" +
        "<button id='forfeitBidding' class='confirmBox'>Leave Bidding</button>" +
        "</div>" +
        "</div>");

    if(message.body == "lastBidder"){
        console.log("message contains a body ");
        $("#textToChange").text("You are the last player, would you like to lose min cards to move on with quest?");
    }else if(message.body == "lastBidderForced"){
        lastBidderForced = true;
        $("#textToChange").text("You are the last player, select "+ message.minBids+ " cards");
        $("#forfeitBidding").remove();
    }
    //$("#numberIncrementer").val(message.minBids);
    var defaultVariable = -1;
    $("#forfeitBidding").click(function(){
        var responseObj = JSON.stringify({
            'method':'biddingRequest',
            'participating':'no',
            'body':defaultVariable.toString(),
            'currentStage':message.currentStage.toString()
        });
        ws.send(responseObj);
        $(".popup").empty();
    });


    //TODO prompt user when closing browsers, fake a session from the client side

    $("#confirmResponse").click(function() {
        if(!lastBidderForced) {
            var valueInt = parseInt($("#numberIncrementer").val());
            if (isNaN(valueInt)) {
                valueInt = message.minBids;
            }
            console.log("entered value is " + valueInt);
            var responseObj = JSON.stringify({
                'method': 'biddingRequest',
                'participating': 'yes',
                'body': valueInt.toString(),
                'currentStage':message.currentStage.toString()
            });
        }else {
            var responseObj = JSON.stringify({
                'method': 'biddingRemoveCards',
                'body': JSON.stringify(cardsSelectedToBePlayed),
                'currentStage': message.currentStage.toString()
            });
        }
        ws.send(responseObj);
        $(".popup").empty();
    });

}

function showToast(type, msg) {
    toastr[type](msg);
    toastr.options = {
        "closeButton": false,
        "debug": false,
        "newestOnTop": true,
        "progressBar": false,
        "positionClass": "toast-top-right",
        "preventDuplicates": false,
        "onclick": null,
        "showDuration": "300",
        "hideDuration": "1000",
        "timeOut": "5000",
        "extendedTimeOut": "1000",
        "showEasing": "swing",
        "hideEasing": "linear",
        "showMethod": "fadeIn",
        "hideMethod": "fadeOut"
    }
}

var globalTestTemp;
function alliesInPlay(message){
    globalTestTemp = message;
    $("#allies").remove();
    $("#main-content").append("<div style='background-color: khaki; position: absolute; left: -20px; top: 150px;' id='allies' class='row'>");
    console.log("<------------------------------------------------------>");
    console.log("allies in play" + message);
    console.log("allies in play" + message[0]);
    for(var i =0; i < message.length; i++) {
        console.log("allies in play" + message[i]);
        var temp = message[i];
        for(var j=0; j<message[i].length; j++){
            console.log(temp[j].name);
        }
    }
    console.log("<------------------------------------------------------>");
    // for (var i = 0; i < (message.length); i++) {
    //     var cardName = "/images/" + message[i].name + ".png";
    //     var cardName = message[i].name;
    //     if (message[i].name.includes(" ")) {
    //         cardName = cardName.replace(/\s/g, '_');
    //     }
    //     //$("#cards").append("<td>" + "<img name='"+i+"' id='"+(message[i].name+i)+"' class='cardImg' height='190' width='133' alt='Card image cap' src='"+cardName+"'>" + "</td>");
    //     $("#cards").append("<td>" + "<img name='" + i + "' id='" + i + "' class='cardImg' height='190' width='133' alt='Card image cap' src='" + cardName + "'>" + "</td>");
    // }
}