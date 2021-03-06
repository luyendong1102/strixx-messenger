'use strict';

var messageForm = document.querySelector('#messageForm');
var messageArea = document.querySelector('#messageArea');
var messageContent = document.querySelector('#message');
var connectingElement = document.querySelector('#connecting');
var stompClient = null;
var socket = new SockJS('/ws/message');
var key = userid;
key = key.replaceAll("-", "");
key = key + global;
key = key.substring(0, 32);
var hashKey = CryptoJS.MD5(userid).toString();
var fileInput = document.getElementById('img_form');

function connect() {
    stompClient = Stomp.over(socket);
    stompClient.connect({}, onConnected, onError);
}

connect();

function onConnected() {
    stompClient.subscribe('/personal/msg/' + rroomid, onMessageReceived);
    connectingElement.textContent = 'connected';
    connectingElement.style.color = 'green'
    var message = {
        author: username,
        content: null,
        roomid: rroomid,
        type: 'CONNECT',
        userid: userid
    };
    stompClient.send("/app/send.entrypoint", {}, JSON.stringify(message));
}


function onError(error) {
    connectingElement.textContent = 'disconnected';
    connectingElement.style.color = 'red';
}

var message = {
    author: username,
    content: null,
    roomid: rroomid,
    type: null,
    userid: ''
};

// image message
// function imageChoose() {
//     var file = document.querySelector('input[type=file]')['files'][0];
//     var reader = new FileReader();
//     console.log("next");
//     reader.onload = function () {
//         var encrypMessage = encrypt(key, reader.result);
//         message.type = 'IMAGE';
//         message.content = encrypMessage;
//     }
//     reader.readAsDataURL(file);
//     messageContent.value = 'IMG: ' + fileInput.value;
// }



function filterImg(element) {
    var regex = /^ IMG:.* $/;
    if (!regex.test(element.value)) {
        console.log("going to send valina message");
        message.type = 'CHAT';
    }
}

async function sendMessage(event) {

    // command section
    if (stompClient) {

        var uuid = document.createElement('span');
        var context = document.createElement('p');
        uuid.textContent = message.author + ': ';
        var devision = document.createElement('div');
        var li = document.createElement('li');

        if (messageContent.value === '/LEAVE') {
            message.type = 'LEAVE';
            stompClient.send("/app/send.command", {}, JSON.stringify(message));
            socket.close();
            window.location.replace("/")
        }

        if (messageContent.value === '/QR') {
            genQRCode();
            messageContent.value = '';
            event.preventDefault();
            return;
        }

        if (messageContent.value === '/INVITE') {
            messageContent.value = '';
            uuid.textContent = window.location.host + '/invite/' + rroomid;
            uuid.setAttribute("id", "copyText");
            uuid.setAttribute("onclick", "copyText();");
            uuid.style.color = '#419CF2';
            li.style = 'list-style-type:none; margin-top:10px; justify-content: left;';
            devision.appendChild(uuid);
            li.appendChild(devision);
            messageArea.appendChild(li);
            messageArea.scrollTop = messageArea.scrollHeight;
            event.preventDefault();
            return;
        }

        if (messageContent.value === '/LOCK') {
            message.type = 'LOCK';
            stompClient.send("/app/send.command", {}, JSON.stringify(message));
            messageContent.value = '';
            event.preventDefault();
            return;
        }

        if (messageContent.value === '/CURMEM') {
            message.type = 'CURMEM';
            stompClient.send("/app/send.command", {}, JSON.stringify(message));
            messageContent.value = '';
            event.preventDefault();
            return;
        }

        if (/^[/]ROOMSIZE [0-9]+$/.test(messageContent.value)) {
            message.type = 'ROOMSIZE';
            message.content = messageContent.value;
            stompClient.send("/app/send.command", {}, JSON.stringify(message));
            messageContent.value = '';
            event.preventDefault();
            return;
        }

        if (messageContent.value === '/UNLOCK') {
            message.type = 'UNLOCK';
            stompClient.send("/app/send.command", {}, JSON.stringify(message));
            messageContent.value = '';
            event.preventDefault();
            return;
        }

        if (messageContent.value === '/HELP') {
            messageContent.value = '';
            context.style.color = '#419CF2';
            var context1 = document.createElement('p');
            var context2 = document.createElement('p');
            var context3 = document.createElement('p');
            var context4 = document.createElement('p');
            var context5 = document.createElement('p');
            var context6 = document.createElement('p');
            var context7 = document.createElement('p');
            context1.innerHTML = "/INVITE : Generate an invitelink";
            context2.textContent = "/LEAVE : Leave conversation";
            context3.textContent = "/LOCK : Lock room only admin";
            context4.textContent = "/UNLOCK : Unlock room only admin";
            context5.textContent = "/CURMEM : Get current member";
            context6.textContent = "/ROOMSIZE : /ROOMSIZE <new_max_member> only admin";
            context7.textContent = "/QR : Generate QR code to invite people";
            devision.appendChild(context1);
            devision.appendChild(context2);
            devision.appendChild(context3);
            devision.appendChild(context4);
            devision.appendChild(context5);
            devision.appendChild(context6);
            devision.appendChild(context7);
            li.style = 'list-style-type:none; margin-top:10px; justify-content: left;';
            li.appendChild(devision);
            messageArea.appendChild(li);
            messageArea.scrollTop = messageArea.scrollHeight;
            event.preventDefault();
            return;
        }

        // TODO image message
        if (message.type === 'IMAGE') {
            stompClient.send("/app/send.chat", {}, JSON.stringify(message));
            messageContent.value = '';
            event.preventDefault();
            return;
        }

        var encrypMessage = encrypt(key, messageContent.value);
        message.content = encrypMessage;
        message.type = 'CHAT';
        stompClient.send("/app/send.chat", {}, JSON.stringify(message));

    }

    messageContent.value = '';
    event.preventDefault();
}

