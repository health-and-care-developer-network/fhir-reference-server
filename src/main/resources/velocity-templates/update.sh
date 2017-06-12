#!/bin/bash

DN_URL=$1
ROOT_URL=$2
ROOT_CRUMB="FHIR Reference Server"

OLD_URL="developer-wp.azurewebsites.net"
NEW_URL="developer.nhs.uk"

if [ -z $DN_URL ]
then
  echo "Usage: update.sh developer_network_url root_domain    - for example ./update.sh https://developer.nhs.uk/apis/ developer.nhs.uk"
else
  wget --convert-links --output-document=devnet.html $DN_URL  # Download page


# Fix any dodgy URLs
sed -i -- "s|$OLD_URL|$NEW_URL|g" devnet.html


  # Add custom styles
  sed -i '/\/wp-content\/themes\/HDN\/style.css/a\		<link href="\/js\/jquery-ui\/jquery-ui.css" rel="stylesheet"><link rel="stylesheet" href="\/style.css"\/>' devnet.html

  # Update breadcrumbs
  #NEW_CRUMBS="<span xmlns:v=\"http:\/\/rdf\.data-vocabulary\.org\/#\"><span typeof=\"v:Breadcrumb\"><a href=\"http:\/\/$ROOT_URL\/\" rel=\"v:url\" property=\"v:title\">Home<\/a>  <span class=\"bc_arrow\" aria-hidden=\"true\" data-icon=\"&#x2a;\"></span></li>    <li><span xmlns:v=\"http:\/\/rdf\.data-vocabulary\.org\/#\"><span typeof=\"v:Breadcrumb\"><a href=\"http:\/\/$ROOT_URL\/\/apis\/\" rel=\"v:url\" property=\"v:title\">APIs<\/a>  <span class=\"bc_arrow\" aria-hidden=\"true\" data-icon=\"&#x2a;\"><\/span><\/li>     <li> <strong class=\"breadcrumb_last\">$ROOT_CRUMB<\/strong><\/span><\/span><\/li><\/ul>	<\/div><!--end wrapper-->"
  NEW_CRUMBS="#parse( \"\/velocity-templates\/breadcrumbs.vm\" )"
  sed -i '/typeof=\"v:Breadcrumb\"/c\'"$NEW_CRUMBS"'' devnet.html

  # Clear out page content
  sed -i '/<div id="api_list">/,/<!-- end api list -->/{//!d}' devnet.html
  
  # Add in a velocity template field we can replace when generating pages
  sed -i '/<div id="api_list">/c\<div id="api_list"><div class="wrapper cf container">$page-content<div><div>&nbsp;<\/div>' devnet.html

  # Add jquery ui tabs initialisation
  sed -i '/<\/body>/i\        <script src="\/js\/jquery-ui\/jquery-ui.js"></script><script src="\/js\/site-functions.js"></script>' devnet.html

  # Copy this to be our index page template
  cp devnet.html index.vm

  echo "Template updated"
fi

