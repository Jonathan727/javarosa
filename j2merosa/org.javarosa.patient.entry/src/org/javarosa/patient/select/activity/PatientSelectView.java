package org.javarosa.patient.select.activity;

import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;

import org.javarosa.core.api.IView;
import org.javarosa.patient.entry.activity.util.ClickableContainer;
import org.javarosa.patient.entry.activity.util.IClickEventListener;

import de.enough.polish.ui.FramedForm;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemCommandListener;
import de.enough.polish.ui.ItemStateListener;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.TextField;

public class PatientSelectView extends FramedForm implements IView, ItemStateListener, CommandListener, IClickEventListener, ItemCommandListener {
	//TODO: NO. WRONG. Polish Pre-processors should not be a neccesity for the code to not have
	//errors in it.
	
	//#if javarosa.patientselect.formfactor == nokia-s40
	private static final int MAX_ROWS_ON_SCREEN = 5;
	private static final int SCROLL_INCREMENT = 3;	
	//#else
	private static final int MAX_ROWS_ON_SCREEN = 11;
	private static final int SCROLL_INCREMENT = 5;	
	//#endif
	
	public static final int NEW_DISALLOWED = 0;
	public static final int NEW_IN_LIST = 1;
	public static final int NEW_IN_MENU = 2;
	
	private static final int INDEX_NEW = -1;
	
	//behavior configuration options
	public boolean sortByName = true; //if false, sort by ID
	public boolean wrapAround = false; //TODO: support this
	public int newType = NEW_IN_LIST;
	
	private PatientSelectActivity controller;
	public String entityType;
	
	private TextField tf;
	//private Container list;
	private Command exitCmd;
	private Command sortCmd;
    private Command newCmd;
	
	private int firstIndex;
	private int selectedIndex;
		
	private Vector rowIDs; //index into data corresponding to current matches
		
	public PatientSelectView(PatientSelectActivity controller, String title) {
		super(title);
		
		this.controller = controller;

		tf = new TextField("Find:  ", "", 20, TextField.ANY);
		tf.setInputMode(TextField.MODE_UPPERCASE);
		tf.setItemStateListener(this);
		
		//list = new Container(false);
		//list.setAppearanceMode(Item.INTERACTIVE);
		
        append(Graphics.BOTTOM, tf);
        //Jan 21, 2009 - csims@dimagi.com
        //Changed from Graphics.VCENTER because that isn't actually a valid
        //choice. Only top, bottom, left, and right are.
        //append(Graphics.TOP, list);
        
        exitCmd = new Command("Cancel", Command.CANCEL, 4);
        sortCmd = new Command("Sort", Command.SCREEN, 3);
        addCommand(exitCmd);
        addCommand(sortCmd);
        this.setCommandListener(this);
        
        rowIDs = new Vector();
	}

	public void init () {
        selectedIndex = 0;
        firstIndex = 0;

        //can't go in constructor, as entityType is not set there yet
        if (newType == NEW_IN_MENU) {
        	newCmd = new Command("New " + entityType, Command.SCREEN, 4);
        	addCommand(newCmd);
        }
        
        refresh();
	}
	
	public void refresh () {
		refresh(-1);
	}
	
	public void refresh (int selectedEntity) {
		if (selectedEntity == -1)
			selectedEntity = getSelectedEntity();
		
        getMatches(tf.getText());
        selectEntity(selectedEntity);
        refreshList();
	}
	
	public void show () {
		this.setActiveFrame(Graphics.BOTTOM);
		controller.setView(this);
	}
	
	public Object getScreenObject() {
		return this;
	}

	private void getMatches (String key) {
		rowIDs = controller.search(key);
		sortRows();
		if (newType == NEW_IN_LIST) {
			rowIDs.addElement(new Integer(INDEX_NEW));
		}
	}

	private void stepIndex (boolean increment) {
		selectedIndex += (increment ? 1 : -1);
		if (selectedIndex < 0) {
			selectedIndex = 0;
		} else if (selectedIndex >= rowIDs.size()) {
			selectedIndex = rowIDs.size() - 1;
		}
		
		if (selectedIndex < firstIndex) {
			firstIndex -= SCROLL_INCREMENT;
			if (firstIndex < 0)
				firstIndex = 0;
		} else if (selectedIndex >= firstIndex + MAX_ROWS_ON_SCREEN) {
			firstIndex += SCROLL_INCREMENT;
			//don't believe i need to do any clipping in this case
		}
	}
		
	private int getSelectedEntity () {
		int selectedEntityID = -1;
		
		//save off old selected item
		if (!listIsEmpty()) {
			int rowID = rowID(selectedIndex);
			if (rowID != INDEX_NEW) {	
				selectedEntityID = controller.getRecordID(rowID(selectedIndex));
			}
		}

		return selectedEntityID;
	}
	
	private boolean listIsEmpty () {
		return rowIDs.size() == 0 || (rowIDs.size() == 1 && newType == NEW_IN_LIST);
	}
	
	private int rowID (int i) {
		return ((Integer)rowIDs.elementAt(i)).intValue();
	}
		
