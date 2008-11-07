package org.javarosa.demo.shell;

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.microedition.midlet.MIDlet;

import org.javarosa.communication.http.HttpTransportProperties;
import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.Constants;
import org.javarosa.core.api.IActivity;
import org.javarosa.core.api.IShell;
import org.javarosa.core.api.IView;
import org.javarosa.core.model.CoreModelModule;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.storage.FormDefRMSUtility;
import org.javarosa.core.services.properties.JavaRosaPropertyRules;
import org.javarosa.core.util.PropertyUtils;
import org.javarosa.core.util.WorkflowStack;
import org.javarosa.formmanager.activity.FormEntryActivity;
import org.javarosa.formmanager.activity.FormEntryContext;
import org.javarosa.formmanager.activity.FormListActivity;
import org.javarosa.formmanager.activity.FormTransportActivity;
import org.javarosa.formmanager.activity.MemoryCheckActivity;
import org.javarosa.formmanager.activity.ModelListActivity;
import org.javarosa.formmanager.properties.FormManagerProperties;
import org.javarosa.formmanager.utility.FormDefSerializer;
import org.javarosa.formmanager.utility.TransportContext;
import org.javarosa.formmanager.view.Commands;
import org.javarosa.j2me.storage.rms.RMSStorageModule;
import org.javarosa.model.xform.XFormSerializingVisitor;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.services.properties.activity.PropertyScreenActivity;
import org.javarosa.user.activity.AddUserActivity;
import org.javarosa.user.activity.LoginActivity;
import org.javarosa.user.model.User;
import org.javarosa.xform.util.XFormUtils;

/**
 * This is the shell for the JavaRosa demo that handles switching all of the views
 * @author Brian DeRenzi
 *
 */
public class JavaRosaDemoShell implements IShell {
	// List of views that are used by this shell
	MIDlet midlet;

	WorkflowStack stack;
	Context context;

	IActivity currentActivity;
	IActivity mostRecentListActivity; //should never be accessed, only checked for type

	public JavaRosaDemoShell() {
		stack = new WorkflowStack();
		context = new Context();
	}

	public void exitShell() {
		midlet.notifyDestroyed();
	}

	public void run() {
		init();
		workflow(null, null, null);
	}

	private void startGCThread () {
        final int GC_INTERVAL = 1000;

        Timer timer = new Timer();
        timer.schedule(new TimerTask () {
            public void run () {
                System.gc();
//                System.out.print("gc attempted:: ");
//                System.out.println(Runtime.getRuntime().freeMemory());
            }
        }, GC_INTERVAL, GC_INTERVAL);
    }

