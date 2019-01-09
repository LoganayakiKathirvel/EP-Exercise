/*
 * json-formatter.js  version 1.0
 * http://quickjsonformatter.codeplex.com/
 * http://blog.bodurov.com/Formatter-and-colorer-of-raw-JSON-code
 *
 * Copyright (c) 2008 Vladimir Bodurov
 * Licensed under the MIT licence.
 * http://quickjsonformatter.codeplex.com/license
 *
 * Added in Cortex semantics:
 *     window.inHref
 *     window.inForm
 *     window.inputIds
 *     window.nonFormProperties
 *     .Rel, .Href CSS classes
 *
 * Removed code that was not relevant to our UI.
 */

// we need tabs as spaces and not CSS margin-left
// in order to retain format when coping and pasting the code
window.SINGLE_TAB = "  ";
window.FORMATTED_JSON_DIV = "FormattedJson";
window.ImgCollapsed = "images/Collapsed.gif";
window.ImgExpanded = "images/Expanded.gif";
window.QuoteKeys = false;
window._dateObj = new Date();
window._regexpObj = new RegExp();
window.IsCollapsible = true;
window.TAB = "   ";
// Cortex Extensions
//START_INPUT_ID is a known ID to help the field ID generator find a unique id.
window.START_INPUT_ID = 'START';
window.INPUT_ID_PREFIX = 'inputId_';
window.nonFormProperties = ['self', 'links', 'messages'];


function $id(id) {
  return document.getElementById(id);
}


function IsArray(obj) {
  return obj &&
    typeof obj === 'object' &&
    typeof obj.length === 'number' &&
    !(obj.propertyIsEnumerable('length'));
}


/*
  If window.inForm is true, then display a form for non-standard fields. Standard fields include
  self and links.
 */
function Process(json){
  var html = "";
  try{
    var obj = null;
    if (typeof json == 'object') {
      obj = json;
    } else {
      if(json == "") json = "\"\"";
      obj = eval("["+json+"]")[0];
    }

    window.currentPropStack = [];
    if (window.inForm) {
      //clear out the inputIds map
      window.inputIds = { };
      window.inputIds[START_INPUT_ID] =  ' ';
    }
  //put the links at the bottom; this seems to work
    var currentLinks = obj.links;
    if (currentLinks) {
      delete obj.links;
      obj.links = currentLinks;
    }
    //currentObject is the object being rendered.
    window.currentObject = obj;
    html = ProcessObject(obj, 0, false, false, false);
    if (window.inForm) {
      //wrap it in a <form> so that the user can hit ENTER/RETURN in text fields to submit
      html = '<form id="submitForm" onsubmit="return false;">' + html + '</form>';
    }
    $id(FORMATTED_JSON_DIV).innerHTML = "<PRE class='CodeContainer'>"+html+"</PRE>";
    if (window.inForm) {
      $('#submitForm :text').first().focus();
    }
  }catch(e){
    alert("JSON is not well formatted:\n"+e.message);
    $id(FORMATTED_JSON_DIV).innerHTML = "";
  }
}

