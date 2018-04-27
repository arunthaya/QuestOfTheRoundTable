var JDEV = JDEV || {};


JDEV.namespace = function(ns_string) {
    var parts = ns_string.split('.');
    var parent = JDEV;

    // strip redundant leading global
    if (parts[0] === "JDEV") {
        parts = parts.slice(1);
    }
    for (var i = 0; i < parts.length; i += 1) {
        // create a property if it doesn't exist
        if (typeof parent[parts[i]] === "undefined") {
            parent[parts[i]] = {};
        }
        parent = parent[parts[i]];
    }
    return parent;
};


JDEV.namespace('logging');
JDEV.logging = (function() {

    var logToServer = function(logMessage) {
        var logEventObject = {
            "message" : logMessage,
            "location" : location.href,
            "browser" : navigator.userAgent,
        };
        var logMsg = JSON.stringify(logEventObject);
        var url = "js-log";
        $.ajax({
            type : "POST",
            url : url,
            data : logMsg,
            contentType : "application/json",
            cache : "false",
        });
    }

    return {
        logToServer : logToServer,
    }

})();