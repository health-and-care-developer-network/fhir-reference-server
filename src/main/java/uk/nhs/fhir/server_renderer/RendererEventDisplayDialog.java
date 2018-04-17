package uk.nhs.fhir.server_renderer;

import java.awt.BorderLayout;
import java.io.File;
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
import javax.swing.tree.TreePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.data.wrap.WrappedResource;
import uk.nhs.fhir.event.EventType;
import uk.nhs.fhir.event.RendererEvent;
import uk.nhs.fhir.event.RendererEvents;
import uk.nhs.fhir.util.StringUtil;

@SuppressWarnings("serial")
public class RendererEventDisplayDialog extends JDialog {
	
	private static final Logger LOG = LoggerFactory.getLogger(RendererEventDisplayDialog.class);
	
	private final DefaultMutableTreeNode root = new DefaultMutableTreeNode("Events");
	private final DefaultMutableTreeNode justWarnings = new DefaultMutableTreeNode("Warnings");
	private final DefaultMutableTreeNode withErrors = new DefaultMutableTreeNode("Errors");
	private final JTree tree = new JTree(root);
    private final JScrollPane treeView = new JScrollPane(tree);
	
	public RendererEventDisplayDialog(List<RendererEvents> events, JFrame parentWindow) {
		super(parentWindow, "Renderer Events Log", ModalityType.TOOLKIT_MODAL);

		setSize(1000, 500);
		setLayout(new BorderLayout());

		createNodes(events);
		
		if (justWarnings.getChildCount() > 0) {
			root.add(justWarnings);
		}
		if (withErrors.getChildCount() > 0) {
			root.add(withErrors);
		}
		
	    add(treeView, BorderLayout.CENTER);
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			boolean includesErrors = withErrors.getChildCount() > 0;
			boolean includesWarnings = justWarnings.getChildCount() > 0;
			
			if (includesErrors) {
				JOptionPane.showMessageDialog(RendererEventDisplayDialog.this,
					"Rendering failed for some files - please review errors",
					"Rendering error",
				    JOptionPane.ERROR_MESSAGE);
			} else if (includesWarnings) {
				JOptionPane.showMessageDialog(RendererEventDisplayDialog.this,
					"Rendering succeeded with some warnings",
					"Renderer warnings",
				    JOptionPane.WARNING_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(RendererEventDisplayDialog.this,
					"Rendering succeeded",
					"Success",
				    JOptionPane.INFORMATION_MESSAGE);
			}
			
			int levelsToExpand = 2;
			expandTreeRecursive(root, levelsToExpand);
		}
		
		super.setVisible(visible);
	}
	
	private void expandTreeRecursive(DefaultMutableTreeNode node, int levelsToExpand) {
		tree.expandPath(new TreePath(node.getPath()));
		
		if (--levelsToExpand > 0) {
			for (int childIndex=0; childIndex < node.getChildCount(); childIndex++) {
				DefaultMutableTreeNode child = (DefaultMutableTreeNode)node.getChildAt(childIndex);
				expandTreeRecursive(child, levelsToExpand);
			}
		}
	}
	
	/**
	 * Ensures that all resources are sorted by their resource names.
	 * For any files that couldn't be parsed, so we don't have their names, they should appear at the top of the list, sorted
	 * by file path.
	 */
	private static class SortOptionalWrappedResourcesByFilePath implements Comparator<Map.Entry<String, DefaultMutableTreeNode>> {
		private final Map<String, Optional<WrappedResource<?>>> resourcesByFilePath;

		public SortOptionalWrappedResourcesByFilePath(Map<String, Optional<WrappedResource<?>>> resourcesByFilePath) {
			this.resourcesByFilePath = resourcesByFilePath;
		}
		
		@Override
		public int compare(Entry<String, DefaultMutableTreeNode> o1, Entry<String, DefaultMutableTreeNode> o2) {
			String filepath1 = o1.getKey();
			Optional<WrappedResource<?>> wrappedResource1 = resourcesByFilePath.get(filepath1);
			String filepath2 = o2.getKey();
			Optional<WrappedResource<?>> wrappedResource2 = resourcesByFilePath.get(filepath2);
			
			boolean hasResource1 = wrappedResource1.isPresent();
			boolean hasResource2 = wrappedResource2.isPresent();
			
			if (hasResource1 && !hasResource2) {
				return 1;
			} else if (!hasResource1 && hasResource2) {
				return -1;
			} else if (!hasResource1 && !hasResource2) {
				// order by filepaths
				return filepath1.compareTo(filepath2);
			} else {
				try {
				// hasResource1 && hasResource2
				return wrappedResource1.get().getName().compareTo(wrappedResource2.get().getName());
				} catch (Exception e) {
					LOG.error("Error calling getName() on one or more resources", e);
					return 0;
				}
			}
		}
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
		
		List<Map.Entry<String, DefaultMutableTreeNode>> sortedNodes = 
			nodesByFilePath
				.entrySet()
				.stream()
				.sorted(new SortOptionalWrappedResourcesByFilePath(resourcesByFilePath))
				.collect(Collectors.toList());
		
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
			truncatedFilePath = " (" + trimmedFilePath + ")";
		}
		
		String name;
		try {
			if (resource.isPresent()) {
				name = resource.get().getName();
			} else {
				name = "[unknown resource name]";
			}
		} catch (Exception e) {
			name = "[exception while trying to read resource name]";
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
