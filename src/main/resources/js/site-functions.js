var anchorToJumpTo = "";

// Intercept links within tabs to open the appropriate tab and jump to the anchor point
function setupLinkInterceptsInTabs() {

	if (anchorToJumpTo.length > 0) {
		// Jump to the required anchor
		var anchorElement = $('[name="'+anchorToJumpTo+'"]');
		$(document.body).scrollTop( anchorElement.offset().top);
		anchorToJumpTo = "";
	}
	
	$(".tabLink").click(function() {
		var href = $(this).attr("href");
		var linkParts = href.split("#");
		if (linkParts.length == 2) {
			var tabName = linkParts[0];
			var anchor = linkParts[1];
			
			// Now strip out the extension from the tabName
			var tabName = tabName.split(".")[0];
			
			// Activate the relevant tab
			$( "#tabs" ).find('a[href=\\#tabs-' + tabName + ']').click();
			// And set the anchor to scroll to (once the tab has been loaded)
			anchorToJumpTo = anchor;
		} else {
			console.log("Unable to process link with href:" + href);
		}
		
		// Stop the browser actually trying to follow the link to a new page
		return false;
	});
}

// http://localhost:8080/artefact?resourceID=gpconnect-appointment-1&artefactType=DIFFERENTIAL
function loadTab(tabID, resourceID, resourceVersion, artefactType, baseURL) {
	$( tabID ).load( fixBaseURL(baseURL)+"/artefact?resourceID="+resourceID+"&resourceVersion="+resourceVersion+"&artefactType="+artefactType,
			setupLinkInterceptsInTabs);
}

function loadMetadata() {
	baseURL = $('#metadataFromGenerator').attr('baseURL');
	resourceID = $('#metadataFromGenerator').attr('resourceID');
	resourceVersion = $('#metadataFromGenerator').attr('version');
	metadataType = $('#metadataFromGenerator').attr('metadataType');
	if (typeof metadataType !== typeof undefined && metadataType !== false) {
		$('#metadataFromGenerator').load(
				fixBaseURL(baseURL)+"/artefact?resourceID="+resourceID+"&resourceVersion="+resourceVersion+"&artefactType="+metadataType,
				addExpandCollapseForMetadata);
	}
}

function fixBaseURL(baseURL) {
	protocol = window.location.protocol;
	baseURLprotocol = baseURL.substring(0,6);
	if (protocol == 'https:' && baseURLprotocol != 'https:' ) {
		// Need to alter the base URL to be https (we've been tricked by the reverse proxy...)
		baseURL = baseURL.replace("http://", "https://");
	}
	return baseURL;
}

function loadInitialTab() {
	$('#firstTab').click();
}

function addExpandCollapseForMetadata() {
	$('.metadataSection .fhir-panel-heading-text').prepend("<span class='ui-accordion-header-icon ui-icon ui-icon-triangle-1-s'/>");
	$('.metadataSection .fhir-panel-heading-text').click(function(){
	    $('.metadataSection .fhir-panel-body').slideToggle('slow');
	    $('.metadataSection .ui-icon').toggleClass("ui-icon-triangle-1-s");
	    $('.metadataSection .ui-icon').toggleClass("ui-icon-triangle-1-e");
	});
}

$( document ).ready(function() {
	// Initialise tabs
	$( "#tabs" ).tabs();
	setupLinkInterceptsInTabs();
	loadMetadata();
	loadInitialTab();
});