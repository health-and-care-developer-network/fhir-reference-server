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
package uk.nhs.fhir.makehtml.uml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;

import org.anarres.graphviz.builder.GraphVizGraph;
import org.anarres.graphviz.builder.GraphVizGraphOption;
import org.anarres.graphviz.builder.GraphVizScope;

/**
 * This is an experimental class to generate UML representations of resources
 * Currently just using hard-coded values to test graphviz and the java API for
 * generating the input dot file.
 * @author Adam Hatherly
 * @see https://github.com/shevek/graphviz4j
 * @see http://www.ffnn.nl/pages/articles/media/uml-diagrams-using-graphviz-dot.php
 * @see http://www.graphviz.org/content/command-line-invocation
 */
public class MakeUML implements GraphVizScope {

	public static void main(String[] args) {
		MakeUML builder = new MakeUML();
		builder.run();
	}
	
	public void run() {
		GraphVizGraph graph = new GraphVizGraph();
	    //assertNotNull(graph.node(this, "foo"));
	    //assertSame(graph.node(this, "foo"), graph.node(this, "foo"));
	    graph.setGraphOption(GraphVizGraphOption.rankdir, "LR");
	    
	    /*graph.node(this, "foo").label("something");
	    graph.node(this, "bar").label("something").comment("multi\nline\nnode comment");
	    graph.edge(this, "bar", "foo");
	    graph.edge(this, "foo", "bar").label("Some label").comment("multi\nline\nedge comment\n\n");

	    graph.label("This is my graph\ntitle");

	    graph.cluster(this, "foo").label("My cluster").add(this, "foo");*/
	    
	    
	    
	    graph.node(this, "Patient").label("Patient (Domain Resource)|identifier : identifier [0..*]\nInactive : boolean [0..1]").shape("record");
	    graph.node(this, "Animal").label("Animal|species : CodeableConcepr [1..1] << AnimalSpecies?? >>").shape("record");
	    graph.edge(this,"Patient", "Animal");

	    String dotFile = "";
	    try {
	    	StringWriter writer = new StringWriter();
	        graph.writeTo(writer);
	        dotFile = writer.toString();
	        System.out.println("Graph is:\n" + dotFile);
	    } catch(IOException e) {
	    	e.printStackTrace();
	    }
	    System.out.println("Graph is " + graph);
	    
	    executeCommand("dot -Tpng -otest.png", dotFile);
	}

	private String executeCommand(String command, String dotFile) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			
			// Send data into dot through stdin
			OutputStream stdin = p.getOutputStream();
			for (byte b : dotFile.getBytes()) {
				stdin.write(b);
			}
			stdin.flush();
			stdin.close();
			
			// Now, wait for the response
			p.waitFor();
			BufferedReader reader =
                            new BufferedReader(new InputStreamReader(p.getInputStream()));

                        String line = "";
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();
	}
}
