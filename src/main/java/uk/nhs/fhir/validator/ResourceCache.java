/*
 * Copyright (C) 2016 Health and Social Care Information Centre.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.nhs.fhir.validator;

import java.util.HashMap;
import java.util.logging.Logger;
import org.hl7.fhir.instance.model.DomainResource;
import uk.nhs.fhir.util.PropertyReader;


/**
 * Holds an in-memory cache of the resources we've fetched for validation.
 * @author Originally Adam Hatherly - reused and abused by Tim Coates
 */
public class ResourceCache {
	private static final Logger LOG = Logger.getLogger(ResourceCache.class.getName());

	// Singleton object to act as a cache of the files in the profiles directory
	private static HashMap<String, DomainResource> profileFileList = new HashMap<String, DomainResource>();
	
	private static long lastUpdated = 0;
	private static long updateInterval = Long.parseLong(PropertyReader.getProperty("cacheReloadIntervalMS"));	

        /**
         * Just make sure we can't instantiate the class.
         * 
         */
        private ResourceCache() {
        }
        
        /**
         * Get a specified resource.
         * 
         * @param identifier
         * @return 
         */
        public static DomainResource getResource(String identifier) {
            DomainResource resourceItem = null;
            if(updateRequired()) {
                flushCache();
            } else {
                if(profileFileList.containsKey(identifier)) {
                    resourceItem = profileFileList.get(identifier);
                }
            }
            return resourceItem;
        }
        
        /**
         * Cache a new resource
         * 
         * @param key   String identifier of the resource, i.e. it's URL
         * @param value The BaseResource itself.
         */
        public static void putResource(String key, DomainResource value) {
            if(!profileFileList.containsKey(key)) {
                profileFileList.put(key, value);
            }
        }
	
        /**
         * Checks whether enough time has passed that we ought to flush the cache
         * 
         * @return 
         */
        private static boolean updateRequired() {
            long currentTime = System.currentTimeMillis();
            if(updateInterval == 0) {   // Fallback if config didnt work
                updateInterval = 30000;
            }
            
            if (currentTime > (lastUpdated + updateInterval)) {
                LOG.fine("Cache needs updating");
                return true;
            }
            return false;
        }
	
        /**
         * As expected, it just flushes all values out of the cache
         * 
         */
	private synchronized static void flushCache() {
		if (updateRequired()) {
                    profileFileList.clear();
                    lastUpdated = System.currentTimeMillis();
		}
	}
}
