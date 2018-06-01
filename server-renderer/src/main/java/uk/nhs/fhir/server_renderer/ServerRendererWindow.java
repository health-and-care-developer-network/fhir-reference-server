package uk.nhs.fhir.server_renderer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.nhs.fhir.datalayer.FilesystemIF;

@SuppressWarnings("serial")
public class ServerRendererWindow extends JFrame implements RendererListener {
	
	private static final Logger LOG = LoggerFactory.getLogger(ServerRendererWindow.class);

	private static final String SYS_PROP_BIG_FONT_SIZE = "BigFontSize";
	private static final String SYS_PROP_HEIGHT_SCALE = "BigHeightScale";
	private static final String SYS_PROP_WIDTH_SCALE = "BigWidthScale";

	private static final Optional<Integer> BIG_FONT_SIZE = Optional.ofNullable(System.getProperty(SYS_PROP_BIG_FONT_SIZE)).map(Integer::parseInt);
	private static final Optional<Double> BIG_HEIGHT_SCALE = Optional.ofNullable(System.getProperty(SYS_PROP_HEIGHT_SCALE)).map(Double::parseDouble);
	private static final Optional<Double> BIG_WIDTH_SCALE = Optional.ofNullable(System.getProperty(SYS_PROP_WIDTH_SCALE)).map(Double::parseDouble);
	private static final Font BIG_FONT = new Font("Dialog", Font.BOLD, BIG_FONT_SIZE.isPresent() ? BIG_FONT_SIZE.get() : 50);
	private static final double BIG_WINDOW_HEIGHT_FACTOR = BIG_HEIGHT_SCALE.isPresent() ? BIG_HEIGHT_SCALE.get() : 1.5;
	private static final double BIG_WINDOW_WIDTH_FACTOR = BIG_WIDTH_SCALE.isPresent() ? BIG_WIDTH_SCALE.get() : 1.5;
	
	private final Path renderedFileDir;
	private final Path importedFileDir;
	private final Path githubCacheDir;
	private final Optional<Path> logFileDir;

	public ServerRendererWindow(Path renderedFileDir, Path importedFileDir, Path githubCacheDir, Optional<Path> logFileDir, ServerRendererArgs cliArgs) {
		this.renderedFileDir = renderedFileDir;
		this.importedFileDir = importedFileDir;
		this.githubCacheDir = githubCacheDir;
		this.logFileDir = logFileDir;
		
		boolean largeText = cliArgs.getLargeText();
		
		initWindow(largeText);
		initPanel();
		initButtonActions();
		
		if (largeText) {
			LOG.info("Applying big settings to server/renderer window...");
			LOG.info("Big Font size = " + BIG_FONT.getSize());
			LOG.info("Big window height factor = " + BIG_WINDOW_HEIGHT_FACTOR);
			LOG.info("Big window width factor = " + BIG_WINDOW_WIDTH_FACTOR);
			
			applyLargeConfig();
		}
		
		pack();
	}

	private void applyLargeConfig() {
		setContainerFonts(BIG_FONT, main);
		setContainerFonts(BIG_FONT, rootDirectoryChooser);
		
		enlargeContainer(main);
		enlargeContainer(rootDirectoryChooser);
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
	
	private void initWindow(boolean largeText) {
		setTitle("Local FHIR Server");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
	}
	
	private void enlargeContainer(Container c) {
		Dimension preferredSize = c.getPreferredSize();
		c.setPreferredSize(
			new Dimension(
				(int)(preferredSize.width * BIG_WINDOW_HEIGHT_FACTOR), 
				(int)(preferredSize.height * BIG_WINDOW_WIDTH_FACTOR)));
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
				githubCacheDir,
				logFileDir,
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

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				runRendererButton.setEnabled(false);
				runRendererButton.setText("Running");

				chooseRootDirectoryButton.setEnabled(false);
				clearCacheButton.setEnabled(false);
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
			}});
	}
}
