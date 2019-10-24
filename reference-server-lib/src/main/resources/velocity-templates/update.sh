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

# Add warning to file to say that it is generated
sed -i '1 i\ ## WARNING - This file is generated using the update.sh script, so should not be edited directly! ** ' devnet.html

# Fix any dodgy URLs
sed -i -- "s|$OLD_URL|$NEW_URL|g" devnet.html


  # Add custom styles
  sed -i '/\/wp-content\/themes\/HDN\/style.css/a\		<link href="\/js\/jquery-ui\/jquery-ui.css" rel="stylesheet"><link rel="stylesheet" href="\/style\/style.css"\/>' devnet.html

  # Update breadcrumbs
  NEW_CRUMBS="#parse( \"\/velocity-templates\/breadcrumbs.vm\" )"
  sed -i '/typeof=\"v:Breadcrumb\"/c\'"$NEW_CRUMBS"'' devnet.html

  # Update page title
  NEW_TITLE="		<title>FHIR Reference Server \$!resource.getName()</title>"
  sed -i '/<title>/c\'"$NEW_TITLE"'' devnet.html

  # Add crawler description
  CRAWLER="<meta id=\"page-description\" name=\"description\" content=\"\$crawlerDescription\" />"
  sed -i '/\/ Yoast SEO plugin/a\'"$CRAWLER"'' devnet.html

  # Clear out page content
  sed -i '/<div id="api_list">/,/<!-- end api list -->/{//!d}' devnet.html
  
  # Add in a velocity template field we can replace when generating pages
  sed -i '/<div id="api_list">/c\<div id="api_list"><div class="wrapper cf container">#parse($contentTemplateName)<\/div>' devnet.html

  # Add jquery ui tabs initialisation
  sed -i '/<\/body>/i\        <script src="\/js\/jquery-ui\/jquery-ui.js"></script><script src="\/js\/site-functions.js"></script>' devnet.html

  # Remove old copyright statement
  sed -i '/Crown Copyright/d' devnet.html
  # Rewrite contact us to not have the "last" class
  sed -i 's/class="last" href="https:\/\/developer.nhs.uk\/contact-us/href="https:\/\/developer\.nhs\.uk\/contact-us/' devnet.html
  # Add the copyright statement and HL7 trademark statement
  sed -i '/https:\/\/developer\.nhs\.uk\/contact-us/a\<li><a target="_blank" href="http:\/\/www\.hl7\.org\.uk\/">HL7\&reg; and FHIR\&reg; are the registered trademarks of Health Level Seven International<\/a><\/li><li><a class="last" target="_blank" href="https:\/\/www\.nationalarchives\.gov\.uk\/information-management\/our-services\/crown-copyright\.htm" class="copyright">\&copy; Crown Copyright 2017<\/a><\/li>' devnet.html

  # Copy this to be our index page template
  cp devnet.html app-shell.vm

  echo "Template updated"
fi

