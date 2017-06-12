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
			$( "#tabs" ).find('a[href=#tabs-' + tabName + ']').click();
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
function loadTab(tabID, resourceID, resourceVersion, artefactType) {
	$( tabID ).load( "/artefact?resourceID="+resourceID+"&resourceVersion="+resourceVersion+"&artefactType="+artefactType,
			setupLinkInterceptsInTabs);
}

// Initialise tabs
$( "#tabs" ).tabs();
setupLinkInterceptsInTabs();