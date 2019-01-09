/*
 * This file contains code to listen to the UI and connect to Cortex to process the calls.
 *
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */

window.cortex.ui = {

  /****************
   User Interface
   ****************/
  CUSTOM_ENTRIES_KEY: "customEntries",

  ENTRY_TO_EDIT: "",
  CURRENT_ENTRY_VALUE: "",

  USER_ENTRY_BUTTON_ADD: "Add",
  USER_ENTRY_BUTTON_EDIT: "Edit",

  setServerPrefix: function () {
    var prefixDiv = $('#serverPrefix');
    var prefix = prefixDiv.val().trim();
    if (prefix.length > 0) {
      if (prefix.charAt(prefix.length - 1) === '/') {
        prefix = prefix.substring(0, prefix.length - 1);
        prefixDiv.val(prefix);
      }
    }
    cortex.ui.saveField('serverPrefix');

    // display a fade away message "Change saved"
    var $changeMsg = $('<div id="serverPrefixChanged"><p class="text-success text-center">Cortex server updated.</p></div>');
    $('#setup-panel').prepend($changeMsg).fadeIn('slow');
    $changeMsg.delay(1200).fadeOut('slow');
  },

  saveField: function (field) {
    var value = $('#' + field).val();
    cortex.ui.saveToStorage(field, value);
  },

  saveToStorage: function (key, value) {
    if (typeof value === 'object') {
      value = JSON.stringify(value);
    } else {
      value = value.trim();
    }
    cortex[key] = value;
    if (Modernizr.localstorage) {
      localStorage[cortex.STORAGE_PREFIX + key] = value;
    }
    else if (chrome.storage) {
      var frame = {};
      frame[key] = value;
      chrome.storage.local.set(frame, function () {
        console.log(key + ': ' + value + ' saved.')
      });
    }
    else {
      $.cookie(cortex.STORAGE_PREFIX + key, value);
    }
  },

  loadFromStorage: function (key) {
    // load value from localStorage or cookie
    var value;

    if (Modernizr.localstorage) {
      value = localStorage[cortex.STORAGE_PREFIX + key] || cortex[key];
    } else if (chrome.storage) {
      chrome.storage.local.get(key, function (row) {
        value = row[key];
      });
    } else {
      value = $.cookie(cortex.STORAGE_PREFIX + key) || cortex[key];
    }

    return value;
  },

  loadField: function (field) {
    var value = cortex.ui.loadFromStorage(field);
    if (value) {
      // save value to cortex
      cortex[field] = value;
      // set the value in corresponding form fields
      $('#' + field).val(value);
    }
  },

  removeFromStorage: function (key) {
    if (Modernizr.localstorage) {
      window.localStorage.removeItem(cortex.STORAGE_PREFIX + key);
    } else if (chrome.storage) {
      chrome.storage.local.remove(key);
    }
    cortex[key] = '';
  },

  removeField: function (field) {
    cortex.ui.removeFromStorage(field);
    $('#' + field).val('');
  },

  getUriField: function () {
    var uri = $('#uriField').val().trim();
    if (uri.length > 0) {
      // prepend splash (/) at begining of request
      if (uri.charAt(0) !== '/') {
        uri = '/' + uri;
      }

      // trim away queries (queries at with ?)
      uri = uri.split('?')[0];
      return uri;
    }
    return false;
  },

  loadDataFromForm: function () {
    var submitData = {};
    $('#submitForm').find(':text').each(function () {
      var fieldValue = $(this).val();
      var path = window.inputIds[this.id];
      cortex.ui.addFieldsToPutData(cortex.currResponse.json, submitData, path, fieldValue);
    });
    return submitData;
  },

  removeKey: function (obj, key, isNested) {
    var k;
    if (obj instanceof Object) {
      for (k in obj) {
        if (obj.hasOwnProperty(k)) {
          if (k == key && isNested == true) {
            delete obj[k]
          }
          cortex.ui.removeKey(obj[k], key, true);
        }

      }
    }
  },

  showJSON: function (response) {
    cortex.ui.showingJSON = true;

    //Remove zoomed self and links fields if configured to do so
    if ($('#removeSelf').prop('checked')) {
      cortex.ui.removeKey(response.json, 'self', false);
    }
    if ($('#removeLinks').prop('checked')) {
      cortex.ui.removeKey(response.json, 'links', false);
    }

    //Source View
    cortex.ui.processSourceView(response);

    //Structure View
    Process(response.json);
    //set the URI and Zoom fields
    var fullUri = jsonPath(cortex.currResponse.json, '$.self.uri')[0];
    if (fullUri) {
      //set the location URI
      $('#uriField').val(fullUri);

      // fill the zoom field
      var uriArray = fullUri.split('zoom=');
      $('#zoomField').val(uriArray[1]);
    }
    //populate zoom auto-complete list
    cortex.zoom.populateAutoComplete();

    //show action buttons
    var urls = jsonPath(response.json, '$.links.*');
    $.each($('.Rel'), function (index) {
      var data = urls[index];
      //the number of .Rel elements can be greater than the urls found in links in zoomed results
      if (data) {
        var rel = data.rel;
        //turn link into a button if the rel ends with 'action'
        if (/action$/g.test(rel)) {
          var submitFormHtml = '<button class="submit-btn" value="' + rel + '@' + data.href + '">' + rel + '</button>';
          //replace the rel string in the UI with this button
          $(this).parent().replaceWith(submitFormHtml);
        }
      }
    });

    //fade in the view
    $('#FormattedJson').fadeIn('slow', cortex.ui.afterFadeIn);
    cortex.ui.showingJSON = false;

    /**
     * On hover of strings formatted by the 'json-formatter' toggle a class (after a delay)
     * to subtly disable the text-overflow: ellipsis styling.
     */
    var delayedToggle;
    $('#FormattedJson .String').mouseenter(function () {
      var self = this;
      delayedToggle = setTimeout(function () {
        $(self).addClass('expand');
      }, 500);

    }).mouseleave(function () {
      clearTimeout(delayedToggle);
      $(this).removeClass('expand');
    });
  },

  processSourceView: function (response) {
    var formattedCode = FormatJSON(response.json);
    var content = [
      response.status,
      response.responseHeaders,
      formattedCode
    ];
    cortex.ui.sourceEditor.setValue(content.join('\n'));
  },

  showResponseArea: function () {
    $('#FormattedJson').fadeIn('slow', cortex.ui.afterFadeIn);
  },

  afterFadeIn: function () {
    //highlight needinfo links
    $('.Rel:contains(needinfo)').blink();
  },

  alertMessage: function (title, message, jsonResponse) {
    //clear previous messages
    $('#msgModalHtml').html('');
    cortex.ui.errorJson.setValue('')

    // populate modal with message
    $('#msgModalTitle').text(title);
    if (jsonResponse) {
      cortex.ui.errorJson.setValue(FormatJSON(jsonResponse))
    } else {
      if (message.indexOf('<html>') > -1) {
        $('#msgModalHtml').html(message);
      } else {
        cortex.ui.errorJson.setValue(message)
      }
    }

    var that = cortex.ui;
    setTimeout(function() {
        that.errorJson.refresh();
      },1);
    $('#msgModal').modal({
      'show': true,
      'backdrop': false
    });
  },

  spin: {
    //spinner target
    target: '',

    start: function (region) {
      if (!(region && region.selector)) {
        return;
      }
      this.target = region;
      // decorate target so the overlay can display correctly
      this.target.addClass('spin-overlay-anchor');
      this.target.spin();
    },

    stop: function () {
      if (this.target) {
        this.target.spin(false);
        this.target.removeClass('spin-overlay-anchor');
        this.target = '';
      }
    }
  },

  showForm: function (response) {
    //merge submitted object into json from server to copy previous fields
    if (cortex.submittedData && cortex.submittedData.self && cortex.submittedData.self.uri === response.json.self.uri) {
      cortex.ui.mergeObject(cortex.submittedData, response.json);
      cortex.submittedData = false;
    }
    window.inForm = true;
    cortex.ui.showJSON(response);
  },

  handleHistory: function (hash) {
    if (hash.length > 0) {
      var url = cortex.serverPrefix + hash;
      //reset a bit of state
      window.inForm = false;

      $('#FormattedJson').fadeOut('fast', function () {
        if (/\/form$/.test(url)) {
          cortex.GET_history(url, cortex.ui.showForm, cortex.handleFailure);
        }
        else {
          cortex.GET_history(url, cortex.ui.showJSON, cortex.handleFailure);
        }
      });
    }
  },

  //handle GET button
  handleGetUriButton: function (uri) {
    if (uri) {
      cortex.GET(cortex.serverPrefix + uri);
    }
  },

  //handle DELETE button
  handleDeleteUriButton: function (uri) {
    if (uri) {
      var success = function () {
        var title = 'DELETE succeeded';
        var message = 'Successfully delete resource at ' + uri;
        cortex.ui.alertMessage(title, message);

        $('.CodeContainer').html('(empty)');
      };
      cortex.DELETE(cortex.serverPrefix + uri, success, cortex.handleFailure);
    }
  },

  //handle PUT button
  handlePUTButton: function () {
    var url = cortex.currResponse.json.self.href;

    var submitData = cortex.ui.loadDataFromForm();
    var success = function () {
      var title = 'PUT succeeded';
      var message = 'Successfully updated resource at ' + url;
      cortex.ui.alertMessage(title, message);

      cortex.GET(url);

      // make input fields editable again.
      window.inForm = true;
      cortex.ui.showJSON(cortex.currResponse);
    };
    cortex.PUT(url, submitData, success, cortex.handleFailure);
  },

  //handle POST button
  handlePOSTButton: function () {
    var url = cortex.currResponse.json.self.href;

    var submitData = cortex.ui.loadDataFromForm();
    var success = function () {
      var title = 'POST succeeded';
      var message = 'Successfully created resource at ' + url;
      cortex.ui.alertMessage(title, message);

      cortex.GET(url);

      // make input fields editable again.
      window.inForm = true;
      cortex.ui.showJSON(cortex.currResponse);
    };
    cortex.POST(url, submitData, success, cortex.handleFailure);
  },

  handleMakeRequestButton: function () {
    var uri = cortex.ui.getUriField() + cortex.zoom.getZoomQuery();
    var operationType = $('#operationType').val();

    switch (operationType) {
      case 'GET':
        cortex.ui.handleGetUriButton(uri);
        break;
      case 'PUT':
        cortex.ui.handlePUTButton();
        break;
      case 'POST':
        cortex.ui.handlePOSTButton();
        break;
      case 'DELETE':
        cortex.ui.handleDeleteUriButton(uri);
        break;
    }

    cortex.ui.spin.start($('.CodeContainer')); // start a spinner overlay
  },

  handleOperationTypeChange: function () {
    // if no response, do not execute rest of the function body
    if (!cortex.currResponse.json) {
      return;
    }

    var operationType = $('#operationType').val();

    if (operationType === 'PUT' || operationType === 'POST') {
      window.inForm = true;
      // Add an extra class so Bootstrap styles the text fields
      $('#structure-tab :text').addClass('form-control');
    }
    else {
      window.inForm = false;
    }
    cortex.ui.showJSON(cortex.currResponse);
  },

  //Handle link clicks
  handleHrefClick: function () {
    var url = $(this).text();
    if (url.indexOf(cortex.serverPrefix) < 0) {
      url = cortex.serverPrefix + url;
    }

    $('#operationType').selectpicker('val', 'GET');
    cortex.ui.spin.start($('.CodeContainer')); // start a spinner overlay
    cortex.GET(url);
  },

  handleSubmitForm: function (event) {
    var submitData = cortex.ui.loadDataFromForm();
    var values = event.target.value.split('@');
    var url = values[1];
    cortex.POST(url, submitData, cortex.ui.formSubmitSuccess, cortex.handleFailure);
    cortex.ui.spin.start($('.CodeContainer')); // start a spinner overlay
  },

  formSubmitSuccess: function (data, status, jqXHR) {
    cortex.submittedData = false;
    cortex.submittedUrl = null;

    if (!cortex.followLocationHeader(jqXHR)) {
      if (cortex.destBeforeAuth) {
        //we were in an auth state, and the assumption is that if we
        // successfully submitted a form, then auth succeeded.
        var url = cortex.destBeforeAuth;
        cortex.destBeforeAuth = false;
        cortex.GET(url);
      } else if (data) {
        window.inForm = false;
        cortex.currResponse = {
          json: data,
          responseHeaders: jqXHR.getAllResponseHeaders(),
          status: jqXHR.status + " " + jqXHR.statusText
        };
        cortex.ui.showJSON(cortex.currResponse);
      }
    }
  },

  /**
   * Event handler for clicks on the clear buttons of the request headers panel.
   * Finds the closest text field, clears its value and triggers the change event for the field.
   */
  handleRequestHeaderClearClick: function (e) {
    e.preventDefault();

    var closestTextField = $(this).parents('.input-group').children(':text');

    // Empty the text field
    closestTextField.val('');

    // Manually trigger the change event to force refresh of the header value in localStorage
    closestTextField.trigger('change');

    closestTextField.focus();
  },

  handleJsonPathClick: function () {
    var path = $('#jsonpath-field').val().trim();
    var result = jsonPath(cortex.currResponse.json, path);
    var strResult = '<span class="path">' + path + '</span>' + (result
        ? '<pre>' + FormatJSON(result) + '</pre>'
        : '<p>No values found for path.</p>');
    var jpResult = $('#jsonpath-result');
    jpResult.html(strResult);
    jpResult.show();
  },

  handleEncodingUtilEncodeButton: function () {
    var code = $('#encodingutil-code-field').val().trim();
    var encoded = base32.encode(code);

    if (encoded.length < 26) {
      encoded += '=';
    }

    $('#encodingutil-encoded-field').val(encoded).select();
    return false;
  },

  handleEncodingUtilDecodeButton: function () {
    var encoded = $('#encodingutil-encoded-field').val();
    var decoded = base32.decode(encoded);
    $('#encodingutil-code-field').val(decoded).select();
    return false;
  },

  handleUriFieldKeydown: function (e) {
    // 13 - ENTER key
    if (e.which === 13) {
      e.preventDefault();
      $('#makeRequest').trigger('click');
    }
  },

  getZoomQueryStringParameterIndex: function(uri) {
    return uri.toLowerCase().indexOf("?zoom=");
  },

  /**
   * We split the uri by colons to allow for read-through links to avoid having to manually follow links in the browser
   * We handle zooms as the last segment of a chain of read-through links; any colons after the zoom is part of the zoomed response.
   */
  parseUriSegments: function(uri) {
    var segments;

    var zoomQueryStringIndex = cortex.ui.getZoomQueryStringParameterIndex(uri);
    if (zoomQueryStringIndex == -1) {
      segments = uri.split(':');
    } else {
      var uriBeforeZoom = uri.substring(0, zoomQueryStringIndex)
      segments = uriBeforeZoom.split(':')
      segments[segments.length - 1] = segments[segments.length - 1] + uri.substring(zoomQueryStringIndex)
    }

    return segments;
  },

  handleSampleUriButton: function (event) {
    event.preventDefault();
    var uri = $(event.target).data('actionlink');
    var lastResource = null;
    var resourceIndex = 1;

    var segments = cortex.ui.parseUriSegments(uri);

    if (segments.length == 1) {
      cortex.GET(cortex.serverPrefix + uri);
      return;
    }

    fetchResource();

    function fetchResource() {
      if (resourceIndex == segments.length) {
        cortex.GET(cortex.serverPrefix + lastResource.uri);
        return;
      }

      if (!lastResource)
        uri = segments[0];
      else
        uri = lastResource.uri;

      cortex._send('get', cortex.serverPrefix + uri, null, successCallback, cortex.handleFailure)
    }

    function successCallback(data) {
      var segment = segments[resourceIndex];

      var relToLookFor = segment;
      var zoomQueryStringIndex = cortex.ui.getZoomQueryStringParameterIndex(relToLookFor);
      if (zoomQueryStringIndex != -1) {
        relToLookFor = relToLookFor.substring(0, zoomQueryStringIndex);
      }

      lastResource = $.grep(data['links'], function (link) {
        return link.rel == relToLookFor;
      })[0];

      if (lastResource == undefined) {
        cortex.ui.alertMessage('Error', 'Resource link "' + segment + '" not found');
        return;
      }

      if (zoomQueryStringIndex != -1) {
        lastResource.uri = lastResource.uri + segment.substring(zoomQueryStringIndex);
      }

      resourceIndex++;
      fetchResource();
    }
  },

  handleSampleUriReset: function () {
    cortex.ui.ENTRY_TO_EDIT = null;

    $('#custom-start-point-key-field').val(null);
    $('#custom-start-point-url-field').val(null);

    $('#custom-start-point-add-button').val(cortex.ui.USER_ENTRY_BUTTON_ADD);
  },

  handleSampleUriEditButton: function (event) {
    event.preventDefault();

    var key = $(event.currentTarget).data('actionlink');

    if (!key) {
      return;
    }

    // First change the button to 'edit'
    $('#custom-start-point-add-button').val(cortex.ui.USER_ENTRY_BUTTON_EDIT);

    // Then find the entry to edit and populate the form fields
    var customEntries = JSON.parse(cortex.ui.loadFromStorage(cortex.ui.CUSTOM_ENTRIES_KEY));

    var entry = $.grep(customEntries, function (entry) {
      return entry.name == key;
    })[0];

    cortex.ui.ENTRY_TO_EDIT = entry;

    $('#custom-start-point-key-field').val(entry.name);
    $('#custom-start-point-url-field').val(entry.value);

    // Finally ensure the form is displayed
    cortex.ui.handleShowUserEntryForm()
  },

  handleSampleUriCopyButton: function (event) {
    event.preventDefault();

    var url = $(event.currentTarget).data('actionlink');

    if (!url) {
      return;
    }

    // First set the button to 'Add'
    $('#custom-start-point-add-button').val(cortex.ui.USER_ENTRY_BUTTON_ADD);

    // Copy the url across and clear the key field as may already be set
    $('#custom-start-point-key-field').val(null);
    $('#custom-start-point-url-field').val(url);

    // Finally ensure the form is displayed
    cortex.ui.handleShowUserEntryForm()
  },

  handleSampleUriRemoveButton: function (event) {
    event.preventDefault();

    var key = $(event.currentTarget).data('actionlink');

    if (!key) {
      return;
    }

    var customEntryNames = JSON.parse(cortex.ui.loadFromStorage(cortex.ui.CUSTOM_ENTRIES_KEY));

    $.each(customEntryNames, function (index) {
      if (this.name == key) {
        customEntryNames.splice(index, 1);
      }
    });

    cortex.ui.saveToStorage(cortex.ui.CUSTOM_ENTRIES_KEY, customEntryNames);
    cortex.ui.reloadSampleURIsPanel();
  },

  handleAddEntryPointButton: function (event) {
    var startPointKey = $('#custom-start-point-key-field').val();
    var startPointURL = $('#custom-start-point-url-field').val();

    var storedEntries = cortex.ui.loadFromStorage(cortex.ui.CUSTOM_ENTRIES_KEY);
    var customEntries = [];

    if (startPointKey && startPointURL) {

      if (storedEntries.length > 0) {
        customEntries = JSON.parse(storedEntries);
      }

      if (cortex.ui.ENTRY_TO_EDIT) {
        $.each(customEntries, function () {
          if (JSON.stringify(this) === JSON.stringify(cortex.ui.ENTRY_TO_EDIT)) {
            this.name = startPointKey;
            this.value = startPointURL;
          }
        })
      } else {
        customEntries.push({name: startPointKey, value: startPointURL});
      }

      cortex.ui.saveToStorage(cortex.ui.CUSTOM_ENTRIES_KEY, customEntries);
    }

    cortex.ui.handleToggleUserEntryForm();
    cortex.ui.reloadSampleURIsPanel();
  },

  handleToggleUserEntryForm: function (event) {
    // Toggle the Add/Hide label and toggle display of the form
    $('.toggle-new-entry-point').toggleClass('entry-point-form-opened');
    $('#add-entry-point-form').toggleClass('entry-point-form-opened');

    cortex.ui.handleSampleUriReset();
  },

  handleShowUserEntryForm: function () {
    $('#add-entry-point-form').addClass('entry-point-form-opened');
    $('.toggle-new-entry-point').addClass('entry-point-form-opened');
  },

  sourceViewEdited: function (editor) {
    //is the app trying to show JSON right now?
    if (cortex.ui.showingJSON) {
      return;
    }
    try {
      var src = editor.getValue();
      var json = JSON.parse(src);
      Process(json);
      cortex.ui.editorSyntaxError = false;
    }
    catch (e) {
      cortex.ui.editorSyntaxError = true;
    }
  },

  addFieldsToPutData: function (from, to, pathToField, value) {
    //find value at path in 'from' object
    var segments = pathToField.split('|');
    var currentObject = from;
    var currentValue;
    for (var pos = 0; pos < segments.length; pos++) {
      var segment = segments[pos];
      currentValue = currentObject[segment];
      var cloneObject;
      var cloneValue;
      var field;

      if (typeof currentValue === 'object' && !$.isArray(currentValue)) {
        currentObject = currentValue;
      } else if ($.isArray(currentValue)) {
        field = segments[pos];
        cloneObject = to;
        // for arrays, we want to add the whole array
        for (var cPos = 0; cPos < pos; cPos++) {
          cloneValue = cloneObject[field];
          if (cloneValue === undefined) {
            cloneValue = {};
          }
          cloneObject[field] = cloneValue;
          cloneObject = cloneObject[field];
        }
        cloneObject[segment] = currentValue;
      } else {
        //set the field on the property
        var sourceObject = from;
        cloneObject = to;
        for (var currPos = 0; currPos < segments.length; currPos++) {
          field = segments[currPos];
          var sourceValue = sourceObject[field];
          cloneValue = cloneObject[field];
          if (cloneValue === undefined && currPos + 1 < segments.length) {
            cloneValue = $.isArray(sourceValue) ? [] : {};
          } else if (currPos + 1 === segments.length) {
            //found the leaf point.
            cloneValue = value;
          }
          //set the value at that point in the object. It may be the same value.
          cloneObject[field] = cloneValue;
          //set up for the next iteration
          sourceObject = sourceObject[field];
          cloneObject = cloneObject[field];
        }
        //the value has been set in the to object
        return;
      }
    }
  },

  mergeObject: function (from, to) {
    for (var prop in from) {
      if (from.hasOwnProperty(prop)) {
        var fromValue = from[prop];
        var toValue = to[prop];
        if (fromValue && !toValue) {
          if (typeof fromValue === 'object') {
            if ($.isArray(fromValue)) {
              toValue = [];
              cortex.ui.mergeArray(fromValue, toValue);
            }
            else {
              toValue = {};
              cortex.ui.mergeObject(fromValue, toValue);
            }
            to[prop] = toValue;
          }
          else {
            to[prop] = fromValue;
          }
        }
      }
    }
  },

  mergeArray: function (from, to) {
    for (var pos = 0; pos < from.length; pos++) {
      var fromValue = from[pos];
      var toValue = to[pos];
      if (fromValue && !toValue) {
        if (typeof fromValue === 'object') {
          if ($.isArray(fromValue)) {
            toValue = [];
            cortex.ui.mergeArray(fromValue, toValue);
          }
          else {
            toValue = {};
            cortex.ui.mergeObject(fromValue, toValue);
          }
          to[pos] = toValue;
        }
        else {
          to[pos] = fromValue;
        }
      }
    }
  },

  /**
   * Analyzes a set of form fields and returns a boolean indicating whether they have each been populated
   * @param {jQuery} textFieldObj a jQuery object representing one or more input fields
   * @return {Boolean}
   */
  isFilled: function (textFieldObj) {
    var filled = true;
    textFieldObj.each(function () {
      if ($(this).val() === '') {
        filled = false;
        // Break out of the $.each loop
        return false;
      }
    });
    return filled;
  },

  /**
   * Loads a selection of start points as a datalist to the main URI field
   * @param {String} scope The Cortex scope to insert into the start points
   */
  // disabled as we use loadSampleURIsPanel
  loadRequestBarStartPoints: function (scope) {
    var startPointTemplateArr = [
      "/carts/{scope}/default",
      "/geographies/{scope}/countries",
      "/navigations/{scope}",
      "/profiles/{scope}/default",
      "/lookups/{scope}",
      "/registrations/{scope}/newaccount/form",
      "/searches/{scope}"
    ];

    var startPointArr = [];

    // Replace the placeholders in the start point array with the current scope
    for (var i = 0, j = startPointTemplateArr.length; i < j; i++) {
      startPointArr[i] = startPointTemplateArr[i].replace(/\{scope}/i, scope);
    }

    // initialize the bar with the first item from the starting point array
    if ($('#uriField').val() === "") {
      $('#uriField').val(startPointArr[0]);
    }

    // Append them as a new option to a <datalist>
    var $dataList = $('<datalist id="requestStartPointsList"></datalist>');
    for (var x = 0, y = startPointArr.length; x < y; x++) {
      var $newOption = $('<option>' + startPointArr[x] + '</option>');
      $newOption.attr('value', startPointArr[x]);
      $dataList.append($newOption);
    }
    // Add the new <datalist> to the DOM after the associated URI field
    $dataList.insertAfter($('#uriField'));
  },

  /**
   * Reloads the selection of start point uris based on what scope value has been set, with the HTTP header value taking precedence.
   */
  reloadSampleURIsPanel: function () {
    cortex.ui.loadSampleURIsPanel(cortex.scopesHeader || cortex.oAuthScope);
    cortex.ui.handleSampleUriReset();
  },

  /**
   * Loads a selection of start point uris into Sample URIs panel
   * @param {String} scope The Cortex scope to insert into the start points
   */
  loadSampleURIsPanel: function (scope) {
    cortex.ui.loadCustomSampleURIPanel(scope);
    cortex.ui.loadDefaultSampleURIsPanel(scope);
  },

  loadCustomSampleURIPanel: function (scope) {
    var $uriList = $('#CustomSampleUriList');
    $uriList.empty();

    var customEntries = cortex.ui.loadFromStorage(cortex.ui.CUSTOM_ENTRIES_KEY);
    var entries = [];

    if (customEntries.length > 0) {
      entries = JSON.parse(customEntries);
    }

    for (var i in entries) {
      var entry = entries[i];
      var $newListItem = $('<li class="sample-uri-list-item"></li>');
      var resolvedUri = entry.value.replace(/\{scope}/i, scope);
      $newListItem.append('<button class="sample-uri-btn btn-link" title="' + resolvedUri + '" data-actionlink="' + resolvedUri + '">' + entry.name + '</button>');
      $newListItem.append('<div class="sample-uri-btn-group">' +
        '<button class="btn-link sample-uri-copy-btn" data-actionlink="' + entry.value + '" title="Copy ' + entry.name + '"><span class="glyphicon glyphicon-link"></span> </button>' +
        '<button class="btn-link sample-uri-edit-btn" data-actionlink="' + entry.name + '" title="Edit ' + entry.name + '"><span class="glyphicon glyphicon-pencil"></span> </button>' +
        '<button class="btn-link sample-uri-remove-btn" data-actionlink="' + entry.name + '" title="Remove ' + entry.name + '"><span class="glyphicon glyphicon-remove"></span></button>' +
        '</div>');
      $uriList.append($newListItem);
    }
  },

  loadDefaultSampleURIsPanel: function (scope) {
    var startPointTemplates = {
      "Shopper's Current Order": "/carts/{scope}/default:order",
      "Shopper's Default Profile": "/profiles/{scope}/default",
      "Shopper's Default Cart": "/carts/{scope}/default",
      "Shopper's Default Wishlist": "/wishlists/{scope}/default",
      "Searches": "/searches/{scope}",
      "Lookups": "/lookups/{scope}",
      "Navigations": "/navigations/{scope}",
      "Geographies": "/geographies/{scope}/countries",
      "Account Registration": "/registrations/{scope}/newaccount/form"
    };

    var $uriList = $('#DefaultSampleUriList');
    $uriList.empty();

    // Replace the placeholders in the start point array with the current scope
    for (var uriProp in startPointTemplates) {
      var newUri = startPointTemplates[uriProp].replace(/\{scope}/i, scope);
      var $newListItem = $('<li class="sample-uri-list-item"></li>');
      $newListItem.append('<button class="sample-uri-btn btn-link" title="' + newUri + '" data-actionlink="' + newUri + '">' + uriProp + '</button>');
      $newListItem.append('<div class="sample-uri-btn-group">' +
        '<button class="btn-link sample-uri-copy-btn" data-actionlink="' + newUri + '" title="Copy ' + uriProp + '"><span class="glyphicon glyphicon-link"></span> </button>' +
        '</div>');
      $uriList.append($newListItem);
    }
  }
};

