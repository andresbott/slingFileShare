
// Set of functions that come handy when using esp Rendering
var app ={
    request: request,
    pathInfo: request.getRequestPathInfo(),
    resourceResolver: request.getResourceResolver(),
    resource: resource,
    //properties: resource.adaptTo(Packages.org.apache.sling.api.resource.ValueMap),
    currentNode: currentNode,
    session: currentNode.getSession(),
    userID : currentNode.getSession().getUserID(),
    // pageManager: request.getResourceResolver().adaptTo(Packages.com.day.cq.wcm.api.PageManager),
    //xss: sling.getService(Packages.com.adobe.granite.xss.XSSAPI)
};

/**
 * Get the sufix of the Sling reques
 * @returns {string}
 */
function getSlingSuffix(){
    var Suffix = request.getRequestPathInfo().suffix+"";
    if(typeof(Suffix) == "string" && Suffix != "null" && Suffix != "/" ){
        Suffix = Suffix.substring(1);
    }else{
        Suffix = "";
    }
    return Suffix;
}

/**
 * Get the Get an Array of selectors
 * @returns {string}
 */
function getSelectors(){
    var R = [];
    R = request.getRequestPathInfo().selectorString+"";
    R = R.split(".");
    return R;
}

/**
 * check if a selector is deffined
 * @param selector
 * @returns {boolean}
 */
function isSelector(selector){
    var R = getSelectors();

    if(R.indexOf(selector) > -1){
        return true;
    }else{
        return false;
    }
}


function getServerData(){

    return server = {
        scheme : request.scheme,
        name: request.serverName,
        port: request.serverPort,
        suffix : getSlingSuffix(),
        baseURL : request.scheme+ "://"+ request.serverName + ":"+ request.serverPort
    };

}



function getNodeProperty(propname){
    var R = "";
    if(propname in currentNode){
        var val = currentNode[propname];
        if(typeof(val) != "undefined"){
            R = val
        }
    }
    return R;
}

function getRequestParameter(param){
    var R="";
    if(typeof (param) == "string"){
        R = app.request.getParameter(param);
        R=R+"";
        if(R == "null"){
            R= "";
        }
    }
    return R;
}


