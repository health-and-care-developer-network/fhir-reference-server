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
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Errors");

		Map<String, DefaultMutableTreeNode> nodesByFilePath = new HashMap<>();
		Map<String, WrappedResource<?>> resourcesByFilePath = new HashMap<>();
		
		String commonFilepathStart = null;
		for (RendererEvents eventGroup : eventsList) {
			String filePath = eventGroup.getFile().getAbsolutePath();
			
			if (commonFilepathStart == null) {
				commonFilepathStart = filePath;
			} else if (!filePath.startsWith(commonFilepathStart)) {
				commonFilepathStart = StringUtil.commonStringStart(commonFilepathStart, filePath);
			}
		}
		
		for (RendererEvents eventGroup : eventsList) {
			String filePath = eventGroup.getFile().getAbsolutePath();
			resourcesByFilePath.put(filePath, eventGroup.getResource());
			
			String name;
			if (eventGroup.getResource() != null) {
				name = eventGroup.getResource().getName();
			} else {
				name = "[unknown resource name]";
			}
			
			// combine event lists by file path if necessary
			DefaultMutableTreeNode fileNode;
			if (nodesByFilePath.containsKey(filePath)) {
				fileNode = nodesByFilePath.get(filePath);
			} else {
				String trimmedFilePath = filePath.substring(commonFilepathStart.length());
				fileNode = new DefaultMutableTreeNode(name + " (" + File.separator + trimmedFilePath + ")");
				nodesByFilePath.put(filePath, fileNode);
			}
			
			for (RendererEvent event : eventGroup.getEvents()) {
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
				
				DefaultMutableTreeNode eventNode = new DefaultMutableTreeNode(eventString);
				
				if (error.isPresent()) {
					// tree data is displayed in a label. Use HTML to make it multi-line.
					String htmlStacktrace = "<html>" + StringUtil.getStackTrace(error.get()).replace("\n", "<br>") + "</html>";
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
				WrappedResource<?> wrappedResource1 = resourcesByFilePath.get(o1.getKey());
				WrappedResource<?> wrappedResource2 = resourcesByFilePath.get(o2.getKey());
				
				if (wrappedResource1 == null 
				  || wrappedResource2 == null) {
					return 0;
				}
				
				return wrappedResource1.getName().compareTo(wrappedResource2.getName());
			}
		});
		
		for (Map.Entry<String, DefaultMutableTreeNode> entry : sortedNodes) {
			root.add(entry.getValue());
		}
		
		return root;
	}
}