/**********
 * App setup
 **********/

/**
 * Setup 3rd party plugins
 */
$(document).ready(function () {
  //Source Editor
  cortex.ui.sourceEditor = new CodeMirror(document.getElementById('source-tab'),
    {
      indentUnit: 1,
      mode: {name: 'javascript', json: true},
      indentWithTabs: true,
      matchBrackets: true,
      theme: 'solarized dark',
      onChange: cortex.ui.sourceViewEdited,
      onCursorActivity: function () {
        cortex.ui.sourceEditor.matchHighlight("CodeMirror-matchhighlight");
      }
    });

  cortex.ui.errorJson = new CodeMirror(document.getElementById('msgModalBody'), {
    indentUnit: 1,
        indentWithTabs: true,
    mode: {name: 'javascript', json: true}
  });

  // Tabs
  $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
    if (e.target.hash === '#source-tab') {
      cortex.ui.sourceEditor.refresh();
    } else {
      if (cortex.ui.editorSyntaxError) {
        cortex.ui.processSourceView(cortex.response);
        cortex.ui.editorSyntaxError = false;
      }
    }
  });

  // Toggle raw JSON button
  $('#toggleSource').on('click', function (e) {
    var self = $(this);
    // Toggle the tab (via href) that the toggle button refers to
    if (self.attr('href') === '#source-tab') {
      self.attr('href', '#structure-tab');
    } else {
      self.attr('href', '#source-tab');
    }
    // Give the button a 'depressed' styling
    $(this).toggleClass('active');
  });

  // convert ugly select dropdown to button dropdown by bootstrap-select
  $(".selectpicker").selectpicker({
    width: '100px'
  });
  $('#oAuthRole').selectpicker({
    width: '100%'
  });

  cortex.zoom.initAutoComplete();
});

