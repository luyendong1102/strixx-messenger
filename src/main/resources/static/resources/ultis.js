function copyText() {
    var rooid = document.getElementById("copyText");
    var r = document.createRange();
    r.selectNode(document.getElementById("copyText"));
    window.getSelection().removeAllRanges();
    window.getSelection().addRange(r);
    document.execCommand('copy');
    window.getSelection().removeAllRanges();
    alert("Copied: " + rooid.innerText);
}

function resizeImg(currentIMG) {
    console.log("imageview invoke");
    var img = document.getElementById("imagecontent");
    var container = document.getElementById("imageviewer");
    img.src = currentIMG.src;
    img.alt = 'message image';
    container.style.display = 'block';
}

async function imageChoose() {
    var file = document.querySelector('input[type=file]')['files'][0];
    
    if ((file.size / 10 / 1024 / 1024) > 1) {
        alert("File must be smaller than 20MB");
        return;
    }

    messageContent.value = 'IMG: ' + fileInput.value;

    var reader = new FileReader();
    reader.onload = function () {
        var encrypMessage = encrypt(key, reader.result);
        message.type = 'IMAGE';
        message.content = encrypMessage;
    }
    reader.readAsDataURL(file);
    
}

// function imageChoose() {
//     var file = document.querySelector('input[type=file]')['files'][0];
//     if ((file.size / 20 / 1024 / 1024) > 1) {
//         alert("File must be smaller than 20MB");
//     }
//     var reader = new FileReader();
//     reader.onload = function () {
//         var dataurl = reader.result;

//         // image validate
//         var ifImg = /^data:image.*$/;
//         if (ifImg.test(dataurl)) {
//             var encrypMessage = encrypt(key, dataurl);
//             message.type = 'IMAGE';
//             message.content = encrypMessage;
//             return;
//         }

//         var pseudoVideo = document.createElement("video");
//         pseudoVideo.src = dataurl;
//         var width = pseudoVideo.videoWidth;
//         var height = pseudoVideo.videoHeight;

//         console.log(width);
//         console.log(height);
//         return;
        

//         var encrypMessage = encrypt(key, dataurl);
//         message.type = 'IMAGE';
//         message.content = encrypMessage;
//     }
//     reader.readAsDataURL(file);
//     messageContent.value = 'IMG: ' + fileInput.value;
// }