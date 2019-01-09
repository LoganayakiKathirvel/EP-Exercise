/*
 * This file contains code about authentication panel and its behaviours.
 *
 * Copyright © 2014 Elastic Path Software Inc. All rights reserved.
 */

window.cortex.authentication = (function () {
  var oAuthSubmit = $('#oAuthSubmit');
  var oAuthRole = $('#oAuthRole');
  var oAuthScope = $('#oAuthScope');
  var oAuthUserName = $('#oAuthUserName');
  var oAuthPassword = $('#oAuthPassword');

  var $greetingContainer = $('#OAuthGreetingContainer');
  var $feedbackContainer = $('#OAuthFeedbackContainer');
  var $feedbackLabel = $('.feedback-label');

  /**
   * renders the components of authentication panel into the right state based on oauth states.
   */
  var renderComponent = function () {
    clearAuthForm();
    renderGreetingLabel();

    // dynamically populate scope input field with saved oAuthScope value
    oAuthRole.selectpicker('val', cortex.oAuthRole);
    oAuthScope.val(cortex.oAuthScope);

    // toggle auth fields availability
    toggleSubmitBtnAvailability();
    toggleRegisteredAuthFieldsAvailability();
  };

  // clear relevant auth form fields
  function clearAuthForm() {
    oAuthUserName.val('');
    oAuthPassword.val('');
    // clear the feedback message
    $feedbackContainer.hide();
    $feedbackLabel.empty();
  }

  // displays authentication greeting message that tells user what kind of authentication he is under
  function renderGreetingLabel() {
    var $greetingLabel = $('.greeting-label');

    if (cortex.authHeader) {

      $greetingContainer.show(function () {
        var greetingHtml = [];
        var roleText = (cortex.oAuthRole === 'PUBLIC') ? 'public' : 'registered';

        greetingHtml.push('Authenticated as a <strong>' + roleText + '</strong> shopper');
        // if this is a registered user, include their username in the greeting
        if (cortex.oAuthRole === 'REGISTERED') {
          greetingHtml.push(': <strong>' + cortex.oAuthUserName + '</strong>');
        }
        greetingHtml.push(' with scope <strong>' + cortex.oAuthScope + '</strong>.');

        $greetingLabel.html(greetingHtml.join(''));
      });
    }
    else {
      $greetingLabel.empty();
      $greetingContainer.hide();
    }
  }

  /**
   * Empties the username and password fields if the user has chosen PUBLIC auth role
   */
  var clearUsernamePasswordFields = function() {
    if (oAuthRole.val() === 'PUBLIC') {
      oAuthUserName.val('');
      oAuthPassword.val('');
    }
  };

  /**
   * Empties the x-ep-user-* fields. Triggered if oAuth token requested.
   */
  var clearUserHeaderFields = function () {
    cortex.ui.removeField('userIdHeader');
    cortex.ui.removeField('dataPolicySegmentsHeader');
    cortex.ui.removeField('rolesHeader');
    cortex.ui.removeField('scopesHeader');
    cortex.ui.reloadSampleURIsPanel();
  };

  /**
   * toggle enable/disable state of the username & password field
   */
  var toggleRegisteredAuthFieldsAvailability = function() {
    if (oAuthRole.val() === 'PUBLIC') {
      // Disable the username and password fields
      oAuthUserName.prop('disabled', true);
      oAuthPassword.prop('disabled', true);
    } else {
      // Enable the username and password fields
      oAuthUserName.prop('disabled', false);
      oAuthPassword.prop('disabled', false);
    }
  };

  /**
   * toggle enable/disable state of the submit button depending what
   *  oAuthRole is selected and if all required fields are filled
   */
  var toggleSubmitBtnAvailability = function() {
    // true: disable submit button; false: enable
    var disable = false;
    var checkList = [
      oAuthRole,
      oAuthScope
    ];

    // if role is REGISTERED, check username & password field too
    if (oAuthRole.val() === 'REGISTERED') {
      checkList.push(oAuthUserName);
      checkList.push(oAuthPassword);
    }

    // check if any fields in checkList is empty,
    // if empty, set disable to true
    var indx = 0;
    while (!disable && indx < checkList.length) {
      if (!cortex.ui.isFilled(checkList[indx])) {
        disable = true;
      }
      indx++;
    }

    oAuthSubmit.prop('disabled', disable);
  };

  /**
   * Authenticate according to values from oAuth form.
   */
  var authFromForm = function(event) {
      if (event) {
        event.preventDefault();
      }

      var role = oAuthRole.val();
      var scope = oAuthScope.val();
      var username = oAuthUserName.val();
      var password = oAuthPassword.val();

     auth(role, scope, username, password, true);
  }

  var authPublic = function(autoRetry) {
    var role = 'PUBLIC';
    var scope = cortex.oAuthScope;

    auth(role, scope, '', '', autoRetry);
  }

  /**
   * Authenticate, save the oAuth values into app on success, and re-render the auth panel components
   *
   * submit button click - disable default button action in this case.
   */
  function auth(role, scope, username, password, autoRetry) {

    // start a spinner overlay, and on success or error stop
    cortex.ui.spin.start($('#oauth-panel'));

    authInternal(role, scope, username, password, autoRetry, new Date().getTime());
  }

  function authInternal(role, scope, username, password, autoRetry, firstAttemptTime) {
    autoRetry = typeof autoRetry !== 'undefined' ? autoRetry : false;
    $.ajax({
      type: 'POST',
      url: cortex.serverPrefix + '/oauth2/tokens',
      contentType: 'application/x-www-form-urlencoded; charset=utf-8',
      data: {
        'grant_type': 'password',
        'role':     role,
        'scope':    scope,
        'username':   username,
        'password': password
      },
      headers: {
        "Authorization": cortex.authHeader
      },
      success: function (json, responseStatus, xhr) {
        // save authentication fields for later reference
        $('#authHeader').val(json.token_type + " " + json.access_token);
        cortex.ui.saveField('authHeader');
        cortex.ui.saveField('oAuthScope');
        cortex.ui.saveField('oAuthRole');
        cortex.ui.saveField('oAuthUserName');

        // We're using oAuth now so clear the user header fields as not being used for authentication
        // In particular this is required in order to update sample links to use the oAuth scope and not the HTTP header scope
        clearUserHeaderFields();

        renderComponent();  // re-render auth components
        cortex.ui.loadSampleURIsPanel(oAuthScope.val()); // Refresh the start points
        cortex.ui.spin.stop();  // stops the spinner indicator
      },
      error: function (jqXHR) {
        // show the feedback container (empty, waiting for message to be injected)
        $feedbackContainer.show();

        // displays authentication error
        function printErrToFeedbackRegion() {
          $feedbackLabel.html(jqXHR.responseText);
          cortex.ui.spin.stop();  // stops the spinner indicator
        }

        function retryAuth() {
          var totalTimeSpent = Math.round((new Date().getTime() - firstAttemptTime) / 1000)
          if (!autoRetry || totalTimeSpent > 5*60) {
            cortex.processServerError(jqXHR);
            $feedbackLabel.html(jqXHR.responseText);
            cortex.ui.spin.stop();  // stops the spinner indicator
          } else {
            setTimeout(function () {
              $feedbackLabel.html("<div style='color: white'><b>Cortex server starting.  Please wait..." + totalTimeSpent + "</b></div>")
              authInternal(role, scope, username, password, autoRetry, firstAttemptTime);
            }, 500);
          }
        }

        // override some of the default error handling function
        var errorHandlers = jQuery.extend({}, cortex.getDefaultErrorHandlers());
        errorHandlers = jQuery.extend(errorHandlers, {
          401: printErrToFeedbackRegion,
          400: printErrToFeedbackRegion,
          415: printErrToFeedbackRegion,
          503: retryAuth,
        });
        jqXHR.statusCode(errorHandlers);
      }
    });
  };


  /**
   * Revoke authenticate (logout), removes the stored oAuth values, reset
   * required fields to default value, and re-render the auth panel
   *
   * @param event (optional) passed in if auth request come from
   * submit button click - disable default button action in this case.
   */
  var revokeAuth = function (event) {
    if (event) {
      event.preventDefault();
    }

    // start a spinner overlay, and on success or error stop
    cortex.ui.spin.start($('#oauth-panel'));

    cortex.DELETE(cortex.serverPrefix + '/oauth2/tokens',
      function (data, responseStatus, xhr) {
        // removes all oauth field values
        cortex.ui.removeField('authHeader');
        cortex.ui.removeField('oAuthScope');
        cortex.ui.removeField('oAuthRole');
        cortex.ui.removeField('oAuthUserName');

        // Reset scope and role value
        cortex.oAuthRole = cortex.DEFAULT_ROLE;
        cortex.oAuthScope = cortex.DEFAULT_SCOPE;

        renderComponent();
      },
      function (jqXHR) {
        // show the feedback container (empty, waiting for message to be injected)
        jqXHR.statusCode(cortex.getDefaultErrorHandlers());
      });
  };

  return {
    anonymousLogin: authPublic,
    login: authFromForm,
    logout: revokeAuth,
    render: renderComponent,
    clearUsernamePasswordFields: clearUsernamePasswordFields,
    toggleRegisteredAuthFieldsAvailability: toggleRegisteredAuthFieldsAvailability,
    toggleSubmitBtnAvailability: toggleSubmitBtnAvailability
  };
})();
