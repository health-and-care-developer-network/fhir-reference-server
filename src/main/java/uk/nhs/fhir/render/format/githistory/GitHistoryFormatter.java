package uk.nhs.fhir.render.format.githistory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.jdom2.Element;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.extras.OkHttpConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.TableFormatter;
import uk.nhs.fhir.render.format.structdef.StructureDefinitionMetadataFormatter;
import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.cell.CellWithAvatar;
import uk.nhs.fhir.render.html.cell.ExternalLinkCell;
import uk.nhs.fhir.render.html.cell.LinkCell;
import uk.nhs.fhir.render.html.cell.ResourceFlagsCell;
import uk.nhs.fhir.render.html.cell.SimpleTextCell;
import uk.nhs.fhir.render.html.cell.ValueWithInfoCell;
import uk.nhs.fhir.render.html.panel.FhirPanel;
import uk.nhs.fhir.render.html.table.Table;
import uk.nhs.fhir.render.html.table.TableRow;
import uk.nhs.fhir.render.html.table.TableTitle;

public class GitHistoryFormatter extends TableFormatter<WrappedNull> {
	private static final Logger LOG = LoggerFactory.getLogger(GitHistoryFormatter.class.getName());
	
	private final Optional<String> repositoryName;
	private final Optional<String> repositoryBranch;
	private final Optional<String> httpCacheDirectory;
	private final String filename;
	private GHRepository repo = null;
	
	public GitHistoryFormatter(Optional<String> repositoryName,
								Optional<String> repositoryBranch,
								Optional<String> httpCacheDirectory,
								String filename) {
		super(null);
		this.repositoryName = repositoryName;
		this.repositoryBranch = repositoryBranch;
		this.httpCacheDirectory = httpCacheDirectory;
		this.filename = filename;
		
		if (repositoryName.isPresent() && repositoryBranch.isPresent()) {
			LOG.debug("Creating Git connection for file: " + filename);
			GitHub github;
			try {
				if (httpCacheDirectory.isPresent()) {
					Cache cache = new Cache(new File(this.httpCacheDirectory.get()), 10 * 1024 * 1024); // 10MB cache
					github = GitHubBuilder.fromEnvironment()
					    .withConnector(new OkHttpConnector(new OkUrlFactory(new OkHttpClient().setCache(cache))))
						.build();
				} else {
					github = GitHubBuilder.fromEnvironment().build();
				}
				this.repo = github.getRepository(this.repositoryName.get());
			} catch (IOException e) {
				try {
					LOG.info("Unable to connect using Github credentials from the environment, fall back on anonymous access");
					github = GitHub.connectAnonymously();
					this.repo = github.getRepository(this.repositoryName.get());
				} catch (Exception ex) {
					ex.printStackTrace();
					this.repo = null;
				}
			}
		}
	}

	@Override
	public HTMLDocSection makeSectionHTML() {
		
		HTMLDocSection section = new HTMLDocSection();
		
		Element historyPanel;
		try {
			historyPanel = buildHistoryPanel();
			section.addBodyElement(historyPanel);
			addStyles(section);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return section;
	}
	
	private Element buildHistoryPanel() throws IOException {
		List<TableRow> tableRows = Lists.newArrayList();
		
		LOG.debug("Attempting to get git history for file: " + this.filename);
		for (GHCommit commit : repo.queryCommits().from(this.repositoryBranch.get()).path(this.filename).list()) {
			tableRows.add(getCommitRow(commit));
		}
		
		Element historyTable = new Table(getColumns(), tableRows).makeTable();
		Element spacer = Elements.newElement("br");
		Element subText = Elements.withText("p", "Note: The above table shows the complete Git revision history for this file published from branch '"+this.repositoryBranch.get()+"' - there may be multiple revisions listed here for each versioned release of this resource onto the FHIR reference server.");
		Element bodyContent = Elements.withChildren("div", Lists.newArrayList(historyTable, spacer, subText));
		
		return new FhirPanel("Git History For Resource", bodyContent).makePanel();
	}
	
	public List<TableTitle> getColumns() {
		return Lists.newArrayList(
			new TableTitle("Date", "Date of Commit", "15%"),
			//new TableTitle("Author", "Author of change", "15%"),
			new TableTitle("Committer", "User that committed the change", "15%"),
			new TableTitle("Commit Comment", "Comment from Git Commit", "55%"),
			new TableTitle("Commit Details Link", "Link to full details of commit", "15%"));
	}
	
	private TableRow getCommitRow(GHCommit commit) throws IOException {
		TableRow commitRow = new TableRow();
		
		// Date
		commitRow.addCell(new SimpleTextCell(commit.getCommitDate().toString()));
		
		// Author
		/*if (commit.getAuthor() == null) {
			commitRow.addCell(new SimpleTextCell(commit.getCommitShortInfo().getAuthor().getName()));
		} else {
			CellWithAvatar authorCell = new CellWithAvatar(commit.getAuthor().getName());
			if (commit.getAuthor().getAvatarUrl() != null) {
				authorCell.setAvatarUrl(commit.getAuthor().getAvatarUrl());
			}
			commitRow.addCell(authorCell);
		}*/
		
		// Committer
		if (commit.getCommitter() == null) {
			commitRow.addCell(new SimpleTextCell(commit.getCommitShortInfo().getCommitter().getName()));
		} else {
			CellWithAvatar committerCell = new CellWithAvatar(commit.getCommitter().getName());
			if (commit.getCommitter().getAvatarUrl() != null) {
				committerCell.setAvatarUrl(commit.getCommitter().getAvatarUrl());
			}
			commitRow.addCell(committerCell);
		}
		
		// Message
		commitRow.addCell(new SimpleTextCell(commit.getCommitShortInfo().getMessage()));
		
		// Link
		ExternalLinkCell link = new ExternalLinkCell(commit.getHtmlUrl().toString(), commit.getSHA1().substring(0,10)+"...");
		commitRow.addCell(link);
		
		return commitRow;
	}

    public void addStyles(HTMLDocSection section) {
        Table.getStyles().forEach(section::addStyle);
        FhirPanel.getStyles().forEach(section::addStyle);
        ValueWithInfoCell.getStyles().forEach(section::addStyle);
        LinkCell.getStyles().forEach(section::addStyle);
        ResourceFlagsCell.getStyles().forEach(section::addStyle);
        StructureDefinitionMetadataFormatter.getStyles().forEach(section::addStyle);
    }
}
