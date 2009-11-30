/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.communication.file;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import org.javarosa.core.services.ITransportManager;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.transport.ITransportDestination;
import org.javarosa.core.services.transport.MessageListener;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.core.services.transport.TransportMethod;

/**
 * 
 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
 */
public class FileConnectionTransportMethod implements TransportMethod {

	private static final String name = "File";

	private TransportMessage message;

	private ITransportManager manager;
	
	/**
	 * Creates a new instance of <code>FileConnectionTransportMethod</code>
	 */
	public FileConnectionTransportMethod() {
		// nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.transport.TransportMethod#transmit(org.openmrs.transport.TransportMessage,
	 *      org.openmrs.transport.TransportManager)
	 */
	public void transmit(TransportMessage message, ITransportManager manager) {
		//#if debug.output==verbose
		System.out.println("File connection: transmit");
		//#endif
		this.message = message;
		this.manager = manager;
		new Thread(new WorkerThread()).start();
	}

	/**
	 * 
	 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
	 */
	private class WorkerThread implements Runnable {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			// TODO: why this=?
			/*for (Enumeration en = FileSystemRegistry.listRoots(); en
					.hasMoreElements();) {
				en.nextElement();
			}*/
			
			FileTransportDestination destination = (FileTransportDestination)message.getDestination();
			FileConnection fcon = null;
			try {
				fcon = (FileConnection) Connector.open(destination.getURI());
				if (!fcon.exists()) {
					fcon.create();
				}
				OutputStream out = fcon.openOutputStream();
				
				InputStream valueStream = message.getPayloadData().getPayloadStream();
				int val = valueStream.read();
				while(val != -1) {
					out.write(val);
					val = valueStream.read();
				}
				
				//This is not legit. 
				//out.write(ExtUtil.serialize(message));
				
				//update status
				message.setStatus(TransportMessage.STATUS_DELIVERED);
				message.setChanged();
				message.notifyObservers(null);
			} catch (SecurityException e) {
				e.printStackTrace();
				manager.showMessage("Not permitted to write to file",
						MessageListener.TYPE_ERROR);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (fcon != null) {
					try {
						fcon.close();
					} catch (Exception e) {
						// ignore
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.transport.TransportMethod#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.transport.TransportMethod#getId()
	 */
	public int getId() {
		return TransportMethod.FILE;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.transport.TransportMethod#getDefaultDestination()
	 */
	public ITransportDestination getDefaultDestination() {
		String uri = PropertyManager._().getSingularProperty(FileTransportProperties.SAVE_URI_PROPERTY);
		if(uri == null) {
			return null;
		} else {
			return new FileTransportDestination(uri);
		}
	}
	
	public void closeConnections() {
		// TODO release all open connections and resources
		
	}
}