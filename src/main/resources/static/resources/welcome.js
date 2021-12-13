var btnCreate = document.getElementById("btnCreate")
var configSetting = document.getElementById("chatconfig")
var btnClose = document.getElementById("btnClose")

btnCreate.onclick = function () {
    configSetting.style.display = "block"
}

btnClose.onclick = function () {
    configSetting.style.display = "none"
}