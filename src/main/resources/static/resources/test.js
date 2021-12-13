var socket = new SockJS('/ws/message');
var stompClient = null;

function connect() {
    stompClient = Stomp.over(socket);
    stompClient.connect({ username: 'tester' }, onConnected, onError);
}

connect();

function onConnected() {
    stompClient.subscribe('/personal/msg/test', onMessageReceived);
    alert("web socket connected")
    stompClient.send("/app/test",
        {},
        "test massge");
}


function onError(error) {

}


function sendMessage() {
    stompClient.send("/app/send",
        {},
        "test massge");
}


function onMessageReceived(payload) {

}


