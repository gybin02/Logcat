var myTimer;
var isTimerRunning = true;
var currentScroll = 0;
var currentScrollTAG = "currentScroll";
var lastPositionTag = "lastPos";

function reloadFunction() {
    document.cookie = currentScrollTAG + "=" + document.body.scrollTop;
    window.location.href = document.URL
}

function stopTemp() {
    if(isTimerRunning){
        document.getElementById("loaderProgress").style.display="none";
        clearTimeout(myTimer), document.body.style.backgroundColor = "#252525";
        document.getElementById("buttonStop").innerText="开始自动刷新"
    }else{
        reloadFunction();
    }
    isTimerRunning=!isTimerRunning;
}

function startTemp() {
    var lastPos = getCookie(lastPositionTag);
    currentScroll = getCookie(currentScrollTAG);

    if (lastPos === undefined || lastPos == null || lastPos.length <= 0 || lastPos < document.body.scrollHeight){
        window.scrollTo(0, currentScroll);
        document.querySelector('#endOfFile').scrollIntoView({
            behavior: 'smooth'
        });
        document.cookie = "lastPos=" + document.body.scrollHeight;
    }else{
        if(lastPos > document.body.scrollHeight){
            document.cookie = "lastPos=" + document.body.scrollHeight;
        }
        window.scrollTo(0, currentScroll);
    }
    myTimer = window.setTimeout(reloadFunction, #MILLISECONDS_TO_RELOADING#);
}

function getCookie(cname) {
    var name = cname + "=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(';');
    for(var i = 0; i <ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

function clearLog() {
    document.body.innerHTML += '<form id="dynForm" action="' + document.URL + '" method="post"><input type="hidden" name="operation" value="clean"></form>', document.getElementById("dynForm").submit()
}

function searchContainsString(){
    window.location.href = window.location.origin + "/log?filterContains=" + document.getElementById("inputSearch").value;
}

function onInputSearchClick(){
    if (isTimerRunning){
        stopTemp();
    }
}

function syntaxHighlight(json) {
    json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function(match) {
        var cls = 'number';
        if (/^"/.test(match)) {
            if (/:$/.test(match)) {
                cls = 'key';
            } else {
                cls = 'string';
            }
        } else if (/true|false/.test(match)) {
            cls = 'boolean';
        } else if (/null/.test(match)) {
            cls = 'null';
        }
        return '<span class="' + cls + '">' + match + '</span>';
    });
}