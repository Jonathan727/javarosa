package org.javarosa.patient.util;

import java.util.Vector;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.utils.IPreloadHandler;
import org.javarosa.patient.model.Patient;
import org.javarosa.patient.model.data.ImmunizationData;
import org.javarosa.patient.model.data.ImmunizationRow;

public class PatientPreloadHandler implements IPreloadHandler {
	
	/** The patient for this handler */
	private Patient patient;
	
	/**
	 * Creates a preload handler that can pull values from
	 * the patient object provided.
	 * 
	 * @param thePatient the patient whose data is to be 
	 * retrevied from this preload handler
	 */
	public PatientPreloadHandler(Patient thePatient) {
		patient = thePatient;
	}
	
	/**
	 * 
	 */
	public IAnswerData handle(String preloadParams) {
		IAnswerData returnVal = null;
		if(preloadParams == "monthsOnTreatment") {
			//TODO: Get actual data from patient
			returnVal = new IntegerData(12);
		} else if(preloadParams == "immunization") {
			//TODO: Load this data in from Patient RMS
			ImmunizationData data = new ImmunizationData();
			Vector rows = new Vector();
			rows.addElement(new ImmunizationRow("BCG"));
			rows.addElement(new ImmunizationRow("OPV"));
			rows.addElement(new ImmunizationRow("DPT"));
			rows.addElement(new ImmunizationRow("Hep B"));
			rows.addElement(new ImmunizationRow("Measles"));
			data.setValue(rows);
			returnVal = data;
		}
		else {
			int selectorStart = preloadParams.indexOf("[");
			if(selectorStart == -1) {
				patient.getRecord(preloadParams);
			} else {
				String type = preloadParams.substring(0, selectorStart -1);
				String selector = preloadParams.substring(selectorStart, preloadParams.length());
				patient.getRecordSet(type, selector);
			}
		}
		return returnVal;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IPreloadHandler#preloadHandled()
	 */
	public String preloadHandled() {
		return "patient";
	}

}
