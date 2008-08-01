package org.javarosa.formmanager.activity;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.storage.DataModelTreeRMSUtility;
import org.javarosa.core.model.storage.FormDefRMSUtility;
import org.javarosa.core.model.utils.IPreloadHandler;
import org.javarosa.core.util.UnavailableExternalizerException;
import org.javarosa.formmanager.controller.FormEntryController;
import org.javarosa.formmanager.controller.IControllerHost;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.view.IFormEntryView;
import org.javarosa.formmanager.view.IFormEntryViewFactory;

public class FormEntryActivity implements IActivity, IControllerHost, CommandListener {

	/** Alert if the form cannot load **/
	private Alert alert;
	
	/** View for entering data **/
	private IFormEntryView view;
	
	/** View's controller **/
	private FormEntryController controller;
	
	/** The form that is to be displayed to the user, and its values **/
	private FormEntryModel model;

	/** Current running context **/
	private FormEntryContext context;
	
	/** The parent shell **/
	private IShell parent;
	
	private IFormEntryViewFactory viewFactory;
	
	/** Loading error string **/
	private final static String LOAD_ERROR = "Deepest Apologies. The form could not be loaded.";

	public FormEntryActivity(IShell parent, IFormEntryViewFactory viewFactory) {
		this.parent = parent;
		this.viewFactory = viewFactory;
	}
	
	public void contextChanged(Context context) {
		Vector contextChanges = this.context.mergeInContext(context);
		
		Enumeration en = contextChanges.elements();
		while(en.hasMoreElements()) {
			String changedValue = (String)en.nextElement();
			if(changedValue == Constants.USER_KEY) {
				//Do we need to update the username?
			}
		}
	}
	
	public void start (Context context) {
		FormDef theForm = null;
		int instanceID = -1;
		
		if(context instanceof FormEntryContext) {
			this.context = (FormEntryContext)context;			
			instanceID = this.context.getInstanceID();			
			
			//TODO: Are we going to make this non-RMS dependant any any point?
			FormDefRMSUtility formUtil = (FormDefRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(FormDefRMSUtility.getUtilityName());  //whoa!
			theForm = new FormDef();
			try {
				formUtil.retrieveFromRMS(this.context.getFormID(), theForm);
				
				if (instanceID != -1) {
					DataModelTreeRMSUtility modelUtil = (DataModelTreeRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(DataModelTreeRMSUtility.getUtilityName());
					IFormDataModel theModel = new DataModelTree();
					modelUtil.retrieveFromRMS(this.context.getInstanceID(), theModel);
					theForm.setDataModel(theModel);
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			catch (InstantiationException e) {
				e.printStackTrace();
			}
			catch (UnavailableExternalizerException uee) {
				uee.printStackTrace();
			}
		}
		if (theForm != null) {
			if (instanceID == -1) {//only preload new forms (we may have to revisit this)
				Vector handlers = this.context.getPreloadHandlers();
				if(handlers != null) {
					Enumeration en = handlers.elements();
					while(en.hasMoreElements()) {
						theForm.getPreloader().addPreloadHandler((IPreloadHandler)en.nextElement());
					}
					
				}
				theForm.preloadModel();
			}
			if (theForm.getLocalizer() != null && theForm.getLocalizer().getLocale() == null) {
				theForm.getLocalizer().setToDefault();
			}
			
			model = new FormEntryModel(theForm, instanceID);
			controller = new FormEntryController(model, this);
			view = viewFactory.getFormEntryView("chatterbox", model, controller);
			view.setContext(this.context);
			controller.setView(view);
			
			view.show();
		} else {
			displayError(LOAD_ERROR);
		}
	}
	
	
	public void halt () {
		//need to do anything?
		System.out.println("whoa, nelly! we're halting!");
	}
	
	public void resume (Context globalContext) {
		view.show();
	}
	
	public void destroy () {
		
	}
	
	public void setDisplay (Displayable d) {
		parent.setDisplay(this, d);
	}
	
	public void controllerReturn (String status) {
		if ("exit".equals(status)) {
			Hashtable returnArgs = new Hashtable();
			
			returnArgs.put("INSTANCE_ID", new Integer(model.getInstanceID()));
			returnArgs.put("DATA_MODEL", model.getForm().getDataModel());
			returnArgs.put("FORM_COMPLETE", new Boolean(model.isFormComplete()));
			returnArgs.put("QUIT_WITHOUT_SAVING", new Boolean(!model.isSaved()));
			
			parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, returnArgs);
		}
	}
	
	public void commandAction(Command command, Displayable display) {
		if(display == alert) {
			parent.returnFromActivity(this, Constants.ACTIVITY_COMPLETE, null);
		}
	}
	
	private void displayError(String errorMsg) {
		alert = new Alert("Form Entry Error",errorMsg,null,AlertType.ERROR);
		alert.setTimeout(Alert.FOREVER);
		//setView(alert);
		//For some reason that I really can't figure out, this alert won't display the error text
		alert.setCommandListener(this);
	}
}