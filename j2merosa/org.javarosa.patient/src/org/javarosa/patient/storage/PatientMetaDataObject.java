package org.javarosa.patient.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.services.storage.utilities.MetaDataObject;
import org.javarosa.core.util.externalizable.ExternalizableHelperDeprecated;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.patient.model.Patient;

public class PatientMetaDataObject extends MetaDataObject {

    private String patientName = ""; //the name of the FormData being referenced
	private int patientRecordId;
	private String patientId;

    
    public String toString(){
    	return new String (super.toString()+" name: "+this.patientName + " patientId: " + patientId);
    }
    
    /**
     * Creates an Empty meta data object
     */
    public PatientMetaDataObject()
    {
        
    }
    
    /**
     * Creates a meta data object for the patient data object given.
     * 
     * @param form The patient whose meta data this object will become
     */
    public PatientMetaDataObject(Patient patient)
    {
        this.patientName = patient.getName();
        this.patientRecordId = patient.getRecordId();
        this.patientId = patient.getPatientIdentifier();
        
    }
    
    /**
     * @param name Sets the name for the form this meta data object represents
     */
    public void setName(String name)
    {
        this.patientName = name;
    }
    
    /**
     * @return the name of the form represented by this meta data
     */
    public String getName()
    {
       return this.patientName; 
    }
    
    /**
     * @return the RMS Storage Id for the form this meta data represents
     */
    public int getPatientRecordId() {
    	return patientRecordId;
    }
    
    /** 
     * @return The Patient ID for the patient that this meta data represents
     */
    public String getPatientId() {
    	return patientId;
    }
    
    /* (non-Javadoc)
     * @see org.javarosa.clforms.storage.MetaDataObject#readExternal(java.io.DataInputStream)
     */
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
    	super.readExternal(in, pf);
    	
        this.patientRecordId = in.readInt();
        
        this.patientName = in.readUTF();
        this.patientId = ExternalizableHelperDeprecated.readUTF(in);
    }

   
    /* (non-Javadoc)
     * @see org.javarosa.clforms.storage.MetaDataObject#writeExternal(java.io.DataOutputStream)
     */
    public void writeExternal(DataOutputStream out) throws IOException
    {
    	super.writeExternal(out);
    	
    	out.writeInt(this.getPatientRecordId());
    	
    	out.writeUTF(this.getName());
    	ExternalizableHelperDeprecated.writeUTF(out, this.patientId);
    }

    /*
     * (non-Javadoc)
     * @see org.javarosa.core.services.storage.utilities.MetaDataObject#setMetaDataParameters(java.lang.Object)
     */
    public void setMetaDataParameters(Object object)
    {
        Patient patient = (Patient)object;
        this.setName(patient.getName());
    }
}