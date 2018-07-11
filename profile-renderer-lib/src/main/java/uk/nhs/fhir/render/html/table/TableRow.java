package uk.nhs.fhir.render.html.table;

import java.util.Arrays;
import java.util.List;

import org.jdom2.Element;
import org.jdom2.Attribute;

import com.google.common.collect.Lists;

import uk.nhs.fhir.render.html.Elements;
import uk.nhs.fhir.render.html.cell.TableCell;
import uk.nhs.fhir.render.html.cell.TreeNodeCell;

public class TableRow {
	private List<TableCell> tableCells = Lists.newArrayList();

	public TableRow(TableCell cell) {
		addCell(cell);
	}
	public TableRow(List<TableCell> cells) {
		addCells(cells);
	}
	public TableRow(TableCell... cells) {
		addCells(Arrays.asList(cells));
	}
	
	public void addCell(TableCell cell) {
		tableCells.add(cell);
	}
	
	public void addCells(List<TableCell> cells) {
		tableCells.addAll(cells);
	}
	
	public List<TableCell> getCells() {
		return tableCells;
	}
	
	/*public Element makeRow() {
		List<Element> cells = Lists.newArrayList();
		tableCells.forEach((TableCell cell) -> cells.add(cell.makeCell()));
		return Elements.withChildren("tr", cells);
	}*/
	
	public Element makeRow() {
        List<Element> cells = Lists.newArrayList();
        String parentNode = "-1";
        tableCells.forEach((TableCell cell) -> {
            cells.add(cell.makeCell());
            System.out.println("elements are11:  " +   TreeNodeCell.static_nodeKey);    
        });
        parentNode = "-1";
        System.out.print(TreeNodeCell.static_nodeKey.contains("."));
        if(TreeNodeCell.static_nodeKey.contains("."))
            {
                parentNode =  TreeNodeCell.static_nodeKey.substring(0, TreeNodeCell.static_nodeKey.lastIndexOf("."));
                return Elements.withAttributesAndChildren("tr", Lists.newArrayList( new Attribute("data-id",TreeNodeCell.static_nodeKey),new Attribute("class","treenode collapsed"),new Attribute("data-parentid",parentNode),new Attribute("data-serial-nr",String.valueOf(TreeNodeCell.data_serial_nr))), cells); // Anand added Parent node, data node and data serial nr
            }
        else
            {
                return Elements.withAttributesAndChildren("tr", Lists.newArrayList(new Attribute("class","rootnode"), new Attribute("data-id",TreeNodeCell.static_nodeKey)), cells); // Anand added root node
            }
            
    }
    //return Elements.withChildren("tr", cells); }
}
