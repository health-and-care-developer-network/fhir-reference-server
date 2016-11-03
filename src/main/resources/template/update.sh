#!/bin/bash

DN_URL=$1

CRUMB="FHIR Reference Server"

if [ -z $DN_URL ]
then
  echo "Usage: update.sh developer_network_url"
else
  wget --convert-links --output-document=devnet.html $DN_URL  # Download page

  # Update breadcrumbs
  NEW_CRUMBS="<span xmlns:v=\"http:\/\/rdf\.data-vocabulary\.org\/#\"><span typeof=\"v:Breadcrumb\"><a href=\"\/\" rel=\"v:url\" property=\"v:title\">Home<\/a>  <span class=\"bc_arrow\" aria-hidden=\"true\" data-icon=\"&#x2a;\"></span></li>    <li><span xmlns:v=\"http:\/\/rdf\.data-vocabulary\.org\/#\"><span typeof=\"v:Breadcrumb\"><a href=\"\/apis\/\" rel=\"v:url\" property=\"v:title\">APIs<\/a>  <span class=\"bc_arrow\" aria-hidden=\"true\" data-icon=\"&#x2a;\"><\/span><\/li>     <li> <strong class=\"breadcrumb_last\">$CRUMB<\/strong><\/span><\/span><\/li><\/ul>	<\/div><!--end wrapper-->"
  sed -i '/typeof=\"v:Breadcrumb\"/c\'"$NEW_CRUMBS"'' devnet.html

  # Clear out page content
  sed -i '/<div id="api_list">/,/<!-- end api list -->/{//!d}' devnet.html
  
  # Add in a pattern we can replace when generating pages
  sed -i '/<div id="api_list">/c\<div id="api_list"><div class="wrapper cf container">{{PAGE-CONTENT}}<div><div>&nbsp;<\/div>' devnet.html

  echo "Template updated"
fi

