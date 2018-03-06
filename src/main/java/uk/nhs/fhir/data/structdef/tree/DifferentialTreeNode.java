package uk.nhs.fhir.data.structdef.tree;

public class DifferentialTreeNode extends AbstractFhirTreeNode<DifferentialData, DifferentialTreeNode> implements MaybeDummy {

	private final boolean isDummy;

	public DifferentialTreeNode(DifferentialData data) {
		this(data, null);
	}

	public DifferentialTreeNode(DifferentialData data, DifferentialTreeNode parent) {
		this(data, parent, false);
	}

	public DifferentialTreeNode(DifferentialData data, DifferentialTreeNode parent, boolean isDummy) {
		super(data, parent);
		this.isDummy = isDummy;
	}
	
	public SnapshotTreeNode getBackupNode() {
		return getData().getBackupNode();
	}
	
	@Override
	public String getPath() {
		return getData().getPath();
	}

	@Override
	public String getNodeKey() {
		return getBackupNode().getNodeKey();
	}

	@Override
	public boolean isDummy() {
		return isDummy;
	}

	@Override
	public DifferentialTreeNode cloneShallow(DifferentialTreeNode newParent) {
		return new DifferentialTreeNode(getData(), newParent, isDummy);
	}
}
