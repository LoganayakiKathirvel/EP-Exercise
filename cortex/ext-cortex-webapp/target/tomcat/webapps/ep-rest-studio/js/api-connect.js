/*
 * This JS file contains code to connect to Cortex.
 * It is aware of the kind of responses one can get and deals appropriately with errors.
 *
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */

//namespace
window.cortex = {

  /******
   CONSTANTS
   ******/
  STORAGE_PREFIX: 'epstudio_',
  DEFAULT_SCOPE: 'mobee',
  DEFAULT_ROLE: 'PUBLIC',

  /******
   SETTINGS
   ******/
  //URL Prefix for URIs
  serverPrefix: window.location.protocol + '//' + window.location.host + '/cortex',
  //where ajax is calling now
  destinationUrl: false,
  //if there was an auth problem, this is where ajax was going
  destBeforeAuth: false,
  //the data being submitted for a form. It is stored so that it can be used to populate the same form on failure
  submittedData: false,

  // value for x-ep-user-traits header
  userTraitsHeader: '',
  // value for x-forwarded-base header
  forwardedBaseHeader: '',
  // value for x-ep-user-id header
  userIdHeader: '',
  // value for x-ep-data-policy-segments header
  dataPolicySegmentsHeader: '',
  // value for x-ep-user-roles header
  rolesHeader: '',
  // value for x-ep-user-scopes header
  scopesHeader: '',
  // value for OAuth token
  authHeader: '',
   // custom values initial value
  customEntries: [],

  // values for authentication, can be overridden later in app
  oAuthScope: 'mobee',
  oAuthRole: 'PUBLIC',
  oAuthUserName: '',


  /******
   AJAX
   ******/
  GET: function (url) {
    if (url) {
      //create the history token and load
      var uri = url.substring(cortex.serverPrefix.length);
      var currHash = window.location.hash.substr(1)
        .replace('%26', '&');

      // if uri is same as last one in history, manually trigger the onhashchange event
      // to force the jQuery.history plugin to make a new request. This helps ensure
      // that the request is still made when only request headers change, etc.
      if (uri === currHash) {
        $(window).trigger('hashchange');
      }
      else {
        $.history.load(uri);
      }
    }
  },

  //GET the URL, called by the History controller.
  GET_history: function (url, successCallback, failureCallback) {
    var promise;

    if (url) {
      //Note where we are going
      cortex.destinationUrl = url;
      promise = cortex._send('get', url, null, function (data, textStatus, jqXHR) {
        cortex.currResponse = {
          json: data,
          responseHeaders: jqXHR.getAllResponseHeaders(),
          status: jqXHR.status + " " + jqXHR.statusText
        };
        successCallback(cortex.currResponse);
      }, failureCallback, 'json');
    }

    return promise;
  },

  POST: function (url, data, successCallback, failureCallback) {
    url = cortex.addFollowLocationQuery(url);

    // Limit to batch lookup only
    var urlRegExp = new RegExp("/batches/items/form");
    if (urlRegExp.test(url)) {
        var codes = [];
        for (var property in data) {
            var propertyValuesStr = data[property] + '';
            var propertyValues = propertyValuesStr.split(',');

            for(var i = 0; i < propertyValues.length; i++) {
                codes.push(propertyValues[i].trim());
            }
            data[property] = codes;
        }
    }

    cortex.submittedData = data;
    cortex.submittedUrl = url;
    return cortex._send('post', url, data, successCallback, failureCallback);
  },

  PUT: function (url, data, successCallback, failureCallback) {
    cortex.submittedData = data;
    return cortex._send('put', url, data, successCallback, failureCallback);
  },

  DELETE: function (url, successCallback, failureCallback) {
    return cortex._send('delete', url, null, successCallback, failureCallback);
  },

  _send: function (verb, url, data, successCallback, failureCallback, dataType) {
    //when one passes in all the params, one has to specify all the globals
    var params = {
      type: verb,
      url: url,
      headers: {
        'x-ep-user-traits': cortex.userTraitsHeader,
        'x-forwarded-base': cortex.forwardedBaseHeader,
        'x-ep-user-id': cortex.userIdHeader,
        'x-ep-data-policy-segments': cortex.dataPolicySegmentsHeader,
        'x-ep-user-roles': cortex.rolesHeader,
        'x-ep-user-scopes': cortex.scopesHeader,
        'Authorization': cortex.authHeader
      },
      //these are not the same name as in jqXHR!
      success: successCallback,
      error: failureCallback,
      complete: function () {
        cortex.ui.spin.stop();  // stops the spinner indicator
      }
    };
    if (dataType) {
      params.dataType = dataType;
    }
    if (data) {
      params.data = JSON.stringify(data);
      if (window.console) {
        window.console.log('sending data to ' + url + ' : ' + params.data);
      }
    }
    return $.ajax(params);
  },

  addFollowLocationQuery: function (url) {
    if (url.indexOf('?') < 0) {
      url += '?followlocation';
    } else {
      url += '&followlocation';
    }
    return url;
  },

  followLocationHeader: function (jqXHR) {
    var location = jqXHR.getResponseHeader('Location');
    if (location) {
      cortex.GET(location);
      return true;
    }
    return false;
  },

  getDefaultErrorHandlers: function () {
    var handlers = {
      400: cortex.processServerError,
      401: cortex.processAuthError,
      403: cortex.processAuthError,
      404: cortex.processServerError,
      405: cortex.processServerError,
      409: cortex.processServerError,
      412: cortex.processServerError,
      415: cortex.processServerError,
      500: cortex.processServerError,
      501: cortex.processServerError,
      502: cortex.processServerError,
      503: cortex.processServerError
    };

    return handlers;
  },

  //handle failures
  handleFailure: function (jqXHR) {
    jqXHR.statusCode(cortex.getDefaultErrorHandlers());
  },

  //Handle auth failures
  processAuthError: function (jqXHR) {
    cortex.processServerError(jqXHR);
    if (!cortex.destBeforeAuth) {
      //first time into auth failure state, so set the destination
      cortex.destBeforeAuth = cortex.destinationUrl;
    }
  },

  processServerError: function (jqXHR) {
    // determine the source of error message (from responseText or statusText
    var message = jqXHR.responseText;
    if (message && message.length === 0) {
      message = jqXHR.statusText;
    }
    var jsonResponse = jqXHR.responseJSON
    if (!jsonResponse && jqXHR.getResponseHeader("Content-Type").toLowerCase().trim() == "application/json") {
      jsonResponse = JSON.parse(message)
    }
    cortex.ui.alertMessage('Error Status: ' + jqXHR.status, message, jsonResponse);

    // unhide response before failure
    cortex.ui.showResponseArea();
  }
};


/**********
 * App setup
 **********/
// Set up jquery ajax defaults for non-$.ajax() calls.
$.ajaxSetup({
  timeout: 10 * 60 * 1000,  //10 minutes
  async: true,
  contentType: 'application/json',
  cache: false
});
