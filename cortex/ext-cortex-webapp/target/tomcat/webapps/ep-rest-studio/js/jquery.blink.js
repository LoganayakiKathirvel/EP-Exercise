/*
 * jquery.blink.js
 * https://raw.github.com/fcarriedo/jquery-blink/master/lib/jquery.blink.js
 * 
 * Copyright (C) 2011 by Francisco J. Carriedo
 * Licensed under the MIT license.
 * https://github.com/fcarriedo/jquery-blink/blob/master/LICENSE
 */

/**
 * Basic usage (Most common settings. {no max blinks = blink indefinitely, blinkDuration ~1 sec, no callback}):
 *
 *      $('selector').blink();
 *
 * Advanced usage:
 *
 *      $('selector').blink({maxBlinks: 60, blinkPeriod: 1000, speed: 'slow', onMaxBlinks: function(){}, onBlink: function(){}});
 */

(function( $ ) {
	$.fn.blink = function( options ) {

		var settings = {
			maxBlinks    : undefined,
			blinkPeriod  : 1000,
			onMaxBlinks  : function() {},
			onBlink      : function(i) {},
			speed        : undefined
		};

		if(options) {
			$.extend(settings, options);
		}

		var blinkElem = this;
		var on = true;
		var blinkCount = 0;
		settings.speed = settings.speed ? settings.speed : settings.blinkPeriod/2;

		/* The function that does the actual fading. */
		(function toggleFade() {
			var maxBlinksReached = false;
			if(on){
				blinkElem.fadeTo(settings.speed, 0.01);
			} else {
				blinkCount++;
				maxBlinksReached = (settings.maxBlinks && (blinkCount >= settings.maxBlinks));
				blinkElem.fadeTo(settings.speed, 1, function() {
					settings.onBlink.call(blinkElem, blinkCount);
					if(maxBlinksReached) {
						settings.onMaxBlinks.call();
					}
				});
			}
			on = !on;

			if(!maxBlinksReached) {
				setTimeout(toggleFade, settings.blinkPeriod/2); // #3
			}
		})();

		return this; // Returning 'this' to maintain chainability.
	};
})(jQuery);