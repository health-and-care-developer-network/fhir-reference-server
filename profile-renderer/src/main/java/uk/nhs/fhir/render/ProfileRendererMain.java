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
package uk.nhs.fhir.render;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.event.RendererLoggingEventHandler;

/**
 * @author tim.coates@hscic.gov.uk
 */
public class ProfileRendererMain {
	private static final Logger LOG = LoggerFactory.getLogger(ProfileRendererMain.class);
	
	/**
     * Main entry point.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    	RendererCliArgs cliArgs = new RendererCliArgsParser().parseArgs(args);
    	if (cliArgs == null) {
    		LOG.error("Failed to parse renderer arguments. Exiting.");
    		System.exit(1);
    	}
    	
    	FhirProfileRenderer renderer = new FhirProfileRenderer(
    		cliArgs.getInputDir(), 
    		cliArgs.getOutputDir(), 
    		cliArgs.getNewBaseUrl(), 
    		cliArgs.getAllowedMissingExtensionPrefixes(), 
    		new RendererLoggingEventHandler(), 
    		cliArgs.getLocalDomains());
    	
    	RendererExitStatus exitStatus = renderer.process();
    	System.exit(exitStatus.exitCode());
    }
}
