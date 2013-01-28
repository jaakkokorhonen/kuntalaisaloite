
var localization, generateModal, loadChosen, validateForm, modalContent, modalType;

/**
 * 
 * Localization
 * ==============
 * - Returns localized texts for JavaScript-elements.
 * 
 * */
localization = {
	
	chosenNoResults:function(locale){
    	if (locale == 'sv'){
    		return "Inga träffar"
    	} else {
    		return "Ei tuloksia"
    	}
	},
	getSubmitInfo:function(locale){
    	if (locale == 'sv'){
    		return "Det verkar räcka längre än väntat att utföra funktionen. Vänligen vänta en stund."
    	} else {
    		return "Toiminnon suorittaminen näyttäisi kestävän odotettua kauemmin. Ole hyvä ja odota hetki."
    	}
	},
	getSubmitWarning:function(locale){
    	if (locale == 'sv'){
    		return "Det gick inte att utföra funktionen. Vänligen försök på nytt om några minuter."
    	} else {
    		return "Toimintoa ei voitu suorittaa. Ole hyvä ja yritä uudelleen muutaman minuutin kuluttua."
    	}
	}
};

/**
 * 
 * Generate modal
 * ==============
 * - Gets modalContent containing all HTML
 * - Uses jsRender to render template into the modal container
 * - Shows modal template with the defined content
 * 
 * */
generateModal = function (modalContent, modalType) {
	$("#modal-container").html($("#modal-template").render(modalContent));
	$(".modal").loadModal(modalType);
	return false;
};

/**
 * 
 * Load chosen
 * ===========
 * Little helper for loading chosen
 * 
 * - Chosen.js requires that first option is empty. We empty the default value which is for NOSCRIPT-users.
 * - Initialize chosen with localized text
 * 
 * */
jQuery.fn.loadChosen = function(){
	var self = $(this);
	
	self.find('option:first').text('');
	self.chosen({no_results_text: localization.chosenNoResults(Init.getLocale())});
};


