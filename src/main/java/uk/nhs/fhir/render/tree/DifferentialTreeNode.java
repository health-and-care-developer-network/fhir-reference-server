package uk.nhs.fhir.render.tree;

public class DifferentialTreeNode extends AbstractFhirTreeNode<DifferentialData, DifferentialTreeNode>	 {

	private final DifferentialData data;

	public DifferentialTreeNode(DifferentialData data) {
		this(null, data);
	}

	public DifferentialTreeNode(DifferentialTreeNode parent, DifferentialData data) {
		super(parent);
		this.data = data;
	}

	@Override
	public DifferentialData getData() {
		return data;
	}
	
	public SnapshotTreeNode getBackupNode() {
		return data.getBackupNode();
	}
	
	@Override
	public String getPath() {
		return getData().getPath();
	}

	@Override
	public boolean hasSlicingSibling() {
		return super.hasSlicingSibling()
		  || getBackupNode().hasSlicingSibling();
	}

	@Override
	public String getNodeKey() {
		return getBackupNode().getNodeKey();
	}

}
