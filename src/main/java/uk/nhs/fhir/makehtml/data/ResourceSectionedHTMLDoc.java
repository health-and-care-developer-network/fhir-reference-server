package uk.nhs.fhir.makehtml.data;

import uk.nhs.fhir.util.SectionedHTMLDoc;

/**
 * Created by kevinmayfield on 08/05/2017.
 */
public class ResourceSectionedHTMLDoc {
    SectionedHTMLDoc treeView;
    SectionedHTMLDoc bindings;

    public SectionedHTMLDoc getTreeView() {
        return this.treeView;
    }

    public SectionedHTMLDoc getBindings() {
        return this.bindings;
    }

    public SectionedHTMLDoc setTreeView(SectionedHTMLDoc myTreeView) {
        this.treeView = myTreeView;
        return this.treeView;
    }

    public SectionedHTMLDoc setBindings(SectionedHTMLDoc myBindings) {
        this.bindings = myBindings;
        return this.bindings;
    }


}