	private void init() {
		loadModules();
		loadProperties();
		startGCThread();

		boolean readSerialized = false;
		boolean genSerialized = false;
		if (genSerialized) {
			generateSerializedForms("/CHMTTL_Help.xhtml");
//			generateSerializedForms("/MobileSurvey.xhtml");
		}

		System.out.println("TOTAL MEM AVAIL: "+java.lang.Runtime.getRuntime().totalMemory());
		System.out.println("PRE LOAD FORM MEM: "+java.lang.Runtime.getRuntime().freeMemory());
		
		FormDefRMSUtility formDef = (FormDefRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(FormDefRMSUtility.getUtilityName());
		if (formDef.getNumberOfRecords() == 0) {
			if (readSerialized ) {
				//load from serialized form.
				FormDef form = new FormDef();
				form = XFormUtils.getFormFromSerializedResource("/CHMTTL.xhtml.serialized");
				//#if debug.output==verbose
				System.out.println("SERIALIZE TEST:");
				System.out.println(form.getName());
				//#endif
				formDef.writeToRMS(form);
				//load from serialized form.
				/*form = new FormDef();
				form = XFormUtils
				.getFormFromSerializedResource("/MobileSurvey.xhtml.serialized");
				//#if debug.output==verbose
				System.out.println("SERIALIZE TEST:");
				System.out.println(form.getName());
				//#endif
				formDef.writeToRMS(form);*/
			}else{
				formDef.writeToRMS(XFormUtils.getFormFromResource("/CHMTTL_Help.xhtml"));
//				formDef.writeToRMS(XFormUtils.getFormFromResource("/CHMTOpenDay2.xhtml"));
//				formDef.writeToRMS(XFormUtils.getFormFromResource("/CHMTTLT2.xhtml"));
//			formDef.writeToRMS(XFormUtils.getFormFromResource("/hmis-a_draft.xhtml"));
//			formDef.writeToRMS(XFormUtils.getFormFromResource("/MobileSurvey.xhtml"));
			}
		}

		System.out.println("POST LOAD FORM MEM: "+java.lang.Runtime.getRuntime().freeMemory());


	}

	private void loadModules() {
		new RMSStorageModule().registerModule(context);
		new XFormsModule().registerModule(context);
		new CoreModelModule().registerModule(context);
		//new HttpTransportModule().registerModule(context);
		//new FormManagerModule().registerModule(context);
	}
		
	private void generateSerializedForms(String originalResource) {
		FormDef a = XFormUtils.getFormFromResource(originalResource);
		FormDefSerializer fds = new FormDefSerializer();
		fds.setForm(a);
		fds.setFname(originalResource+".serialized");
		new Thread(fds).start();
	}

	private void workflow(IActivity lastActivity, String returnCode, Hashtable returnVals) {
		if (returnVals == null)
			returnVals = new Hashtable(); //for easier processing

		if (lastActivity != currentActivity) {
			System.out.println("Received 'return' event from activity other than the current activity" +
					" (such as a background process). Can't handle this yet");
			return;
		}

		if (returnCode == Constants.ACTIVITY_SUSPEND || returnCode == Constants.ACTIVITY_NEEDS_RESOLUTION) {
			stack.push(lastActivity);
			workflowLaunch(lastActivity, returnCode, returnVals);
		} else {
			if (stack.size() > 0) {
				workflowResume(stack.pop(), lastActivity, returnCode, returnVals);
			} else {
				workflowLaunch(lastActivity, returnCode, returnVals);
				if (lastActivity != null)
					lastActivity.destroy();
			}
		}
	}

	private void workflowLaunch (IActivity returningActivity, String returnCode, Hashtable returnVals) {
		if (returningActivity == null) {

		/*	launchActivity(new SplashScreenActivity(this, "/splash.gif"), context);

		} else if (returningActivity instanceof SplashScreenActivity) {
*/
			returningActivity = null;
			//#if javarosa.dev.shortcuts
			launchActivity(new FormListActivity(this, "Forms List"), context);
			//#else
				    	String passwordVAR = midlet.getAppProperty("username");
            String usernameVAR = midlet.getAppProperty("password");
            if ((usernameVAR == null) || (passwordVAR == null))
            {
            context.setElement("username","admin");
            context.setElement("password","adat");
            }
            else{
                    context.setElement("username",usernameVAR);
                    context.setElement("password",passwordVAR);
            }
            context.setElement("authorization", "admin");
			launchActivity(new LoginActivity(this, "Login"), context);
			//#endif

		} else if (returningActivity instanceof LoginActivity) {

			Object returnVal = returnVals.get(LoginActivity.COMMAND_KEY);
			if (returnVal == "USER_VALIDATED") {
				User user = (User)returnVals.get(LoginActivity.USER);

				MemoryCheckActivity memCheck = new MemoryCheckActivity(this);
				if (user != null){
					context.setCurrentUser(user.getUsername());
					context.setElement("USER", user);
				}

				launchActivity(memCheck, context);
			} else if (returnVal == "USER_CANCELLED") {
				exitShell();
			}

		}else if (returningActivity instanceof MemoryCheckActivity) 
		{
			launchActivity(new FormListActivity(this, "Forms List"), context);
		}
		else if (returningActivity instanceof FormListActivity) {

			String returnVal = (String)returnVals.get(FormListActivity.COMMAND_KEY);
			if (returnVal == Commands.CMD_SETTINGS) {
				launchActivity(new PropertyScreenActivity(this), context);
			} else if (returnVal == Commands.CMD_VIEW_DATA) {
				launchActivity(new ModelListActivity(this), context);
			} else if (returnVal == Commands.CMD_SELECT_XFORM) {
				launchFormEntryActivity(context, ((Integer)returnVals.get(FormListActivity.FORM_ID_KEY)).intValue(), -1);
			} else if (returnVal == Commands.CMD_EXIT) {
				exitShell();
			}else if (returnVal == Commands.CMD_ADD_USER) 
				launchActivity( new AddUserActivity(this),context);

		} else if (returningActivity instanceof ModelListActivity) {

			Object returnVal = returnVals.get(ModelListActivity.returnKey);
			if (returnVal == ModelListActivity.CMD_MSGS) {
				launchFormTransportActivity(context, TransportContext.MESSAGE_VIEW, null);
			} else if (returnVal == ModelListActivity.CMD_EDIT) {
				launchFormEntryActivity(context, ((FormDef)returnVals.get("form")).getID(),
						((DataModelTree)returnVals.get("data")).getId());
			} else if (returnVal == ModelListActivity.CMD_SEND) {
				launchFormTransportActivity(context, TransportContext.SEND_DATA, (DataModelTree)returnVals.get("data"));
			} else if (returnVal == ModelListActivity.CMD_BACK) {
				launchActivity(new FormListActivity(this, "Forms List"), context);
			}

		} else if (returningActivity instanceof FormEntryActivity) {

			if (((Boolean)returnVals.get("FORM_COMPLETE")).booleanValue()) {
				launchFormTransportActivity(context, TransportContext.SEND_DATA, (DataModelTree)returnVals.get("DATA_MODEL"));
			} else {
				relaunchListActivity();
			}

		} else if (returningActivity instanceof FormTransportActivity) {

			relaunchListActivity();

			//what is this for?
			/*if (returnCode == Constants.ACTIVITY_NEEDS_RESOLUTION) {
				String returnVal = (String)returnVals.get(FormTransportActivity.RETURN_KEY);
				if(returnVal == FormTransportActivity.VIEW_MODELS) {
					currentActivity = this.modelActivity;
					this.modelActivity.start(context);
				}
			}*/
		}else if (returningActivity instanceof AddUserActivity) 
		 	launchActivity(new FormListActivity(this, "Forms List"), context); 
		
	}

	private void workflowResume (IActivity suspendedActivity, IActivity completingActivity,
								 String returnCode, Hashtable returnVals) {

		//default action
		resumeActivity(suspendedActivity, context);
	}

	private void launchActivity (IActivity activity, Context context) {
		if (activity instanceof FormListActivity || activity instanceof ModelListActivity)
			mostRecentListActivity = activity;

		currentActivity = activity;
		activity.start(context);
	}

	private void resumeActivity (IActivity activity, Context context) {
		currentActivity = activity;
		activity.resume(context);
	}

	private void launchFormEntryActivity (Context context, int formID, int instanceID) {
		FormEntryActivity entryActivity = new FormEntryActivity(this, new FormEntryViewFactory());
		FormEntryContext formEntryContext = new FormEntryContext(context);
		formEntryContext.setFormID(formID);
		if (instanceID != -1)
			formEntryContext.setInstanceID(instanceID);
		launchActivity(entryActivity, formEntryContext);
	}

	private void launchFormTransportActivity (Context context, String task, DataModelTree data) {
		FormTransportActivity formTransport = new FormTransportActivity(this);
		formTransport.setDataModelSerializer(new XFormSerializingVisitor());
		formTransport.setData(data); //why isn't this going in the context?
		TransportContext msgContext = new TransportContext(context);
		msgContext.setRequestedTask(task);

		launchActivity(formTransport, msgContext);
	}

	private void relaunchListActivity () {
		if (mostRecentListActivity instanceof FormListActivity) {
			launchActivity(new FormListActivity(this, "Forms List"), context);
		} else if (mostRecentListActivity instanceof ModelListActivity) {
			launchActivity(new ModelListActivity(this), context);
		} else {
			throw new IllegalStateException("Trying to resume list activity when no most recent set");
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.shell.IShell#activityCompeleted(org.javarosa.activity.IActivity)
	 */
	public void returnFromActivity(IActivity activity, String returnCode, Hashtable returnVals) {
		//activity.halt(); //i don't think this belongs here? the contract reserves halt for unexpected halts;
						   //an activity calling returnFromActivity isn't halting unexpectedly
		workflow(activity, returnCode, returnVals);
	}

	public boolean setDisplay(IActivity callingActivity, IView display) {
		if(callingActivity == currentActivity) {
			JavaRosaServiceProvider.instance().getDisplay().setView(display);
			return true;
		}
		else {
			//#if debug.output==verbose
			System.out.println("Activity: " + callingActivity + " attempted, but failed, to set the display");
			//#endif
			return false;
		}
	}

	public void setMIDlet(MIDlet midlet) {
		this.midlet = midlet;
	}

	//need 'addpropery' too.
	private String initProperty(String propName, String defaultValue) {
		Vector propVal = JavaRosaServiceProvider.instance().getPropertyManager().getProperty(propName);
		if (propVal == null || propVal.size() == 0) {
			propVal = new Vector();
			propVal.addElement(defaultValue);
			JavaRosaServiceProvider.instance().getPropertyManager().setProperty(propName, propVal);
			//#if debug.output==verbose
			System.out.println("No default value for [" + propName
					+ "]; setting to [" + defaultValue + "]"); // debug
			//#endif
			return defaultValue;
		}/*else {
			propVal.addElement(defaultValue);
			JavaRosaServiceProvider.instance().getPropertyManager().setProperty(propName, propVal);
			//#if debug.output==verbose
			System.out.println("added value for [" + propName
					+ "]; setting to [" + defaultValue + "]"); // debug
			//#endif
			return defaultValue;
		}*/
		return (String) propVal.elementAt(0);
	}

	private void loadProperties() {
		JavaRosaServiceProvider.instance().getPropertyManager().addRules(new JavaRosaPropertyRules());
		JavaRosaServiceProvider.instance().getPropertyManager().addRules(new HttpTransportProperties());
		JavaRosaServiceProvider.instance().getPropertyManager().addRules(new FormManagerProperties());

		initProperty("DeviceID", PropertyUtils.genGUID(25));
		initProperty(FormManagerProperties.VIEW_TYPE_PROPERTY, FormManagerProperties.VIEW_CLFORMS);
		initProperty(HttpTransportProperties.POST_URL_LIST_PROPERTY, "http://survey.cell-life.org/admin/post2limeNew.php");
		Vector v = JavaRosaServiceProvider.instance().getPropertyManager().getProperty(HttpTransportProperties.POST_URL_LIST_PROPERTY);
		v.addElement("http://dev.cell-life.org/javarosa/web/limesurvey/admin/post2lime.php");
		JavaRosaServiceProvider.instance().getPropertyManager().setProperty(HttpTransportProperties.POST_URL_LIST_PROPERTY, v);
//		initProperty(HttpTransportProperties.POST_URL_LIST_PROPERTY, "http://dev.cell-life.org/javarosa/web/limesurvey/admin/post2lime.php");
//		initProperty(HttpTransportProperties.POST_URL_PROPERTY, "http://dev.cell-life.org/javarosa/web/limesurvey/admin/post2lime.php");
		initProperty(HttpTransportProperties.POST_URL_PROPERTY, "http://survey.cell-life.org/admin/post2limeNew.php");
		//		initProperty(HttpTransportProperties.POST_URL_LIST_PROPERTY, "http://update.cell-life.org/save_dump_org.php");
//		initProperty(HttpTransportProperties.POST_URL_PROPERTY, "http://update.cell-life.org/save_dump_org.php");
	//	initProperty(FormManagerProperties.VIEW_TYPE_PROPERTY, FormManagerProperties.VIEW_CLFORMS);
//		initProperty(HttpTransportProperties.POST_URL_LIST_PROPERTY, "http://openrosa.org/testsubmit.html");
//		initProperty(HttpTransportProperties.POST_URL_PROPERTY, "http://openrosa.org/testsubmit.html");
	}
}