$(document).ready(function () {
  // populate cortex values from storage
  //---authentication values
  cortex.ui.loadField('oAuthScope');
  cortex.ui.loadField('oAuthRole');
  cortex.ui.loadField('oAuthUserName');

  //---header values
  cortex.ui.loadField('serverPrefix');
  cortex.ui.loadField('authHeader');
  cortex.ui.loadField('scopesHeader');
  cortex.ui.loadField('rolesHeader');
  cortex.ui.loadField('userIdHeader');
  cortex.ui.loadField('dataPolicySegmentsHeader');
  cortex.ui.loadField('userTraitsHeader');
  cortex.ui.loadField('forwardedBaseHeader');

  //Attach UI listeners
  $('#serverPrefix').on('change', cortex.ui.setServerPrefix);

  //----Request Header
  $('#http-headers-panel').on('click', '.btn-clear', cortex.ui.handleRequestHeaderClearClick);
  $('#authHeader').on('change', function () {
    cortex.ui.saveField('authHeader');
  });
  $('#scopesHeader').on('change', function () {
    cortex.ui.saveField('scopesHeader');
  });
  $('#rolesHeader').on('change', function () {
    cortex.ui.saveField('rolesHeader');
  });
  $('#userIdHeader').on('change', function () {
    cortex.ui.saveField('userIdHeader');
  });
  $('#dataPolicySegmentsHeader').on('change', function () {
      cortex.ui.saveField('dataPolicySegmentsHeader');
    });
  $('#userTraitsHeader').on('change', function () {
    cortex.ui.saveField('userTraitsHeader');
  });
  $('#forwardedBaseHeader').on('change', function () {
    cortex.ui.saveField('forwardedBaseHeader');
  });

  //----JSONPath Tester
  $('#jsonpath-button').on('click', cortex.ui.handleJsonPathClick);

  //----Encoding Util
  $('#encodingutil-encode-form').on('submit', cortex.ui.handleEncodingUtilEncodeButton);
  $('#encodingutil-decode-form').on('submit', cortex.ui.handleEncodingUtilDecodeButton);

  //----HTTP Methods
  $('#operationType').on('change', cortex.ui.handleOperationTypeChange);
  $('#makeRequest').on('click', cortex.ui.handleMakeRequestButton);
  $('#uriField').on('keydown', cortex.ui.handleUriFieldKeydown);
  $('#FormattedJson').on('click', '.Href', cortex.ui.handleHrefClick);
  $('#FormattedJson').on('click', '.submit-btn', cortex.ui.handleSubmitForm);

  //----Custom start points
  $('#add-entry-point-form').on('submit', cortex.ui.handleAddEntryPointButton);
  $('#sample-uri-panel').on('click', '.toggle-new-entry-point', cortex.ui.handleToggleUserEntryForm);
  $('#sample-uri-panel').on('click', '.sample-uri-btn', cortex.ui.handleSampleUriButton);
  $('#sample-uri-panel').on('click', '.sample-uri-remove-btn', cortex.ui.handleSampleUriRemoveButton);
  $('#sample-uri-panel').on('click', '.sample-uri-edit-btn', cortex.ui.handleSampleUriEditButton);
  $('#sample-uri-panel').on('click', '.sample-uri-copy-btn', cortex.ui.handleSampleUriCopyButton);

  //If input fields on a dropdown menu are clicked do not hide the menu
  $(document).on('click', '.dropdown input, .dropdown label, .dropdown textarea', function (e) {
    e.stopPropagation();
  });

  //History is how the GET calls are handled.
  $.history.init(cortex.ui.handleHistory, {unescape: '/:,=?'});

  // Load a default set of start points
  cortex.ui.reloadSampleURIsPanel();

  // Update the start points when the scope changes
  $('#oAuthScope, #scopesHeader').on('change', function () {
    cortex.ui.reloadSampleURIsPanel();
  });

  /*
   * Authentication Events
   */
  // Attach a keyup event handler to watch for changes to the auth form text fields
  var $oAuthFormInputs = $('#oAuthRole, #oAuthScope, #oAuthUserName, #oAuthPassword');
  $oAuthFormInputs.on('input change', cortex.authentication.toggleSubmitBtnAvailability);

  // Change handler for the auth role drop down - sets appropriate disabled properties on the form controls and buttons
  $('#oAuthRole').on('change', function () {
    cortex.authentication.toggleRegisteredAuthFieldsAvailability();
    cortex.authentication.clearUsernamePasswordFields();
  });

  $('.oAuth-logout').on('click', cortex.authentication.logout);
  $('#oAuthSubmit').on('click', cortex.authentication.login);


  // initialize
  cortex.authentication.render();

  // if not using header authentication and no token stored auto generate public token
  if (!cortex.userIdHeader && !cortex.authHeader) {
    cortex.oAuthRole = 'PUBLIC';
    $('#oAuthRole').selectpicker('val', cortex.oAuthRole);
    cortex.authentication.anonymousLogin();
  }
});