$(document).ready(function () {	
	// Define general variables
	var $body, speedFast, speedSlow, speedVeryFast, speedAutoHide, vpHeight, vpWidth, validateEmail, isIE7, isIE8, locale;
	$body = $('body');
	speedFast = '200';					// General speeds for animations
	speedVeryFast = '10';			 
	speedSlow = 'slow';		
	speedAutoHide = '15000';			// Delay for hiding success-messages (if enabled)
	vpHeight = $(window).height();		// Viewport height
	vpWidth =  $(window).width();		// Viewport width
	isIE7 = $('html').hasClass('ie7');	// Boolean for IE7. Used browser detection instead of jQuery.support().
	isIE8 = $('html').hasClass('ie8');	// Boolean for IE8. Used browser detection instead of jQuery.support().
	locale = Init.getLocale();			// Current locale: fi, sv
	
/**
 * Common helpers
 * ==============
 */
	// Wait a while and hide removable message
	if ( $('.auto-hide').length > 0 ){
		setTimeout(function() {
			$('.auto-hide').fadeOut(speedSlow);
		}, speedAutoHide);
	}

	// Validate emails
	validateEmail = function (email) {
	    var re;
	    re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
	    return re.test(email);
	};
	
	// Switch content between element's HTML and data-alttext -attribute
	// When elem-argument is used, function switches content inside elem.
	jQuery.fn.switchContent = function(elem){
		var switcher, temp;
		
    	if ( elem == null){
    		elem = false;
    	}
    	
    	switcher = $(this);
    	temp = switcher.data('alttext');
		
    	if (elem){
    		switcher.data('alttext', elem.html());
    		elem.html(temp);
    	} else {
    		switcher.data('alttext', switcher.html());
    		switcher.html(temp);
    	}
	};

	/**
	 *	Prevent double clicks
	 *
	 *	TODO:	Finalize after function approved. 
	 *			What happens when JS validation is implemented?
	 * 
	 */
	$("button").live('click', function () {
		var btnClicked, firstBtnInForm, $loader, $loader, $submitInfo, $submitWarning;
		btnClicked = $(this);
		
		// Disable in some cases
		if (!btnClicked.hasClass('disable-dbl-click-check')){
			// We may have more than one submit button. For example in create.
			firstBtnInForm = btnClicked.parents('form').find('button:first');
			siblingButtons = btnClicked.siblings('.small-button, .large-button');
			$loader = $('<span class="loader" />');
			$submitInfo = $('<div class="system-msg msg-info">'+localization.getSubmitInfo(locale)+'</div>');
			$submitWarning = $('<div id="submit-warning" class="system-msg msg-warning">'+localization.getSubmitWarning(locale)+'</div>');
			
			$("#submit-warning").remove(); // Clear warning
			
			btnClicked.css('position','relative'); // Set button relative for loader animation position
			
			// Separate classes for styling and function, so that would not mess up classes and functions.
	        if (!btnClicked.hasClass("clicked")){
	        	btnClicked.addClass("disabled clicked");
	        	siblingButtons.addClass("disabled clicked");
	        	siblingButtons.click(function(){
	        		return false;
	        	});
	        	btnClicked.append($loader);
	        	setTimeout(function() {
	        		firstBtnInForm.before($submitInfo);
	    	   	}, 5000);
	            setTimeout(function() {
	            	btnClicked.removeClass("disabled clicked");
	            	siblingButtons.removeClass("disabled clicked");
	            	$loader.remove();
	            	$submitInfo.remove();
	            	firstBtnInForm.before($submitWarning);
	    	   	}, 30000);
	        } else {
	            return false;
	        }
		}
    });
	
	// Console fix for IE
	if (typeof console == "undefined") {
	    console = { log: function() {} };
	}
	
	// Remove system message
	$('.close-msg').click(function(){
		var parent = $(this).closest('div').fadeOut('speedSlow', function(){
			parent.remove();
		}); 
	});
	
	// Action for external links
	$('a[rel=external]').click(function(){
		window.open( $(this).attr('href') );
		return false;
	});
	
	// TOP ribbon
	var $topRibbon = $('.debug-ribbon.top.fixed');
	$topRibbon
	.after('<div style="height:'+$topRibbon.outerHeight()+'px" />')
	.css('position','fixed');

	
/**
 *	Toggle dropdown menus
 *  =====================
 * */
	var $dToggle, $dMenu, clickedVisible;
	$dToggle = $('.dropdown-toggle'); 
	$dMenu = $('.dropdown-menu');
	
	$dToggle.click(function (e) {
		if( !$(this).next($dMenu).is(':visible') ){
			clickedVisible = true;
		} else {
			clickedVisible = false;
		}
		
		clearMenus();
		
		if( clickedVisible ){
			$(this)
			.addClass('active')
			.next($dMenu).show();
		} 		
		return false;
	});
	// Off clicking closes menus
	$('body').on('click', function(e){
		if( $dMenu.is(':visible') ){
			clearMenus();
        }
	});
	var clearMenus = function(){
		$dMenu.hide();
		$dToggle.removeClass('active');
	};
	
/**
 * 
 * Change the font-size
 * ====================
 * - Uses jQuery Cookie: https://github.com/carhartl/jquery-cookie
 * - Toggles a class for body-element (font-size-small, font-size-medium, font-size-large)
 * - Sets and gets fontSizeClass-cookie
 * 
 * */
	var $fontSizeToggler, fontSizeClass;
	
	$fontSizeToggler = $('.font-size-toggle a');
	fontSizeClass = "font-size-medium";
	
	// Get cookie value for the fontSize
	if( $.cookie("fontSizeClass") != null ){
		fontSizeClass = $.cookie("fontSizeClass");
	}
	
	$('.font-size-toggle .'+fontSizeClass).addClass('active');
	$body.addClass(fontSizeClass);
	
	$fontSizeToggler.click(function(){
		var thisItem = $(this);
		
		$fontSizeToggler.find('span').removeClass('active');
		thisItem.find('span').addClass('active');
		
		if ( thisItem.hasClass('font-size-small-link') ){
			fontSizeClass= "font-size-small";
		} else if ( thisItem.hasClass('font-size-large-link') ){
			fontSizeClass= "font-size-large";
		} else {
			fontSizeClass= "font-size-medium";
		}
		
		$body.removeClass("font-size-small font-size-medium font-size-large").addClass(fontSizeClass);
		
		// Set current fontSize as a cookie value.
		$.cookie("fontSizeClass", fontSizeClass, { expires: 30, path: '/' });
		
		return false;
	});
	
/**
 * 
 * Expand and minify form blocks
 * =============================
 * 
 * TODO: Improve animation vs. scrolling
 * 
 * */
	var validationErrors, showFormBlock, $formHeader;
	
	// If form has validation errors: true / false
	validationErrors = $('#form-initiative').hasClass('has-errors');
	
	// Show this block, hide others
 	showFormBlock = function(blockHeader, scrollId){
 		var thisHeader, thisBlock, otherHeaders, otherBlocks;

		thisHeader = blockHeader;
 		thisBlock = thisHeader.next('.input-block');
 		otherHeaders = thisHeader.parent().siblings().children('.content-block-header');
 		otherBlocks = thisHeader.parent().siblings().children('.input-block');
 		
 		otherBlocks.stop(false,true).slideUp({
			duration: speedFast, 
			easing: 'easeOutExpo'
		});
		otherHeaders.removeClass('open');
 		
 		thisBlock.stop(false,true).slideToggle('fast', function() {
 			$.scrollTo( "#"+scrollId , 800, {easing:'easeOutExpo'});
 		  });
 		
		thisHeader.toggleClass('open');
 	};

 	$formHeader = $('.content-block-header');

 	$formHeader.click(function(){
 		var thisClicker = $(this);
 		
 		if ( !validationErrors && !thisClicker.hasClass('disabled')){
 			showFormBlock(thisClicker, thisClicker.attr('id'));
 		}
 	});

 	// Action for wizard's continue button
 	proceedTo = function(step){
 		var headerNextStep = "#step-header-"+step;
 		
 		if ( !validationErrors ){
	 		var blockHeader = $('#step-'+step).prev('.content-block-header');
			showFormBlock(blockHeader, headerNextStep);
 		} else {
 			$.scrollTo( headerNextStep , 800, {easing:'easeOutExpo'});
 		}

 		return false;
 	};
 	
 	// In case of validation error
 	if ( validationErrors ){
 		$('.input-block').show();
 	}
 	$('#errors-summary a').click(function(){
 		var errorAnchor = $(this);
 		
 		$.scrollTo( errorAnchor.attr('href') , 800, {easing:'easeOutExpo'});
 		
 		return false;
 	});


/**
* Municipality selection
* ======================
* 
* - Initializes Chosen select
* - Updates home municipality automatically
* - Toggles home municipality membership radiobuttons
* - Prevents or allows proceeding to next step in the form
* 
* TODO: Finalize this block when UX is done.
*/	
	var chznSelect, municipalitySelect, homeMunicipalitySelect, selectedMunicipality, municipalityDiffers, municipalMembershipRadios,
	isHomeMunicipality, equalMunicipalitys, slideOptions;

	chznSelect = 				 $(".chzn-select");
	municipalitySelect =		 $('#municipality');
	homeMunicipalitySelect =	 $('#homeMunicipality');
	selectedMunicipalityElem =	 $('#selected-municipality'); // Municipality text in the second step in the form
	municipalityDiffers =		 $('.municipalitys-differs');
	municipalMembershipRadios =  $("input[name=municipalMembership]");
	// Checks which one of the selects we are using
	isHomeMunicipality = function(select){
		if (select.attr('id') == homeMunicipalitySelect.attr('id') ) {
			return true;
		} else {
			return false;
		}
	};
	equalMunicipalitys = function(){
		//if (municipalitySelect.val() == homeMunicipalitySelect.val()) {
		// TODO: Use variables in selectors. Issue: They need to updated when modal is loaded.
		//if ( $('#homeMunicipality').data("init-municipality") == "" ||  $('#municipality').data("init-municipality") == $('#homeMunicipality').val() ) {
		if ( $('#municipality').val() == $('#homeMunicipality').val() ) {
			return true;
		} else {
			return false;
		}
	};
	slideOptions = {
		duration: speedFast, 
		easing: 'easeOutExpo'
	};
	
	$(".chzn-select").loadChosen();
	
	// update text in the municipality data in the form step 2
	var updateSelectedMunicipality = function(){
		var selectedMunicipality = municipalitySelect.find('option:selected').text();
		if (selectedMunicipality != "") {
			selectedMunicipalityElem.text(selectedMunicipality);
		}
	};
	
	updateSelectedMunicipality();
	
 	// Update home municipality automatically
	var updateHomeMunicipality = function(select){
		var selectedMunicipalityId, selectedMunicipalityName;
		
		selectedMunicipalityId = select.val();
		selectedMunicipalityName = select.find('option:selected').text();

		updateSelectedMunicipality();

		// if user has changed the homeMunicipality value we will not mess it up
		if ( !homeMunicipalitySelect.hasClass('updated') ){			
			homeMunicipalitySelect
			.val(selectedMunicipalityId)
			.trigger("liszt:updated"); // updates dynamically the second chosen element
		}
	};
	
	// Disable or enable the next button and clicking the other form block
	// TODO: Make more dynamic
	var preventContinuing = function(prevent){
		var formBlockHeaders = $("#step-header-2, #step-header-3, #step-header-4");
		var formBlocks = $("#step-2, #step-3, #step-4");
		
		//$("#button-next-2").disableBtn(prevent);
		
		if (prevent) {
			$("#button-next-2").addClass('disabled').attr('onClick','return false;');
			$("#submit-participate").addClass('disabled').attr('disabled','disabled');
			formBlockHeaders.addClass('disabled');
			
			if (validationErrors){
				formBlocks.hide();
			}
			
		} else {
			$("#button-next-2").removeClass('disabled').attr('onClick','proceedTo(2); return false;');
			$("#submit-participate").removeClass('disabled').removeAttr('disabled');
			formBlockHeaders.removeClass('disabled');
			
			if (validationErrors){
				formBlocks.show();
			}
		}
	};
	
	function disableSaveAndCollect(disable){
		var btnSaveAndSend = $('button[name=action-save]');
		
		if (disable) {
			btnSaveAndSend.addClass('disabled').attr('disabled','disabled');
		} else {
			btnSaveAndSend.removeClass('disabled').removeAttr('disabled');
		}
	}
	
	// Show or hide the radiobutton selection for municipality membership
	var toggleMembershipRadios = function(select){
		if( equalMunicipalitys() ){
			municipalityDiffers.stop(false,true).slideUp(slideOptions);
			preventContinuing(false);
			
			// Clear radiobuttons
			//municipalMembershipRadios.removeAttr('checked');
			// TODO: Use variables in selectors. Issue: They need to updated when modal is loaded.
			
			$("input[name=municipalMembership]").removeAttr('checked');
			
			disableSaveAndCollect(true);
			
			$('#franchise').removeClass('js-hide'); // TODO: finalize
			$('#municipalMembership').addClass('js-hide'); // TODO: finalize
			
		} else {
			municipalityDiffers.stop(false,true).slideDown(slideOptions);
			preventContinuing(true);
			
			$('button[name=action-save]').removeClass('disabled');
			
			disableSaveAndCollect(false);
			
			if (validationErrors){
				$("input[name=municipalMembership]").removeAttr('checked');
			}
			
			$('#franchise').addClass('js-hide'); // TODO: finalize
			$('#municipalMembership').removeClass('js-hide'); // TODO: finalize
		}
	};
	if (validationErrors){
		toggleMembershipRadios(homeMunicipalitySelect);
		$('input[name=franchise]').removeAttr('checked');
	}
	
	$('input[name=franchise]').click(function(){
		var isFranchise = ( $(this).attr('value') == 'true' );
		
		disableSaveAndCollect(!isFranchise);
	});
	
	// Disable button
	// FIXME: Has issues with revolving values
	/*jQuery.fn.disableBtn = function(disable){
		var defaultVal = 'return false;';
		
		if (disable && ($(this).attr('onClick') != defaultVal)) {
			$(this)
			.addClass('disabled')
			.data('onClickTmp',$(this).attr('onClick'))
			.attr('onClick',defaultVal);
		} else {
			$(this)
			.removeClass('disabled')
			.data($(this).attr('onClick'),'onClickTmp');
		}
		
		console.log($(this).data('onClickTmp')+" | "+$(this).attr('onClick'));
	};*/
	
	// Assure membership for the municipality
	jQuery.fn.assureMembership = function(){
		var cb, btn, cbVal;
		
		cb = $(this);
		btn = $('#button-next-2, #submit-participate');
		cbVal = function(){
			if ($("input[name=municipalMembership]:checked").val() == "true"){
				btn.removeAttr('disabled').removeClass('disabled');
				preventContinuing(false);
			} else {
				btn.attr('disabled','disabled').addClass('disabled');
				preventContinuing(true);
			}
		};
		
		// Use live as this is fired also in the modal
		cb.live('change',function(){
			cbVal();
		});
	};
	municipalMembershipRadios.assureMembership();
	
	// Listen municipality selects
	$('.municipality-select').live('change', function() {
		var thisSelect = $(this);
		
		// Update home municipality automatically
		if (!isHomeMunicipality(thisSelect)){
			updateHomeMunicipality(thisSelect);
			homeMunicipalitySelect.data("init-municipality",municipalitySelect.val());
		} else {
			homeMunicipalitySelect.addClass("updated");
		}
		
		// Toggle membership radiobuttons
		toggleMembershipRadios(thisSelect);
		
		// Disable / enable proceeding to the next form steps
		if ( $("input[name=municipalMembership]:checked").length == 0){
			preventContinuing(!equalMunicipalitys());
		} else {
			municipalMembershipRadios.removeAttr('checked');
		}
	});
	
	
/**
* Search form
* ===========
* 
*/

//Listen search form select
$('.municipality-filter').live('change', function() {
	var thisSelect = $(this);
	
	// Set a small delay so that focus is correctly fired after chance-event.
	setTimeout(function () { $('#search').focus(); }, 50);
});	
	
	

/**
* Toggle collect people
* ====================
* - Toggles an element depending on the selection of other element (radiobutton or checkbox)
* - If the input is clicked hidden:
* 		* the input is disabled so that value will not be saved
* 		* the value is not removed so that the value can be retrieved
* 		  when clicked back to visible 
* - TODO: Bit HardCoded now. Make more generic if needed.
* 			WE MIGHT NOT NEED THIS ANYMORE as secret edit-url is generated after form submit.
*/
/*
var toggleArea, $toggleAreaLabel, radioTrue, $toggleField, toggleBlock;

toggleArea =		'.gather-people-details';
$toggleAreaLabel =	$('#gather-people-container label');
radioTrue =		'gatherPeople.true';
$toggleField =		$('#initiativeSecret');

toggleBlock = function(clicker, input){
	if( input.attr('id') == radioTrue){		
		clicker.siblings(toggleArea).slideDown({
			duration: speedVeryFast, 
			easing: 'easeOutExpo'
		});
		$toggleField.removeAttr('disabled');
	} else {
		clicker.siblings(toggleArea).slideUp({
			duration: speedVeryFast, 
			easing: 'easeOutExpo'
		});
		$toggleField.attr('disabled','disabled');
	}	
};

$toggleAreaLabel.each(function (){
	var clicker, input;
	clicker = $(this);
	input = clicker.find("input:first");
	
	if( input.is(':checked') && input.attr('id') == radioTrue){
		$toggleField.removeAttr('disabled');
		$(toggleArea).show();
	}
	
	clicker.click(function(){
		if(clicker.children('input[type="radio"]').length > 0){
			toggleBlock($(this), input);
		}
	});
	
});
*/

	
/**
 * 
 * Modal: jQuery tools - Overlay
 * =============================
 * - Load modal with defined configurations
 * - Aligns modal to the middle of the viewport
 * - Adds scrollbars to modal content if it doesn't fit in the viewport
 * - Types
 *  - minimal: for simple confirms
 *  - full: for accept invitation
 * 
 * FIXME:
 * - Form buttons are pushed to the bottom when scrollbars are visible. Add some space after the form.
 *  
 * */
	jQuery.fn.loadModal = function(modalType){
		var modal, topPos, modalFixed, maxTopPos, modalHeight, $modalContent, $scrollable;
		modal = $(this);
		
		$modalContent = modal.children('.modal-content');
		$scrollable = $modalContent.children('.scrollable'); // Used when content can be very long. For examples in namelists.
		
		modalHeight = function(){
			return modal.height();
		};
		
		topPos = function(){
			if ((modalType == "full" || modalType == "fixed") && (modalHeight() < vpHeight) ) {
				return Math.floor((vpHeight-modalHeight())/2);
			} else if (modalType == "minimal") {
				// 10% of viewport's size seems to be fine
				return Math.floor(0.1 * vpHeight);
			} else {
				return 10; // 10 px
			}
			
		};
		modalFixed = function(){
			if(modalType == "full") {
				return false;
			} else {
				return true;
			} 
		};
		
		modal.overlay({
		    fixed: modalFixed(),	// modal position
		    top: topPos(),			// custom top position
		    mask: {					// custom mask
		    color: '#000',			// you might also consider a "transparent" color for the mask
		    loadSpeed: speedFast,	// load mask a little faster
		    opacity: 0.5			// very transparent
		    },
		    onBeforeLoad: function() {						// In some cases close-link has href for NOSCRIPT-users.
		    	modal.find('.close').removeAttr("href");	// Removing href to prevent any actions. Preventing default-action did not work.
		    	$('.binder').each(function(){
		    		$(this).bindCheckbox();					// Bind checkbox with submit button (used in remove support votes for example)
		    	});
		    	$(".chzn-select").loadChosen();
		    	
		    	// TODO: Test this properly. We might want to use this.
		    	setTimeout(function () { modal.find('input[type="text"]:first, textarea:first').focus(); }, 50);
		    },
		    closeOnClick: false,	// disable this for modal dialog-type of overlays
		    load: true				// load it immediately after the construction
		}).addClass(modalType);
		
		// Adjust modal after load
		adjustModal(modal, modalType, $modalContent, $scrollable);

		// Adjust modal when user resizes viewport
		$(window).bind('resize', function(){
			vpHeight = $(this).height();
			vpWidth = $(this).width();
			modal.css('top',topPos()+'px');
			
			adjustModal(modal, modalType, $modalContent, $scrollable);
		});
	};
	
	// Adjust modal's position and height
	var adjustModal = function(modal, modalType, $modalContent,$scrollable){
		var modalPosX;
		modalPosX = (vpWidth - modal.width())/2;
		modal.css('left',modalPosX);
		
		$scrollable.css('max-height', 0.75*vpHeight); // Adjust value if needed

		if (modalType == "minimal"){
			
			if (modal.height() > vpHeight) {
				modal.css('position','absolute');
			} else {
				modal.css('position','fixed');
			}
		}
	};
	
	

/**
 * 
 * Modal-loaders
 * =============
 * - Uses class-based actions
 * - Modals loaded also on page load if correct data exists
 * - Calls generateModal() with modalData variable which includes all HTML content for the modal
 * 
 * */
	
	// Initiative saved and ready to collect participants
	if( typeof modalData != 'undefined' && typeof modalData.requestMessage != 'undefined' ){
		generateModal(modalData.requestMessage(), 'minimal');
	}	

 	// Show initiative's public user list
	$('.js-show-franchise-list').click(function(){
		try {
			generateModal(modalData.participantListFranchise(), 'full');
			return false;
		} catch(e) {
			console.log(e);
		}
	});
	
	$('.js-show-no-franchise-list').click(function(){
		try {
			generateModal(modalData.participantListNoFranchise(), 'full');
			return false;
		} catch(e) {
			console.log(e);
		}
	});

	// Show initiative's public user list
	$('.js-participate').click(function(){
		try {
			generateModal(modalData.participateForm(), 'full');
			return false;
		} catch(e) {
			console.log(e);
		}
	});
	
	if( typeof modalData != 'undefined' && typeof modalData.participateFormInvalid != 'undefined' ){
		generateModal(modalData.participateFormInvalid(), 'full');
	}

	
/**
 * 
 * Datepicker: jQuery tool - Dateinput
 * ===================================
 * - Uses the global Init.getLocale() variable to determine the localization
 * 
 * */
	$.tools.dateinput.localize("fi", {
		  months: 'Tammikuu,Helmikuu,Maaliskuu,Huhtikuu, Toukokuu, Kesäkuu, Heinäkuu, Elokuu, Syyskuu, Lokakuu, Marraskuu, Joulukuu',
		  shortMonths:  'Tammi, Helmi, Maalis, Huhti, Touko, Kesä, Heinä, Elo, Syys, Loka, Marras, Joulu',
		  days:         'Sunnuntai, Maanantai, Tiistai, Keskiviikko, Torstai, Perjantai, Lauantai',
		  shortDays:    'Su, Ma, Ti, Ke, To, Pe, La'
	});
	$.tools.dateinput.localize("sv", {
		  months: 'Januari, Februari, Mars, April, Maj, Juni, Juli, Augusti, September, Oktober, November, December',
		  shortMonths:  'Jan, Feb, Mar, Apr, Maj, Jun, Jul, Aug, Sep, Okt, Nov, Dec',
		  days:         'Söndag, Måndag, Tisdag, Onsdag, Torsdag, Lördag',
		  shortDays:    'Sö, Må, Ti, On, To, Fr, Lö'
	});
	
	$.tools.dateinput.conf.lang = Init.getLocale();

	$(".datepicker").dateinput({
		format: 	Init.getDateFormat(),	// this is displayed to the user
		firstDay:	1,						// First day is monday
		offset:		[0, 0],
	 
		// a different format is sent to the server
		change: function() {
			var isoDate = this.getValue('yyyy-mm-dd');
			$("#backendValue").val(isoDate);
		}
	})
	.blur(function(){
		// Trim spaces in copy pasted value
		var trimmed = $.trim( $(this).val() );
		$(this).val(trimmed);
	});
	
/**
 * 
 * Tooltip: jQuery tools - Tooltip
 * ===============================
 * 
 * */
	
	$('.trigger-tooltip').tooltip({
		animation:	true,
		effect:		'fade',
		placement:	'top right', // FIXME: this doesn't seem to work correctly
		offset:		[-5, 0],
		trigger:	'hover'
	});
	

/**
 * DirtyForms jQuery plugin
 * ========================
 * http://mal.co.nz/code/jquery-dirty-forms/
 * 
 * - Checks if form is modified and fires up a notification
 * - To improve usage, LiveQuery could be used
 *   http://brandonaaron.net/code/livequery/docs
 * - $.DirtyForms.dialog overrides plugin's default facebox-dialog
 *   
 * TODO:
 * - Fix dynamic links (for example "Add new link")
 * - Check methods: refire and stash
 * 
 * */
	
$.DirtyForms.dialog = {
	ignoreAnchorSelector: 'a[rel="external"], .modal a', // Ignore external links
		
	// Selector is a selector string for dialog content. Used to determine if event targets are inside a dialog
	selector : '.modal .modal-content',

	// Fire starts the dialog
	fire : function(message, title){
		try {
			generateModal(modalData.formModifiedNotification(), "minimal");
		} catch(e) {
			// TODO: What to do in here? Should we just skip confirmation?
			console.log(e);
		}
	},
	// Bind binds the continue and cancel functions to the correct links
	bind : function(){
	    $('.modal .close').click(function(e){
	    	$.DirtyForms.choiceContinue = false;
	    	$.DirtyForms.choiceCommit(e);
	    });
		$('.modal .continue').click(function(e){
			$.DirtyForms.choiceContinue = true;
			$.DirtyForms.choiceCommit(e);
		});
	    $(document).bind('decidingcancelled.dirtyforms', function(){
	        $(document).trigger('close.facebox');
	    });
	},

	// Refire handles closing an existing dialog AND fires a new one
	refire : function(content){
		return false;
		
//	    var rebox = function(){
//	    	generateModal(modalData.formModifiedNotification());
//	        //$.facebox(content);
//	        $(document).unbind('afterClose.facebox', rebox);
//	    }
//	    $(document).bind('afterClose.facebox', rebox);
	},

	// Stash returns the current contents of a dialog to be refired after the confirmation
	// Use to store the current dialog, when it's about to be replaced with the confirmation dialog.
	// This function can return false if you don't wish to stash anything.
	stash : function(){
		return false;
		
//	    var fb = $('#facebox .content');
//	    return ($.trim(fb.html()) == '' || fb.css('display') != 'block') ?
//	       false :
//	       fb.clone(true);
       
	}
};

// Listen forms that have class 'sodirty'
$('form.sodirty').dirtyForms();


/**
 * 
 * Validation: jQuery tools - Form validation
 * ==========================================
 * TODO:
 * - Will be implemented much later
 * - Localizations should be loaded from message.properties
 * 
 * */

$.tools.validator.localize("fi", {
	'*'			: 'Virheellinen arvo',
	':email'  	: 'Virheellinen s&auml;hk&ouml;postiosoite',
	':number' 	: 'Arvon on oltava numeerinen',
	':url' 		: 'Virheellinen URL',
	'[max]'	 	: 'Arvon on oltava pienempi, kuin $1',
	'[min]'		: 'Arvon on oltava suurempi, kuin $1',
	'[required]'	: 'Kent&auml;n arvo on annettava'
});
$.tools.validator.localize("sv", {
	'*'			: 'SV: Virheellinen arvo',
	':email'  	: 'SV: Virheellinen s&auml;hk&ouml;postiosoite',
	':number' 	: 'SV: Arvon on oltava numeerinen',
	':url' 		: 'SV: Virheellinen URL',
	'[max]'	 	: 'SV: Arvon on oltava pienempi, kuin $1',
	'[min]'		: 'SV: Arvon on oltava suurempi, kuin $1',
	'[required]'	: 'SV: Kent&auml;n arvo on annettava'
});


/**
 * TODO: Clean the validation code 
 *  - If the custom-effect 'inline' is used, remove unneeded options
 *  - Error-messages gets multiplied after each failed form send
 */
/* NOTE: Disabled ATM (20.6.2012) to ease the testing of backend validation */ 
// Add novalidate attribute for preventing Browser's default validation
/*$("form.validate").attr("novalidate","novalidate")
.validator({
	effect: 'inline',
	errorInputEvent: 'keyup',
	lang: Init.getLocale(),
	position: 'top left',
	offset: [-12, 0],
	message: '<div><em/></div>' // em element is the arrow
		
}).bind("onFail", function(e, errors)  {
	
	// we are only doing stuff when the form is submitted
	if (e.originalEvent.type == 'submit') {
 
		// loop through Error objects and add highlight
		$.each(errors, function()  {
			var input = this.input;
			input.addClass("has-error").focus(function()  {
				input.removeClass("has-error");
			});
		});
	
		
		// TODO: Smooth scroll to the first error or to top of the form. The code below causes errors in some cases
		//var positionFirstError = $('form').position().top + $('.has-error:first').prev('label').position().top - 30;
		//console.log(positionFirstError);
		//$('html, body').animate({scrollTop: positionFirstError}, 800);

	}
	
});*/


// Custom effect for the validator
/*
 * TODO: ADD keyup-event for validating inputs.
 */
$.tools.validator.addEffect("inline", function(errors, event) {
 
	// add error before errorenous input
	$.each(errors, function(index, error) {
		error.input.before('<div class="system-msg msg-error">'+error.messages[0]+'</div>');
	});
	
	// validate field on edit
	$('input, textarea').keyup(function()  {
		var inputError = $(this).prev();
		
		if (inputError.hasClass("msg-error")){
			inputError.remove();
		}
	});
	
// the effect does nothing when all inputs are valid
}, function(inputs)  {
 
});


/**
* Bind checkbox
* =============
* - Binds checkbox '.binder' with submit button '.bind'.
* - Binds radiobuttons as well
* - Button is enabled when checkbox/radio is checked otherwise disabled
*/
jQuery.fn.bindCheckbox = function(){
	var cb, btn, cbVal;
	
	cb = $(this);
	btn = cb.parents('form').find('.bind');
	cbVal = function(){
		if (cb.is(':checked')){
			btn.removeAttr('disabled').removeClass('disabled');
		} else {
			btn.attr('disabled','disabled').addClass('disabled');
		}
	};

	var updateSaveButtonText = function( thisCb ){
		var btnSaveText = $('button[name="save"] span');

		if(thisCb.val() == 'FALSE'){
			btnSaveText.text(btnSaveText.data('textsend'));
		} else {
			btnSaveText.text(btnSaveText.data('textsave'));
		}
	}
	
	cbVal();
	cb.change(function(){
		cbVal();

		if (btn.attr('name') == 'save'){
			updateSaveButtonText( $(this) );
		}
	});
};
$('.binder').bindCheckbox();


});