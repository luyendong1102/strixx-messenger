// a0aJsjdn/as

function encrypt(key, message) {

    var base64String = window.btoa(unescape(encodeURIComponent(key)), 'base64');
    var base64Bytes = CryptoJS.enc.Base64.parse(base64String);
    var encryptedData = CryptoJS.AES.encrypt(message, base64Bytes, {
        mode: CryptoJS.mode.ECB,
        padding: CryptoJS.pad.Pkcs7
    });

    return encryptedData.toString();

}

function decrypt(key, message) {
    var base64String = window.btoa(unescape(encodeURIComponent(key)), 'base64');
    var base64Bytes = CryptoJS.enc.Base64.parse(base64String);
    var encryptedData = CryptoJS.AES.decrypt(message, base64Bytes, {
        mode: CryptoJS.mode.ECB,
        padding: CryptoJS.pad.Pkcs7
    });

    return encryptedData.toString(CryptoJS.enc.Utf8);
}