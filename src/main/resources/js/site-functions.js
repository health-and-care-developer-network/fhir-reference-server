
// http://localhost:8080/artefact?resourceID=gpconnect-appointment-1&artefactType=DIFFERENTIAL
function loadTab(tabID, resourceID, resourceVersion, artefactType) {
	$( tabID ).load( "/artefact?resourceID="+resourceID+"&resourceVersion="+resourceVersion+"&artefactType="+artefactType );
}

// Initialise tabs
$( "#tabs" ).tabs();
