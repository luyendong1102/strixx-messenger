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
    var img = document.getElementById("imagecontent");
    var container = document.getElementById("imageviewer");
    img.src = currentIMG.src;
    container.style.display = 'block';
}