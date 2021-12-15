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