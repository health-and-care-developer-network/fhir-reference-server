/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package uk.nhs.fhir.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import com.google.common.base.Strings;

/**
 * Convenience class to read configuration from a property file
 * @author Adam Hatherly
 */
public class FhirServerProperties {
	public static final String SERVLET_CONTEXT_PROPERTY_PROPERTIES = "uk.nhs.fhir.servlet.properties-file";

	private static final String PROP_FAVICON_FILE_NAME = "faviconFile";
	private static final String PROP_DEFAULT_PAGE_SIZE = "defaultPageSize";
	private static final String PROP_MAX_PAGE_SIZE = "maximumPageSize";
	private static final String PROP_RESOURCE_ROOT_PATH = "defaultResourceRootPath";
	private static final String PROP_RESOURCE_FOLDER_PREFIX = "resourceFolderPrefix";
	private static final String PROP_VELOCITY_TEMPLATE_PATH = "velocityTemplateDirectory";

	private final Properties properties;
	
	public FhirServerProperties(String propertiesFile) {
		this.properties = parseProperties(propertiesFile);
	}
	
    public static Properties parseProperties(String propertiesFile) {
    	if (Strings.isNullOrEmpty(propertiesFile)) {
    		throw new IllegalArgumentException("Null properties File path");
    	}
    	
    	Properties properties = new Properties();
    	
    	//Load the property values into a local object from the property file.
    	boolean absolute = (propertiesFile.startsWith("/") || 
    	  (propertiesFile.length() > 1 && propertiesFile.charAt(1) == ':'));
    	
    	if (absolute) {
    		try (FileInputStream in = new FileInputStream(new File(propertiesFile))) {
	        	properties.load(in);
	        } catch (Exception ex) {
	        	throw new IllegalStateException("Error loading properties from file " + propertiesFile, ex);
	        }
    	} else {
	        try (InputStream in = FhirServerProperties.class.getClassLoader().getResourceAsStream(propertiesFile)){
	        	properties.load(in);
	        } catch (Exception ex) {
	        	throw new IllegalStateException("Error loading properties from file " + propertiesFile, ex);
	        }
    	}
        
        return properties;
    }

    /**
     * Retrieve the value of the property with the specified name
     * @param propertyName Name of property to retrieve
     * @return Value of property
     */
    private Object getProperty(String propertyName) {
		return properties.get(propertyName);
    }

    // Should only get called once on Velocity init, so okay to parse on request, rather than adding to ServletContext
	public String getVelocityTemplatePath() {
		return (String)getProperty(PROP_VELOCITY_TEMPLATE_PATH);
	}
    
    public String getFaviconPath() {
    	return (String)getProperty(PROP_FAVICON_FILE_NAME);
    }
    
    public int getDefaultPageSize() {
    	return Integer.parseInt((String)getProperty(PROP_DEFAULT_PAGE_SIZE));
    }
    
    public int getMaxPageSize() {
    	return Integer.parseInt((String)getProperty(PROP_MAX_PAGE_SIZE));
    }
    
    public String getResourceRootPath() {
    	return (String)getProperty(PROP_RESOURCE_ROOT_PATH);
    }
    
    public String getResourceFolderPrefix() {
    	return (String)getProperty(PROP_RESOURCE_FOLDER_PREFIX);
    }
}
