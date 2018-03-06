package uk.nhs.fhir.data.structdef.tree;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;

public class SnapshotTreeNode extends AbstractFhirTreeNode<SnapshotData, SnapshotTreeNode> {

	public SnapshotTreeNode(SnapshotData data) {
		super(data);
	}

	public SnapshotTreeNode(SnapshotTreeNode parent, SnapshotData data) {
		super(data, parent);
	}
	
	@Override
	public String getPath() {
		return getData().getPath();
	}

	public String getNodeKey() {
		Deque<String> ancestorKeys = new LinkedList<>();
		
		for (SnapshotTreeNode ancestor = this; ancestor != null; ancestor = ancestor.getParent()) {
			ancestorKeys.addFirst(ancestor.getKeySegment());
		}
		
		String key = String.join(".", ancestorKeys);
		return key;
	}

	public String getKeySegment() {
		String nodeKey = getData().getPathName();
		
		// Sliced nodes need disambiguating for the details page otherwise we cannot link each element to its
		// unique details entry.
		if (getData().getDiscriminatorValue().isPresent()) {
			// Name is generally more readable and shorter than resolved slicing info, so prioritise that.
			// Slicing discriminator information should always be available as a backup.
			String alias;
			if (getData().getSliceName().isPresent()) {
				alias = getData().getSliceName().get();
			} else if (getData().getName().isPresent()) {
				alias = getData().getName().get();
			} else {
				alias = getData().getDiscriminatorValue().get();
			}
			
			nodeKey += "(" + alias + ")";
		}
		
		return nodeKey;
	}
	
	@Override
	public String toString() {
		Optional<String> sliceName = getData().getSliceName();
		if (sliceName.isPresent()) {
			return getPath() + ":" + sliceName.get();
		} else {
			return getPath();
		}
	}

	@Override
	public SnapshotTreeNode cloneShallow(SnapshotTreeNode newParent) {
		return new SnapshotTreeNode(newParent, getData());
	}

}