/**
 * control utilities panel accordion expand and collapse behavior
 */
$(document).ready(function () {
  var $hideLabel = $("<span>HIDE </span><span class='glyphicon glyphicon-chevron-right'></span>");
  var $utilsLabel = $("<span class='glyphicon glyphicon-cog'></span><span class='util-collapse-label'>UTILS</span>");
  var $queryPanel = $(".query-panels");
  var $utilsPanel = $('.util-panels');
  var $utilPanelWrapper = $('#utilDrawer');
  var $collapseToggle = $(".util-collapse-toggle");

  var queryPanelExpandCol = "col-md-11";
  var queryPanelCollapseCol = "col-md-8";
  var utilPanelExpandCol = "col-md-4";
  var utilPanelCollapseCol = "col-md-1";

  $collapseToggle.html($hideLabel);
  $collapseToggle.addClass("pull-right");

  $utilPanelWrapper.on('hidden.bs.collapse', function () {
    $queryPanel.removeClass(queryPanelCollapseCol).addClass(queryPanelExpandCol);
    $utilsPanel.removeClass(utilPanelExpandCol).addClass(utilPanelCollapseCol);

    $collapseToggle.html($utilsLabel);
    $collapseToggle.removeClass("pull-right").addClass("toggle-center");
  });


  $utilPanelWrapper.on('show.bs.collapse', function () {
    $queryPanel.removeClass(queryPanelExpandCol).addClass(queryPanelCollapseCol);
    $utilsPanel.removeClass(utilPanelCollapseCol).addClass(utilPanelExpandCol);

    $collapseToggle.html($hideLabel);
    $collapseToggle.addClass("pull-right").removeClass("toggle-center");
  });

  // prevent collapse events from propagating/bubbling up from panels to utils column
  $utilPanelWrapper.on("show.bs.collapse", ".util-panel-body", function (event) {
    event.stopPropagation();
  });
  $utilPanelWrapper.on("hidden.bs.collapse", ".util-panel-body", function (event) {
    event.stopPropagation();
  });
});
