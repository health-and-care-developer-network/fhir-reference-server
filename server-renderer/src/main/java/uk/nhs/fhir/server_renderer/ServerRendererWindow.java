package uk.nhs.fhir.server_renderer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.datalayer.FilesystemIF;

@SuppressWarnings("serial")
public class ServerRendererWindow extends JFrame implements RendererListener {
	
	private static final Logger LOG = LoggerFactory.getLogger(ServerRendererWindow.class);
	
	private static final Font BIG_FONT = new Font("Dialog", Font.BOLD, 30);
	private static final double BIG_WINDOW_HEIGHT_FACTOR = 1.5;
	private static final double BIG_WINDOW_WIDTH_FACTOR = 1.5;
	
	private final Path renderedFileDir;
	private final Path importedFileDir;
	private final Path githubCacheDir;
	private final Optional<Path> logFileDir;

	private Optional<Set<String>> allowedMissingExtensionPrefixes;
	private Optional<Set<String>> localDomains;

	public ServerRendererWindow(Path renderedFileDir, Path importedFileDir, Path githubCacheDir, Optional<Path> logFileDir, ServerRendererArgs cliArgs) {
		this.renderedFileDir = renderedFileDir;
		this.importedFileDir = importedFileDir;
		this.githubCacheDir = githubCacheDir;
		this.logFileDir = logFileDir;
	
		boolean largeText = cliArgs.getLargeText();
		this.allowedMissingExtensionPrefixes = cliArgs.getAllowedMissingExtensionPrefixes();
		this.localDomains = cliArgs.getLocalDomains();
		
		initWindow();
		initPanel();
		initButtonActions();
		
		if (largeText) {
			LOG.info("Applying big settings to server/renderer window...");
			LOG.info("Big Font size = " + BIG_FONT.getSize() 
			  + ", Big window height factor = " + BIG_WINDOW_HEIGHT_FACTOR
			  + ", Big window width factor = " + BIG_WINDOW_WIDTH_FACTOR);
			
			applyLargeConfig();
		}
		
		pack();
	}

	private final JPanel mainPanel = new JPanel();

	private final JPanel rendererRootFilePathRow = new JPanel();
	private final JLabel rendererRootFilePathLabel = new JLabel("Renderer root file path:");
	private final JTextField rendererRootfilePathText = new JTextField();
	private final JButton chooseRootDirectoryButton = new JButton("Select...");
	private final JFileChooser rootDirectoryChooser = new JFileChooser();

	private final JPanel buttonsBar = new JPanel();
	private final JButton runRendererButton = new JButton("Run renderer");
	private final JButton clearCacheButton = new JButton("Clear server cache");
	private final JButton exportToZipButton = new JButton("Export rendered items");
	private final JFileChooser exportToZipChooser = new JFileChooser();
	
	private void initWindow() {
		setTitle("Local FHIR Server");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setLayout(new BorderLayout());
	}

	private void applyLargeConfig() {
		setContainerFonts(BIG_FONT, this);
		setContainerFonts(BIG_FONT, rootDirectoryChooser);
		setContainerFonts(BIG_FONT, exportToZipChooser);
		
		enlargeComponent(this);
		enlargeComponent(rootDirectoryChooser);
		enlargeComponent(exportToZipChooser);

		rendererRootfilePathText.setColumns(25);
	}
	
	private void enlargeComponent(Component c) {
		if (c instanceof JLabel 
		  || c instanceof JTextField) {
			// labels already expand according to the font
			return;
		}
		
		Dimension preferredSize = c.getPreferredSize();
		c.setPreferredSize(
			new Dimension(
				(int)(preferredSize.width * BIG_WINDOW_HEIGHT_FACTOR), 
				(int)(preferredSize.height * BIG_WINDOW_WIDTH_FACTOR)));
		
		if (c instanceof Container) {
			for (Component comp : ((Container)c).getComponents()) {
				enlargeComponent(comp);
			}
		}
	}