async function onMessageReceived(payload) {

    var message = JSON.parse(payload.body);

    var uuid = document.createElement('span');
    var statusSpan = document.createElement('span');
    var context = document.createElement('p');
    uuid.style.color = '#B4EF57';
    context.style.color = 'white';
    uuid.textContent = message.author + ': ';
    var devision = document.createElement('div');
    var li = document.createElement('li');

    if (message.type === 'NOTAPV') {
        socket.close();
        window.location.replace("/")
        return;
    }

    if (message.type === 'NOTICE') {
        statusSpan.textContent = message.content;
        statusSpan.style.color = 'green';
        li.style = 'list-style-type:none; margin-top:10px; justify-content: left;';
        devision.appendChild(uuid);
        devision.appendChild(statusSpan);
        li.appendChild(devision);
        messageArea.appendChild(li);
        messageArea.scrollTop = messageArea.scrollHeight;
        return;
    }

    if (message.type === 'NEEDPERM') {
        var popup = document.createElement('div');
        popup.className = "pendinglist";
        popup.id = "ppelist"
        var ccontexxt = document.createElement('p');
        ccontexxt.innerHTML = message.author + ' want to join ?';
        var apv = document.createElement('button');
        var notapv = document.createElement('button');
        apv.className = 'btn btn-primary';
        notapv.className = 'btn btn-danger';

        apv.innerHTML = 'APRROVE';
        notapv.innerHTML = 'NO';

        apv.id = 'apv';
        notapv.id = 'notapv'

        popup.appendChild(ccontexxt);
        popup.appendChild(apv);
        popup.appendChild(notapv);

        popup.style.display = 'block';

        apv.onclick = function (event) {
            message.type = 'APPROVE';
            stompClient.send("/app/send.command", {}, JSON.stringify(message));
            popup.style.display = 'none';
            event.preventDefault();
            return;
        }

        notapv.onclick = function (event) {
            message.type = 'NOTAPV';
            stompClient.send("/app/send.command", {}, JSON.stringify(message));
            popup.style.display = 'none';
            event.preventDefault();
            return;
        }

        var body = document.getElementById('boody');
        body.appendChild(popup);

    }

    if (message.type === 'IMAGE') {
        var uuuid = document.createElement('p');
        uuuid.textContent = message.author + ': ';
        uuuid.style.color = '#B4EF57';
        var dataSrc = message.content;
        var img = document.createElement('img');
        var ifImg = /^data:image.*$/;

        img.onclick = function () {
            resizeImg(img);
        }
        img.src = message.content;
        img.setAttribute('class', 'message_img');

        if (!ifImg.test(dataSrc)) {
            img = document.createElement('video');
            img.autoplay = 'autoplay';
            img.controls = true;
            img.muted = true;
            img.loop = true;
            img.setAttribute('class', 'message_vid');
            img.onclick = null;
            var source = document.createElement('source');
            source.src = dataSrc;
            source.type = 'video/mp4';
            img.removeAttribute('src');
            img.appendChild(source);
        }
        img.alt = 'rendering error';

        if (message.userid !== hashKey) {
            li.style = 'list-style-type:none; margin-top:10px; display:block;';
            li.appendChild(uuuid);
            li.appendChild(img);
            messageArea.appendChild(li);
            messageArea.scrollTop = messageArea.scrollHeight;
            notifyMe(message);
            event.preventDefault();
            return;
        }
        var lli = document.createElement('li');
        lli.style = 'list-style-type:none; margin-top:10px; margin-right : 5%; justify-content: flex-end;';
        lli.appendChild(img);
        messageArea.appendChild(lli);
        messageArea.scrollTop = messageArea.scrollHeight;
        event.preventDefault();
        return;
    }

    devision.setAttribute('id', 'mmessage');
    devision.appendChild(uuid);
    context.textContent = decrypt(key, message.content);
    devision.appendChild(context);

    if (message.userid !== hashKey) {

        li.style = 'list-style-type:none; margin-top:10px; justify-content: left;';
        li.appendChild(devision);

        messageArea.appendChild(li);
        messageArea.scrollTop = messageArea.scrollHeight;
        notifyMe(message);
        return;

    }

    uuid.textContent = '';
    devision.appendChild(uuid);
    li.style = 'list-style-type:none; margin-top:10px; margin-right : 5%; justify-content: flex-end;';
    li.appendChild(devision);
    messageArea.appendChild(li);
    messageArea.scrollTop = messageArea.scrollHeight;

    return;
}

window.addEventListener('beforeunload', function (e) {
    if (stompClient) {
        var message = {
            author: username,
            content: null,
            roomid: rroomid,
            type: null,
            userid: ''
        };

        message.type = 'LEAVE';
        stompClient.send("/app/send.command", {}, JSON.stringify(message));
        socket.close();
        window.location.replace("/")

    }
});

messageForm.addEventListener('submit', sendMessage, true);