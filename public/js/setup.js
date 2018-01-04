if (typeof String.prototype.startsWith != 'function') {
    String.prototype.startsWith = function(str) {
        return this.slice(0, str.length) == str;
    };
}
if (typeof String.prototype.endsWith != 'function') {
    String.prototype.endsWith = function(str) {
        return this.slice(-str.length) == str;
    };
}
if (typeof String.prototype.stripTags != 'function') {
    String.prototype.stripTags = function() {
        var tmp = document.createElement("DIV");
        tmp.innerHTML = this;
        return tmp.textContent || tmp.innerText || "";
    }
}
function rand(length) {
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    for (var i = 0; i < length; i++)
        text += possible.charAt(Math.floor(Math.random() * possible.length));
    return text;
}
function resizeInput() {
    // will resize our inputs to the input value size
    $(this).attr('size', $(this).val().length ? $(this).val().length : $(this).attr('placeholder') ? $(this).attr('placeholder').length : '20');
}
$(document).ready(function() {
    $('textarea').attr('rows', '1');
    makeDynamic();
});