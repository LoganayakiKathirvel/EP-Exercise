window.cortex.zoom = (function () {
  // cache jquery selectors
  var $zoomField = $('#zoomField');
  var $zoomStatsCheckbox = $('#zoomStats');


  // --------------
  var init = function () {
    $zoomField
      // NOTE: keydown binding must be above autocomplete binding
      // for handleTabEnterKeyPressed to work.
      // so we can test if menu item selected before autocomplete menu close
      .on("keydown", function (e) {
        switch (e.keyCode) {
          case 186: // colon key pressed
            if (e.shiftKey) {
              updateSrcWithRemoteData($(this).val());
            }
            break;
          case 188: // comma key pressed
            updateSrcWithLocalData();
            break;
          default:
            handleTabEnterKeyPressed(e);
        }
      })
      .autocomplete({
        minLength: 0,
        autoFocus: true,
        source: [],
        focus: function () {
          // prevent value inserted on focus
          return false;
        },
        select: function (event, ui) {
          // find the last term's position in input value, cut if off and replace
          // with selected auto-complete value
          var pop = split(this.value).pop() || '';
          var cutOffIndx = this.value.toLowerCase().lastIndexOf(pop.toLowerCase());
          if (cutOffIndx !== -1) {
            this.value = this.value.substring(0, cutOffIndx) + ui.item.value;
          }
          return false;
        }
      })
      .on("focusin", function (e) {
        // open the auto-complete menu on focus
        $(this).autocomplete("search", "");
      })
      .on("keyup", function (e) {
        // when backspace is pressed, input changes drastically and suggestion
        // might be inaccurate, so test if last delimiter is a colon or comma,
        // and update the source accordingly
        if (e.keyCode === 8) { // backspace key pressed
          var input = $zoomField.val();
          var commaIndx = input.lastIndexOf(',');
          var colonIndx = input.lastIndexOf(':');

          if (colonIndx > commaIndx) {
            var scope = input.substring(0, colonIndx);
            updateSrcWithRemoteData(scope);
          }
          else {
            updateSrcWithLocalData();
          }
        }
      });
  };

  function split(val) {
    return val.split(/[,:]\s*/);
  }

  function extractLast(term) {
    return split(term).pop();
  }

  /**
   * adjust default TAB & ENTER key behaviour, so pressing these keys
   * while an option is selected will not navigate away or submit request
   */
  function handleTabEnterKeyPressed(e) {
    var menuActive = $zoomField.autocomplete("instance").menu.active;

    // keep TAB key from navigate away when menu item is selected
    if (e.keyCode === $.ui.keyCode.TAB && menuActive) {
      e.preventDefault();
    }
    // keep ENTER key from making request when menu item is selected
    else if (e.keyCode === $.ui.keyCode.ENTER && !menuActive) {
      cortex.ui.handleMakeRequestButton();
    }
  }


  // --------------
  function updateSrcWithRemoteData(input) {
    // get chunk of input before last comma (,)
    var scope = input.split(/[,]\s*/).pop();
    var uri = cortex.ui.getUriField();
    if (!(uri && scope)) {
      return;
    }
    uri = cortex.serverPrefix + uri + '?zoom=' + scope;

    $zoomField.autocomplete('option', 'source', function (request, response) {
      function successFn(data) {
        var json = extractDescendant(data.json, scope);
        var newSuggestions = getZoomItems(json);

        response($.ui.autocomplete.filter(
          newSuggestions, extractLast(request.term)));

        // update autocomplete source function so subsequent queries fetch data
        // locally until next remote call is requested
        $zoomField.autocomplete('option', 'source', updateSrc(newSuggestions));
      }

      function errorFn(jqXHR, textStatus, error) {
        cortex.handleFailure(jqXHR);

        // in case of failure, update autocomplete source function to local version
        // for subsequent queries, but provide no suggestions
        $zoomField.autocomplete('option', 'source', updateSrc([]));
      }

      cortex.GET_history(uri, successFn, errorFn);
    });
  }

  var updateSrcWithLocalData = function () {
    var response = {};
    if (cortex.currResponse && cortex.currResponse.json) {
      response = cortex.currResponse.json;
    }
    var newSuggestions = getZoomItems(response);

    $zoomField.autocomplete('option', 'source', updateSrc(newSuggestions));
  };

  function updateSrc(newData) {
    return function (request, response) {
      // delegate back to autocomplete, but extract the last term
      response($.ui.autocomplete.filter(
        newData, extractLast(request.term)));
    };
  }

  function extractDescendant(response, path) {
    var childPrefix = '_';
    var paths = path.split(':');
    var parent = response;

    while (paths.length > 1) {
      var currLvl = childPrefix + paths.shift();
      parent = parent[currLvl];
      if (parent) {
        parent = parent[0];
      }
      else {
        break;
      }
    }

    var descendant;
    if (parent) {
      descendant = parent[childPrefix + paths.shift()];
    }

    return descendant;
  }

  /**
   * crawl through given json for possible zoom queries,
   * @param json json object to crawl
   */
  function getZoomItems(json) {
    var zoomList = [];

    if (Array.isArray(json)) {
      zoomList = searchArrayForZoom(json);
    }
    else if (typeof json === "object") {
      zoomList = searchObjectForZoom(zoomList, json);
    }

    return zoomList;
  }

  function searchArrayForZoom(arr) {
    var zoomList = [];

    for (var i = 0, len = arr.length; i < len; i++) {
      searchObjectForZoom(zoomList, arr[i]);
    }

    return zoomList;
  }

  /**
   * search given object for links element,
   * and pull out rel elements from it
   */
  function searchObjectForZoom(zoomList, obj) {
    var searchItems = obj.links;
    if (!searchItems) {
      return zoomList;
    }

    searchItems.forEach(function (item) {
      var rel = jsonPath(item, 'rel')[0] || '';
      if (rel && isRelValidAndUnique(zoomList, rel)) {
        zoomList.push(rel);
      }
    });

    return zoomList;
  }

  /**
   * check if rel is valid and unique
   */
  function isRelValidAndUnique(list, candidate) {
    list = list || [];
    return list.indexOf(candidate) === -1
      && candidate.indexOf('action') === -1;
  }


  // --------------
  var getZoomQuery = function () {
    var zoom = $zoomField.val().trim();
    if (zoom) {
      zoom = '?zoom=' + zoom;
      if ($zoomStatsCheckbox.prop('checked')) {
       zoom = zoom + '&zstats';
      }
    }
    return zoom;
  };

  return {
    initAutoComplete: init,
    getZoomQuery: getZoomQuery,
    populateAutoComplete: updateSrcWithLocalData
  };
})();