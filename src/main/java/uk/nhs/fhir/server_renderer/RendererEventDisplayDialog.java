package uk.nhs.fhir.server_renderer;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.error.EventType;
import uk.nhs.fhir.error.RendererEvent;
import uk.nhs.fhir.error.RendererEvents;
import uk.nhs.fhir.util.StringUtil;

@SuppressWarnings("serial")
public class RendererEventDisplayDialog extends JDialog {
	private final JTree tree;
	
	public RendererEventDisplayDialog(List<RendererEvents> events, JFrame parentWindow) {
		super(parentWindow, "Renderer Events Log", ModalityType.TOOLKIT_MODAL);

		setSize(1000, 500);
		setLayout(new BorderLayout());
	    
	    tree = new JTree(createNodes(events));
	    JScrollPane treeView = new JScrollPane(tree);
	    
	    add(treeView, BorderLayout.CENTER);
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			JOptionPane.showMessageDialog(RendererEventDisplayDialog.this,
			    "Rendering failed for some files - please review errors",
			    "Rendering error",
			    JOptionPane.ERROR_MESSAGE);
		}
		
		super.setVisible(visible);
	}

	private TreeNode createNodes(List<RendererEvents> eventsList) {
		Map<String, DefaultMutableTreeNode> nodesByFilePath = new HashMap<>();
		Map<String, Optional<WrappedResource<?>>> resourcesByFilePath = new HashMap<>();
		
		String commonFilepathStart = calculateCommonFilepathStart(eventsList);
		
		Set<String> filesWithErrors = 
			eventsList.stream()
				.filter(
					rendererEvents -> 
						rendererEvents.getEvents()
							.stream()
							.anyMatch(rendererEvent -> 
								rendererEvent.getEventType()
												.equals(EventType.ERROR)))
				.map(rendererEvents -> rendererEvents.getFile().getAbsolutePath())
				.collect(Collectors.toSet());
		
		for (RendererEvents eventGroup : eventsList) {
			String filePath = eventGroup.getFile().getAbsolutePath();
			resourcesByFilePath.put(filePath, eventGroup.getResource());
			
			if (!nodesByFilePath.containsKey(filePath)) {
				nodesByFilePath.put(filePath, newFileNode(filePath, commonFilepathStart, eventGroup.getResource()));
			}
			
			DefaultMutableTreeNode fileNode = nodesByFilePath.get(filePath);
			
			for (RendererEvent event : eventGroup.getEvents()) {

				String eventString = getEventString(event);
				
				DefaultMutableTreeNode eventNode = new DefaultMutableTreeNode(eventString);
				
				if (event.getError().isPresent()) {
					// tree data is displayed in a label. Use HTML to make it multi-line.
					String htmlStacktrace = "<html>" + StringUtil.getStackTrace(event.getError().get()).replace("\n", "<br>") + "</html>";
					DefaultMutableTreeNode stacktraceNode = new DefaultMutableTreeNode(htmlStacktrace);
					eventNode.add(stacktraceNode);
				}
				
				fileNode.add(eventNode);
			}
		}
		
		List<Map.Entry<String, DefaultMutableTreeNode>> sortedNodes = new ArrayList<>(nodesByFilePath.entrySet());
		
		// Order by file path
		sortedNodes.sort(new Comparator<Map.Entry<String, DefaultMutableTreeNode>>(){
			@Override
			public int compare(Entry<String, DefaultMutableTreeNode> o1, Entry<String, DefaultMutableTreeNode> o2) {
				Optional<WrappedResource<?>> wrappedResource1 = resourcesByFilePath.get(o1.getKey());
				Optional<WrappedResource<?>> wrappedResource2 = resourcesByFilePath.get(o2.getKey());
				
				if (!wrappedResource1.isPresent() 
				  || !wrappedResource2.isPresent()) {
					return 0;
				}
				
				return wrappedResource1.get().getName().compareTo(wrappedResource2.get().getName());
			}
		});
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Events");
		DefaultMutableTreeNode justWarnings = new DefaultMutableTreeNode("Warnings");
		DefaultMutableTreeNode withErrors = new DefaultMutableTreeNode("Errors");
		
		root.add(justWarnings);
		root.add(withErrors);
		
		for (Map.Entry<String, DefaultMutableTreeNode> entry : sortedNodes) {
			if (filesWithErrors.contains(entry.getKey())) {
				withErrors.add(entry.getValue());
			} else {
				justWarnings.add(entry.getValue());
			}
		}
		
		return root;
	}

	private String getEventString(RendererEvent event) {
		EventType eventType = event.getEventType();
		Optional<String> message = event.getMessage();
		Optional<Exception> error = event.getError();
		String eventString = eventType.toString();
		if (message.isPresent()) {
			eventString += " - " + message.get();
		}
		
		if (error.isPresent()
		  && error.get().getMessage() != null) {
			eventString += " - " + error.get().getMessage();
		}
		return eventString;
	}

	private DefaultMutableTreeNode newFileNode(String filePath, String commonFilepathStart, Optional<WrappedResource<?>> resource) {
		String trimmedFilePath = filePath.substring(commonFilepathStart.length());
		
		String truncatedFilePath = "";
		if (trimmedFilePath.length() > 0) {
			truncatedFilePath = " (" + File.separator + trimmedFilePath + ")";
		}

		String name;
		if (resource.isPresent()) {
			name = resource.get().getName();
		} else {
			name = "[unknown resource name]";
		}
		
		return new DefaultMutableTreeNode(name + truncatedFilePath);
	}

	private String calculateCommonFilepathStart(List<RendererEvents> eventsList) {
		String commonFilepathStart = "";
		
		for (RendererEvents eventGroup : eventsList) {
			String filePath = eventGroup.getFile().getAbsolutePath();
			String grandparentPath = getGrandparentDirectoryPath(filePath);
			
			if (commonFilepathStart.equals("")) {
				commonFilepathStart = grandparentPath;
			} else if (!grandparentPath.startsWith(commonFilepathStart)) {
				commonFilepathStart = StringUtil.commonStringStart(commonFilepathStart, grandparentPath);
			}
		}
		
		return commonFilepathStart;
	}
	
	private String getGrandparentDirectoryPath(String filePath) {
		String parentDirectoryPath = filePath.substring(0, filePath.lastIndexOf(File.separatorChar));
		return parentDirectoryPath.substring(0, parentDirectoryPath.lastIndexOf(File.separatorChar));
	}
}
