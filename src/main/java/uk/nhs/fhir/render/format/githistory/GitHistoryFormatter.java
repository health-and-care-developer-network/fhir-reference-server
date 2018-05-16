package uk.nhs.fhir.render.format.githistory;

import java.io.IOException;
import java.util.List;

import org.jdom2.Element;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import uk.nhs.fhir.data.wrap.WrappedNull;
import uk.nhs.fhir.render.format.HTMLDocSection;
import uk.nhs.fhir.render.format.TableFormatter;
import uk.nhs.fhir.render.format.structdef.StructureDefinitionMetadataFormatter;
import uk.nhs.fhir.render.html.cell.CellWithAvatar;
import uk.nhs.fhir.render.html.cell.ExternalLinkCell;
import uk.nhs.fhir.render.html.cell.LinkCell;
import uk.nhs.fhir.render.html.cell.ResourceFlagsCell;
import uk.nhs.fhir.render.html.cell.SimpleTextCell;
import uk.nhs.fhir.render.html.cell.ValueWithInfoCell;
import uk.nhs.fhir.render.html.panel.FhirPanel;
import uk.nhs.fhir.render.html.table.Table;
import uk.nhs.fhir.render.html.table.TableRow;

public class GitHistoryFormatter extends TableFormatter<WrappedNull> {
	private static final Logger LOG = LoggerFactory.getLogger(GitHistoryFormatter.class.getName());
	
	protected String repoName = null;
	protected String filename = null;
	protected String cacheDir = null;
	private GHRepository repo = null;
	
	public GitHistoryFormatter(String repoName, String filename, String cacheDir) {
		super(null);
		this.repoName = repoName;
		this.filename = filename;
		this.cacheDir = cacheDir;
		
		try {
			/*
			Cache cache = new Cache(new File(cacheDir), 10 * 1024 * 1024); // 10MB cache
			GitHub github = GitHubBuilder.fromCredentials()
			    .withConnector(new OkHttpConnector(new OkUrlFactory(new OkHttpClient().setCache(cache))))
				.build();
			*/
			GitHub github = GitHub.connectAnonymously();
			repo = github.getRepository(this.repoName);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.repo = null;
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
		GitHistoryTableDataProvider tableData = new GitHistoryTableDataProvider(this.repoName, this.filename);
		
		List<TableRow> tableRows = Lists.newArrayList();
		
		for (GHCommit commit : repo.queryCommits().path(this.filename).list()) {
			LOG.info("Attempting to get git history for file: " + this.filename);
			tableRows.add(getCommitRow(commit));
		}
		
		Element historyTable = new Table(tableData.getColumns(), tableRows).makeTable();
		return new FhirPanel("Git History for: "+this.filename, historyTable).makePanel();
	}
	
	private TableRow getCommitRow(GHCommit commit) throws IOException {
		TableRow commitRow = new TableRow();
		
		// Author
		if (commit.getAuthor() == null) {
			commitRow.addCell(new SimpleTextCell(commit.getCommitShortInfo().getAuthor().getName()));
		} else {
			CellWithAvatar authorCell = new CellWithAvatar(commit.getAuthor().getName());
			if (commit.getAuthor().getAvatarUrl() != null) {
				authorCell.setAvatarUrl(commit.getAuthor().getAvatarUrl());
			}
			commitRow.addCell(authorCell);
		}
		
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
		ExternalLinkCell link = new ExternalLinkCell(commit.getHtmlUrl().toString(), commit.getSHA1().substring(0,10));
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