function ProcessObject(obj, indent, addComma, isArray, isPropertyContent){
  var html = "";
  var comma = (addComma) ? "<span class='Comma'>,</span> " : "";
  var type = typeof obj;
  var clpsHtml ="";
  if(IsArray(obj)){
    if(obj.length == 0){
      html += GetRow(indent, "<span class='ArrayBrace'>[ ]</span>"+comma, isPropertyContent);
    }else{
      clpsHtml = window.IsCollapsible ? "<span><img src=\""+window.ImgExpanded+"\" onClick=\"ExpImgClicked(this)\" /></span><span class='collapsible'>" : "";
      html += GetRow(indent, "<span class='ArrayBrace'>[</span>"+clpsHtml, isPropertyContent);
      for(var i = 0; i < obj.length; i++){
        window.currentPropStack.push(i);
        html += ProcessObject(obj[i], indent + 1, i < (obj.length - 1), true, false);
        window.currentPropStack.pop();
      }
      clpsHtml = window.IsCollapsible ? "</span>" : "";
      html += GetRow(indent, clpsHtml+"<span class='ArrayBrace'>]</span>"+comma);
    }
  }else if(type == 'object'){
    if (obj == null){
        html += FormatLiteral("null", "", comma, indent, isArray, "Null");
    }else if (obj.constructor == window._dateObj.constructor) {
        html += FormatLiteral("new Date(" + obj.getTime() + ") /*" + obj.toLocaleString()+"*/", "", comma, indent, isArray, "Date");
    }else if (obj.constructor == window._regexpObj.constructor) {
        html += FormatLiteral("new RegExp(" + obj + ")", "", comma, indent, isArray, "RegExp");
    }else{
      var numProps = 0;
      for(var prop in obj) numProps++;
      if(numProps == 0){
        html += GetRow(indent, "<span class='ObjectBrace'>{ }</span>"+comma, isPropertyContent);
      }else{
        clpsHtml = window.IsCollapsible ? "<span class=\"ExpandCollapse\"><img src=\""+window.ImgExpanded+"\" onClick=\"ExpImgClicked(this)\" /></span><span class='collapsible'>" : "";
        html += GetRow(indent, "<span class='ObjectBrace'>{</span>"+clpsHtml, isPropertyContent);
        var j = 0;
        for(var prop in obj){
          var quote = window.QuoteKeys ? "\"" : "";
          window.inHref = 'href' == prop;
          window.inRel = 'rel' == prop;
          var inForm = window.inForm;
          if (inForm) {
            //turn off form fields for these standard properties
            if (window.nonFormProperties.indexOf(prop) > -1) {
              window.inForm = false;
            } else {
              window.currentPropStack.push(prop);
            }
          }
          html += GetRow(indent + 1, "<span class='PropertyName'>"+quote+prop+quote+"</span>: "+ProcessObject(obj[prop], indent + 1, ++j < numProps, false, true));
          if (window.inForm) {
            window.currentPropStack.pop();
          }
          window.inForm = inForm;
        }
        clpsHtml = window.IsCollapsible ? "</span>" : "";
        html += GetRow(indent, clpsHtml+"<span class='ObjectBrace'>}</span>"+comma);
      }
    }
  }else if(type == 'number'){
    html += FormatLiteral(obj, "", comma, indent, isArray, "Number");
  }else if(type == 'boolean'){
    html += FormatLiteral(obj, "", comma, indent, isArray, "Boolean");
  }else if(type == 'function'){
    if (obj.constructor == window._regexpObj.constructor) {
        html += FormatLiteral("new RegExp(" + obj + ")", "", comma, indent, isArray, "RegExp");
    }else{
        obj = FormatFunction(indent, obj);
        html += FormatLiteral(obj, "", comma, indent, isArray, "Function");
    }
  }else if(type == 'undefined'){
    html += FormatLiteral("undefined", "", comma, indent, isArray, "Null");
  }else{
    html += FormatLiteral(obj.toString().split("\\").join("\\\\").split('"').join('\\"'), "\"", comma, indent, isArray, "String");
  }
  return html;
}


function FormatLiteral(literal, quote, comma, indent, isArray, style){
  if(typeof literal == 'string')
    literal = literal.split("<").join("&lt;").split(">").join("&gt;");
  if (window.inForm) {
    //start with a known ID that will be defined.
    var inputId = window.START_INPUT_ID;
    while (window.inputIds[inputId] != undefined) {
      inputId = INPUT_ID_PREFIX + Math.random();
    }
    window.inputIds[inputId] = window.currentPropStack.join('|');
    str = '<input type="text" size="40" class="FormField form-control" id="' + inputId + '"';
    if (literal.toString().length > 0) {
      str += ' value="' + literal + '"';
    }
    str += '/>';
  }else {
    if (window.inHref) {
      literal = '<span class="Href">'+literal+'</span>';
    } else if (window.inRel) {
      literal = '<span class="Rel">'+literal+'</span>';
    }
    var str = "<span class='"+style+"'>"+quote+literal+quote+comma+"</span>";
    if(isArray) str = GetRow(indent, str);
  }
  return str;
}


function FormatFunction(indent, obj){
  var tabs = "";
  for(var i = 0; i < indent; i++) tabs += window.TAB;
  var funcStrArray = obj.toString().split("\n");
  var str = "";
  for(var i = 0; i < funcStrArray.length; i++){
    str += ((i==0)?"":tabs) + funcStrArray[i] + "\n";
  }
  return str;
}


function GetRow(indent, data, isPropertyContent){
  var tabs = "";
  for(var i = 0; i < indent && !isPropertyContent; i++) tabs += window.TAB;
  if(data != null && data.length > 0 && data.charAt(data.length-1) != "\n")
    data = data+"\n";
  return tabs+data;
}

function TraverseChildren(element, func, depth){
  for(var i = 0; i < element.childNodes.length; i++){
    TraverseChildren(element.childNodes[i], func, depth + 1);
  }
  func(element, depth);
}


function ExpImgClicked(img){
  var container = img.parentNode.nextSibling;
  if(!container) return;
  var src = window.ImgCollapsed;
  if(container.style.display == "none"){
      src = window.ImgExpanded;
  }
  img.src = src;
  $(container).fadeToggle('fast');
}