	private void selectEntity (int entityID) {
		//if old selected item is in new search result, select it, else select first match
		selectedIndex = 0;
		if (entityID != -1) {
			for (int i = 0; i < rowIDs.size(); i++) {
				int rowID = rowID(i);
				if (rowID != INDEX_NEW) {
					if (controller.getRecordID(rowID) == entityID) {
						selectedIndex = i;
					}
				}
			}
		}
		//position selected item in center of visible list
		firstIndex = selectedIndex - MAX_ROWS_ON_SCREEN / 2;
		if (firstIndex < 0)
			firstIndex = 0;
	}
	
	private void refreshList () {
		if(this.container != null){
			this.container.clear();
		}
		
		if (listIsEmpty()) {
			this.append( new StringItem("", "(No matches)"));
		}
		
		for (int i = firstIndex; i < rowIDs.size() && i < firstIndex + MAX_ROWS_ON_SCREEN; i++) {
			ClickableContainer row;
			int rowID = rowID(i);
			
			if (i == selectedIndex) {
				//#style patselSelectedRow
				row = new ClickableContainer(false);			
			} else if (i % 2 == 0) {
				//#style patselEvenRow
				row = new ClickableContainer(false);
			} else {
				//#style patselOddRow
				row = new ClickableContainer(false);
			}
			
			if (rowID == INDEX_NEW) {
				row.add(new StringItem("", "Add New " + entityType));
			} else {
				String[] rowData = controller.getDataFields(rowID);
				
				for (int j = 0; j < rowData.length; j++) {
					//#style patselCell
					StringItem str = new StringItem("", rowData[j]);
					row.add(str);
				}
			}
			//row.setClickEventListener(this);
			//row.setItemCommandListener(this);
			this.append(row);
			row.setId(rowID);
		}
		setActiveFrame(Graphics.BOTTOM);
	}

	protected boolean handleKeyReleased(int keyCode, int gameAction) {
		if (gameAction == Canvas.UP && keyCode != Canvas.KEY_NUM2) {
			stepIndex(false);
			refreshList();
			return true;
		} else if (gameAction == Canvas.DOWN && keyCode != Canvas.KEY_NUM8) {
			stepIndex(true);
			refreshList();
			return true;
		} else if (gameAction == Canvas.FIRE && keyCode != Canvas.KEY_NUM5) {
			int rowID = rowID(selectedIndex);
			if (rowID == INDEX_NEW) {
				controller.newEntity();
			} else {
				controller.itemSelected(rowID);
			}
			return true;
		}
		
		return super.handleKeyReleased(keyCode, gameAction);
	}
	
	public void itemStateChanged (Item item) {
		if (item == tf) {
			refresh();
		}
	}	
	
	public void changeSort (boolean sortByName) {
		this.sortByName = sortByName;
		refresh();
	}
	
	//can't believe i'm writing a fucking sort function
	private void sortRows () {
		for (int i = rowIDs.size() - 1; i > 0; i--) {
			for (int j = 0; j < i; j++) {
				int rowA = rowID(j);
				int rowB = rowID(j + 1);
				String keyA, keyB;
				if (sortByName) {
					keyA = controller.getDataName(rowA);
					keyB = controller.getDataName(rowB);
				} else {
					keyA = controller.getDataID(rowA);
					keyB = controller.getDataID(rowB);
				}
				if (keyA.compareTo(keyB) > 0) {
					rowIDs.setElementAt(new Integer(rowB), j);
					rowIDs.setElementAt(new Integer(rowA), j + 1);
				}
			}
		}
	}

	public void commandAction(Command cmd, Displayable d) {
		if (d == this) {
			if (cmd == exitCmd) {
				controller.exit();
			} else if (cmd == sortCmd) {
				PatientSelectSortPopup pssw = new PatientSelectSortPopup(this, controller);
				pssw.show();
			} else if (cmd == newCmd) {
				controller.newEntity();
			}
		}
	}

	public void clicked(ClickableContainer container) {
		int rowID = container.getId();
		if (rowID == INDEX_NEW) {
			controller.newEntity();
		} else {
			controller.itemSelected(rowID);
		}
	}	
	
	//#if polish.hasPointerEvents
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Container#handlePointerPressed(int, int)
	 */
	protected boolean handlePointerPressed(int x, int y) {
		boolean handled = false;
		//Item item = this.container.getItemAt(this.container.getAbsoluteX() + x, this.container.getAbsoluteY() + y);
		for(int i = 0 ; i < this.container.size(); ++i) {
			//if(this.container.isInItemArea(this.container.getAbsoluteX() + x, this.container.getAbsoluteY() + y, this.container.getItems()[i] )) {
			if(this.container.isInItemArea(x - this.container.getAbsoluteX(), y - this.container.getAbsoluteY(), this.container.getItems()[i] )) {
				if(this.container.getItems()[i] instanceof ClickableContainer) {
					clicked(((ClickableContainer)this.container.getItems()[i]));
					handled = true;
				}
			}
		}
		if (handled){
			return true;
		} else {
			return super.handlePointerPressed(x, y);
		}
	}
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Container#handlePointerReleased(int, int)
	 */
	protected boolean handlePointerReleased(int arg0, int arg1) {
		Item[] items = this.container.getItems();
		for(int i = 0; i < items.length ; ++i ) {
			((ClickableContainer)items[i]).disarm();
		}
		return super.handlePointerReleased(arg0, arg1);
	}
	//#endif

	public void commandAction(Command c, Item item) {
		System.out.println("This sucks");
	}
}	
