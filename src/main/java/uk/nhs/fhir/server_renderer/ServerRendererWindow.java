package uk.nhs.fhir.server_renderer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.io.FileUtils;

import uk.nhs.fhir.datalayer.FilesystemIF;

@SuppressWarnings("serial")
public class ServerRendererWindow extends JFrame implements RendererListener {
	private final Path renderedFileDir;
	private final Path importedFileDir;

	public ServerRendererWindow(Path renderedFileDir, Path importedFileDir) {
		this.renderedFileDir = renderedFileDir;
		this.importedFileDir = importedFileDir;
		
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
				this));
		
		clearCacheButton.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					FilesystemIF.clearCache();
					try {
						FileUtils.cleanDirectory(renderedFileDir.toFile());
						FileUtils.cleanDirectory(importedFileDir.toFile());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
		
		rootDirectoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		chooseRootDirectoryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int option = rootDirectoryChooser.showOpenDialog(ServerRendererWindow.this);
				if (option == JFileChooser.APPROVE_OPTION) {
			       File rootDirectory = rootDirectoryChooser.getSelectedFile();
			       rendererRootfilePathText.setText(rootDirectory.getPath());
				}
			}
		});
	}

	@Override
	public void startRender() {
		runRendererButton.setEnabled(false);
		runRendererButton.setText("Running");

		chooseRootDirectoryButton.setEnabled(false);
		clearCacheButton.setEnabled(false);
	}

	@Override
	public void finishRender() {
		FilesystemIF.invalidateCache();
		
		runRendererButton.setEnabled(true);
		runRendererButton.setText("Run renderer");
		
		chooseRootDirectoryButton.setEnabled(true);
		clearCacheButton.setEnabled(true);
	}
}