	/**
	 * Recursively set font on all non-container components
	 */
	private void setContainerFonts(Font font, Container container) {
		for (Component c : container.getComponents()) {
			if (c instanceof Container) {
				setContainerFonts(font, (Container)c);
			}
			try {
				c.setFont(font);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void initPanel() {
		int mainPadding = 4;
		
		rendererRootfilePathText.setEditable(false);
		
		// add horizontal padding to text box
		Box textWrapper = Box.createHorizontalBox();
		textWrapper.add(Box.createHorizontalStrut(mainPadding));
		textWrapper.add(rendererRootfilePathText);
		textWrapper.add(Box.createHorizontalStrut(mainPadding));
		
		// Make text box expand to fill space
		rendererRootFilePathRow.setLayout(new BorderLayout());
		rendererRootFilePathRow.add(rendererRootFilePathLabel, BorderLayout.WEST);
		rendererRootFilePathRow.add(textWrapper, BorderLayout.CENTER);
		rendererRootFilePathRow.add(chooseRootDirectoryButton, BorderLayout.EAST);
		
		// Vertical centering if stretched (though we have now disabled resize, maybe it will be relevant in future).
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints mainCons = new GridBagConstraints();
		mainCons.fill = GridBagConstraints.HORIZONTAL;
		mainCons.weightx = 1;
		mainPanel.add(rendererRootFilePathRow, mainCons);
		
		buttonsBar.setLayout(new FlowLayout());
		buttonsBar.add(runRendererButton);
		buttonsBar.add(clearCacheButton);
		buttonsBar.add(exportToZipButton);
		
		// horizontal and vertical padding around edge of main panel
		Box mainPanelhWrapper = Box.createHorizontalBox();
		mainPanelhWrapper.add(Box.createHorizontalStrut(mainPadding));
		mainPanelhWrapper.add(mainPanel);
		mainPanelhWrapper.add(Box.createHorizontalStrut(mainPadding));
		Box mainPanelvWrapper = Box.createVerticalBox();
		mainPanelvWrapper.add(Box.createVerticalStrut(mainPadding));
		mainPanelvWrapper.add(mainPanelhWrapper);
		mainPanelvWrapper.add(Box.createVerticalStrut(mainPadding));

		this.add(buttonsBar, BorderLayout.SOUTH);
		this.add(mainPanelvWrapper, BorderLayout.CENTER);
	}

	private void initButtonActions() {
		runRendererButton.addActionListener(
			new RunRendererActionListener(
				this,
				new FileChooserFilePathSupplier(rootDirectoryChooser),
				new ConsolerRendererOutputDisplay(),
				renderedFileDir,
				githubCacheDir,
				logFileDir,
				this));
		
		clearCacheButton.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						FileUtils.cleanDirectory(renderedFileDir.toFile());
						FileUtils.cleanDirectory(importedFileDir.toFile());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					FilesystemIF.invalidateCache();
				}
			}); 
		
		rootDirectoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		chooseRootDirectoryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int option = rootDirectoryChooser.showOpenDialog(ServerRendererWindow.this);
				if (option == JFileChooser.APPROVE_OPTION) {
					File rootDirectory = rootDirectoryChooser.getSelectedFile();
					LOG.info("Exporting rendered artefacts to " + rootDirectory.getAbsolutePath());
					rendererRootfilePathText.setText(rootDirectory.getPath());
				}
			}
		});
		
		exportToZipChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		exportToZipChooser.setFileFilter(new FileNameExtensionFilter("ZIP archive", ".zip"));
		
		exportToZipButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int option = exportToZipChooser.showSaveDialog(ServerRendererWindow.this);
				if (option == JFileChooser.APPROVE_OPTION) {
					if (checkContainsFiles(importedFileDir.toFile())) {
						File zip = exportToZipChooser.getSelectedFile();
						Optional<String> errorMessage = new ZipExporter(importedFileDir.toFile()).export(zip);
						
						if (errorMessage.isPresent()) {
							JOptionPane.showMessageDialog(ServerRendererWindow.this, errorMessage.get(), "Export error", JOptionPane.ERROR_MESSAGE);
						}
					} else {
						JOptionPane.showMessageDialog(
							ServerRendererWindow.this, 
							"Nothing available to export",
							"Nothing rendered",
							JOptionPane.WARNING_MESSAGE);
						return;
					}
				}
			}
			
			private boolean checkContainsFiles(File f) {
				for (File child : f.listFiles()) {
					if (child.isFile()) {
						return true;
					} else if (child.isDirectory() 
					  && checkContainsFiles(child)) {
						return true;
					}
				}
				
				return false;
			}
		});
	}

	@Override
	public void startRender() {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				runRendererButton.setEnabled(false);
				runRendererButton.setText("Running");

				chooseRootDirectoryButton.setEnabled(false);
				clearCacheButton.setEnabled(false);
				exportToZipButton.setEnabled(false);
			}});
	}

	@Override
	public void finishRender() {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				runRendererButton.setText("Loading resources");
				chooseRootDirectoryButton.setEnabled(true);
			}});
		
		FilesystemIF.invalidateCache();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				runRendererButton.setEnabled(true);
				runRendererButton.setText("Run renderer");
				
				clearCacheButton.setEnabled(true);
				exportToZipButton.setEnabled(true);
			}});
	}

	public Optional<Set<String>> getAllowedMissingExtensionPrefixes() {
		return allowedMissingExtensionPrefixes;
	}

	public Optional<Set<String>> getLocalDomains() {
		return localDomains;
	}
}
