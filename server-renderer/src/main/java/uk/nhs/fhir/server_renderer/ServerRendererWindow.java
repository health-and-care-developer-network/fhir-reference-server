package uk.nhs.fhir.server_renderer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import javax.swing.BoxLayout;
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
	
	private final Path renderedFileDir;
	private final Path importedFileDir;
	private final Path githubCacheDir;
	private final Optional<Path> logFileDir;

	public ServerRendererWindow(Path renderedFileDir, Path importedFileDir, Path githubCacheDir, Optional<Path> logFileDir) {
		this.renderedFileDir = renderedFileDir;
		this.importedFileDir = importedFileDir;
		this.githubCacheDir = githubCacheDir;
		this.logFileDir = logFileDir;
		
		initWindow();
		initPanel();
		initButtonActions();
		
		pack();
	}

	private final JPanel main = new JPanel();
	
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
		setLayout(new BorderLayout());
	}

	private void initPanel() {
		main.setLayout(new BorderLayout());
		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		int filePathPreferredHeight = rendererRootfilePathText.getPreferredSize().height;
		rendererRootfilePathText.setPreferredSize(new Dimension(300, filePathPreferredHeight));
		rendererRootfilePathText.setEditable(false);
		
		rendererRootFilePathRow.setLayout(new FlowLayout(FlowLayout.LEFT));
		rendererRootFilePathRow.add(rendererRootFilePathLabel);
		rendererRootFilePathRow.add(rendererRootfilePathText);
		rendererRootFilePathRow.add(chooseRootDirectoryButton);
		mainPanel.add(rendererRootFilePathRow);
		main.add(mainPanel, BorderLayout.CENTER);
		
		buttonsBar.setLayout(new FlowLayout());
		buttonsBar.add(runRendererButton);
		buttonsBar.add(clearCacheButton);
		buttonsBar.add(exportToZipButton);
		main.add(buttonsBar, BorderLayout.SOUTH);
		
		this.add(main, BorderLayout.CENTER);
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
					if (importedFileDir.toFile().list().length > 0) {
						File zip = exportToZipChooser.getSelectedFile();
						Optional<String> errorMessage = new ZipExporter(importedFileDir.toFile()).export(zip);
						
						if (errorMessage.isPresent()) {
							JOptionPane.showMessageDialog(ServerRendererWindow.this, errorMessage.get(), "Export error", JOptionPane.ERROR_MESSAGE);
						}
					} else {
						JOptionPane.showMessageDialog(
							ServerRendererWindow.this, 
							"Nothing has been rendered yet",
							"Nothing rendered",
							JOptionPane.WARNING_MESSAGE);
						return;
					}
				}
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
}
