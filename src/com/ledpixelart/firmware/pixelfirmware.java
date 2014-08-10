package com.ledpixelart.firmware;



import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.IOIO.VersionType;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.pc.IOIOSwingApp;

import ioio.lib.util.IOIOLooper;
import ioio.lib.util.pc.IOIOSwingApp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.UIManager;

import java.util.prefs.*;
import java.awt.FlowLayout;

//public class pixelfirmware extends IOIOSwingApp  {
//public class pixelfirmware extends IOIOSwingApp implements ActionListener {
public class pixelfirmware  {

	private static final String BUTTON_PRESSED = "bp";
	
	public static String pixelFirmware = "Not Found";
	 
	public static String pixelHardwareID = "";
	    
	private static VersionType v;
	
	private static final int ESTABLISH_CONNECTION = 0x00;
	private static final int CHECK_INTERFACE = 0x01;
	private static final int CHECK_INTERFACE_RESPONSE = 0x01;
	private static final int READ_FINGERPRINT = 0x02;
	private static final int FINGERPRINT = 0x02;
	private static final int WRITE_FINGERPRINT = 0x03;
	private static final int WRITE_IMAGE = 0x04;
	private static final int CHECKSUM = 0x04;
	
	private final static boolean shouldFill = true;
	private final static boolean shouldWeightX = true;
	private final static boolean RIGHT_TO_LEFT = false;

	private static enum Protocol {
		PROTOCOL_IOIO, PROTOCOL_BOOTLOADER
	}

	private static enum Command {
		VERSIONS, FINGERPRINT, WRITE
	}

	private static SerialPortIOIOConnection connection_;
	private static OutputStream out_;
	private static InputStream in_;
	private static final byte[] buffer_ = new byte[16];
	private static final int PROGRESS_SIZE = 40;

	private static String hardwareVersion_;
	private static String bootloaderVersion_;
	private static String applicationVersion_;
	private static String platformVersion_;
	private static Protocol whatIsConnected_;

	private static String portName_;
	private static Command command_;
	private static String fileName_;
	private static boolean reset_ = false;
	private static boolean force_ = false;
	
	private String textFieldLabel;
    private String buttonLabel;
     
    private JLabel label;
    private JTextField textField;
     
    private JFileChooser fileChooser;
    
    private static JTextArea mainText;
    private static JTextArea portText; 
     
    private int mode;
    public static final int MODE_OPEN = 1;
    public static final int MODE_SAVE = 2; //save is not used in this program
    
    private JFilePicker filePicker;
    private static JFrame frame;
    private static JButton versionButton;
    private static JButton upgradeButton;
    private static JLabel portLabel_;
    
    private static JCheckBox forceWriteCheckBox;
    
    private static String combinedText;
    private static String firmwareFilePath;
    
    private static Preferences prefs;
    private static final String prefPort_ = "prefPort";  //Preference key for this package
    private static String ourNodeName = "/com/ledpixelart/firmware";
    
    private static String serialPortInstructions = "- Enter the port PIXEL appears on your computer in the field above" + "\n"
			+ "- Winddows: Open device manager and look for the COM port # next to the device called 'IOIO OTG', format will be COMXX. Ex. COM9 or COM14" + "\n" 
			+ "- Mac OSX: Type < ls /dev/tty.usb* > from a command prompt, format will be /dev/tty.usbmodemXXXX. Ex. /dev/tty.usbmodem1411 or /dev/tty.usbmodem1421" + "\n" 
			+ "- Raspberry Pi and LINUX: format will be IOIOX. Ex. IOIO0 or IOIO1" + "\n";
 
	
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
    
    public static void addComponentsToPane(Container pane) {
        if (RIGHT_TO_LEFT) {
            pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }

 	pane.setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();
	if (shouldFill) {
	//natural height, maximum width
	c.fill = GridBagConstraints.HORIZONTAL;
	}
	
	if (shouldWeightX) {
		c.weightx = 0.5;
		}
	
	c.gridwidth = 1; //for the first row, each component should take up 1 column
	
	c.fill = GridBagConstraints.HORIZONTAL;
	c.gridx = 0;
	c.gridy = 0;
	c.insets = new Insets(0,30,0,0);  //left and right padding
	portLabel_ = new JLabel("PIXEL Port", JLabel.LEFT);
	pane.add(portLabel_, c);
	
	
	c.fill = GridBagConstraints.HORIZONTAL;
	c.gridx = 1;
	c.gridy = 0;
	c.insets = new Insets(0,20,0,400);  //left and right padding
	portText = new JTextArea(portName_);
	portText.setForeground(Color.BLUE);
	pane.add(portText, c);
	
	/// ********** the file picker component ***********************
	c.insets = new Insets(0,0,0,0);  //leave some space on left and right side of buttons
	c.gridwidth = 2; //from here on, each component should takes up 2 columns
	
	c.fill = GridBagConstraints.HORIZONTAL;
	c.ipady = 0;      
	c.gridx = 0;       
	c.gridy = 1;       
	// set up a file picker component
    JFilePicker filePicker = new JFilePicker("Select Firmware File", "Browse...");
    filePicker.setMode(JFilePicker.MODE_OPEN);
    filePicker.addFileTypeFilter(".ioioapp", "Firmware Files"); //you can add another line too if you want to support multiple file types
    // access JFileChooser class directly
    JFileChooser fileChooser = filePicker.getFileChooser();
    fileChooser.setCurrentDirectory(new File("D:/"));
    pane.add(filePicker, c);
	
	//// ************ the check and upgrade firmware buttons ********************
	c.insets = new Insets(10,200,0,200);  //leave some space on left and right side of buttons
	
	versionButton = new JButton("Check Firmware Version");
	c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 0.5;
	c.gridx = 0;
	c.gridy = 2;
	pane.add(versionButton, c);
	
	c.fill = GridBagConstraints.HORIZONTAL;
	c.ipady = 20;      
	c.gridx = 0;   
	c.gridy = 3;     
	upgradeButton = new JButton("UPGRADE FIRMWARE");
	pane.add(upgradeButton, c);

	//*************Force Write Check Box *******************************************
	c.insets = new Insets(0,0,0,0);  //reset the padding
	
	c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 0.5;
	c.gridx = 0;
	c.gridy = 4;
	forceWriteCheckBox = new JCheckBox("Force firmware write even if current firmware matches");
	pane.add(forceWriteCheckBox, c);	
	
	//*************Main Text Area *******************************************
	combinedText = "FIRMWARE UPGRADE INSTRUCTIONS\n"
			+ "How to upgrade firmware video http://ledpixelart.com\n"
		    + "STEP 1\n"
			+ "- Move the toggle switch on the side of PIXEL towards the label 'PC USB'\n"
			+ "- Power off PIXEL and remove the back case by unscrewing the 4 screws by hand\n"
			+ "- While PIXEL is off, hold down the push button on PIXEL's circuit board and power on with the button still held down\n"
			+ "- The green LED on PIXEL's circuit board will be on\n"
			+ "- Release the button and the green LED will blink 3-4 times indicating PIXEL is ready to accept the new firmware\n"
			+ "- Unplug the Bluetooth dongle and connect PIXEL to your PC or Mac using the supplied USB A-A cable (the cable with the large USB connectors on both ends)\n"
			+ "STEP 2\n"
			+  serialPortInstructions
			+ "STEP 3\n"
			+  "Select the firmware upgrade file with the extension .ioioapp and click < UPGRADE FIRMWARE >\n"
			+ "STEP 4\n"
			+  "- Put the back case back on tighening the 4 screws by hand (do not over-tighten or you'll crack the acrylic case)\n"
			+  "- Unplug the USB cable and replace with the Bluetooth dongle\n"
			+  "- Move the toggle switch on the side of PIXEL towards the label 'Bluetooth'\n"
			+  "- Power PIXEL on and off\n";
   		
	c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 0.5;
	c.gridx = 0;
	c.gridy = 5;
	
	mainText = new JTextArea(combinedText);
	Font font = new Font("Verdana", Font.PLAIN, 11);
	mainText.setFont(font);
	
	mainText.setLineWrap(true);
	mainText.setWrapStyleWord(true);
	mainText.setEditable(false);
	mainText.setBackground(Color.LIGHT_GRAY);
	mainText.setForeground(Color.BLUE);
	pane.add(mainText, c);
	
	// Display the window.
	frame.setSize(900, 650);
	frame.setLocationRelativeTo(null); // center it
	frame.setVisible(true);
	
	versionButton.addActionListener(new ActionListener(){
        @Override
        public void actionPerformed(ActionEvent e){
		    
  		    portName_ = portText.getText();
  		    
  		    if (portText.getText().equals("Enter PIXEL Port Here")) {
  		    	combinedText = "Oops... You forgot to enter PIXEL's Port\n\n"
  		    			+ serialPortInstructions + "\n";
      		    mainText.setText(combinedText);
  		    }
  		    
  		    else if (!portText.getText().contains("dev") && !portText.getText().contains("DEV") //do we need dev if we have tty? TO DO test that
  		    		&& !portText.getText().contains("COM") && !portText.getText().contains("com")
  		    		&& !portText.getText().contains("tty") && !portText.getText().contains("TTY") 
  		    		&& !portText.getText().contains("ioio") && !portText.getText().contains("IOIO")) {
  		    	combinedText = "PIXEL PORT FORMAT IS NOT VALID\n\n"
  		    			+  serialPortInstructions + "\n";
      		    mainText.setText(combinedText);
  		    }
  		    
  		    else {
  		    	prefs.put(prefPort_,portText.getText()); //let's write the prefs for the port
  				try {
  					connect(portName_);
  				} catch (IOException e1) {
  					// TODO Auto-generated catch block
  					e1.printStackTrace();
  					combinedText = "Could not find " + "\n";
  	    		    mainText.setText(combinedText);
  				} catch (ProtocolException e1) {
  					// TODO Auto-generated catch block
  					e1.printStackTrace();
  					combinedText = "Could not find " + "\n";
  	    		    mainText.setText(combinedText);
  				}
  			
  		    versionsCommand();
  		    
  		  /*  try {
  				hardReset();
  			} catch (IOException e1) {
  				// TODO Auto-generated catch block
  				e1.printStackTrace();
  			}*/
  		    
  		    tellUserRestart("Please close and restart this app if you need to run it again...");

  		    }
          }
      });
	
	upgradeButton.addActionListener(new ActionListener(){
        @Override
        public void actionPerformed(ActionEvent e){
        	firmwareFilePath = JFilePicker.getSelectedFilePath();
		    prefs.put(prefPort_,portText.getText()); //let's write the prefs for the port
		    force_ = forceWriteCheckBox.isSelected(); //whether or not we will over-write firmware even if the version already matches
		    
		    if (firmwareFilePath.equals("")) {
		    	combinedText = "You must first select a firmware file" + "\n";
    		    mainText.setText(combinedText);
		    }
		    
		    else if (portText.getText().equals("Enter PIXEL Port Here")) {
		    	combinedText = "Oops... You forgot to enter the PIXEL Port\n\n"
		    			+ serialPortInstructions + "\n";
    		    mainText.setText(combinedText);
		    }
		    
		    else if (!portText.getText().contains("dev") && !portText.getText().contains("DEV") //do we need dev if we have tty? TO DO test that
		    		&& !portText.getText().contains("COM") && !portText.getText().contains("com")
		    		&& !portText.getText().contains("tty") && !portText.getText().contains("TTY") 
		    		&& !portText.getText().contains("ioio") && !portText.getText().contains("IOIO")) {
		    	combinedText = "PIXEL PORT FORMAT IS NOT VALID\n\n"
		    			+  serialPortInstructions + "\n";
    		    mainText.setText(combinedText);
		    }
		    
		    else { //ok we're good let's continue and write the firmware
		    	combinedText = firmwareFilePath + "\n";
    		    mainText.setText(combinedText);
    		   
				try {
					connect(portName_);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ProtocolException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
    			
    		    try {
					writeCommand();
				} catch (NoSuchAlgorithmException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ProtocolException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				/*try {
					hardReset();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}*/
    		    
    		    tellUserRestart("Please close and restart this app if you need to run it again...");
    		    
    		    
		    }
        }
    });
    }

	
	//protected Window createMainWindow(String args[]) {
	public static void createAndShowGUI() {
		// Use native look and feel.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		
		prefs = Preferences.userRoot().node(ourNodeName); //let's get our preferences
		String defaultPortValue = "Enter PIXEL Port Here"; //if the pref does not exist yet, use this
		portName_ = prefs.get(prefPort_, defaultPortValue);
	 		
		frame = new JFrame("PIXEL Firmware Upgrade");
		//frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	    //Set up the content pane.
	    addComponentsToPane(frame.getContentPane());

	    //Display the window.
	    frame.pack();
	    frame.setVisible(true);
		//return frame; //had this when I was trying to use IOIOSwing
	}
	
	private static void tellUserRestart (String msg) {
		combinedText = "\n" + combinedText + msg + "\n";
	    mainText.setText(combinedText);
	    upgradeButton.setEnabled(false);
	    versionButton.setEnabled(false);
	    forceWriteCheckBox.setEnabled(false);
	    //pane.showMessageDialog(frame, "Eggs are not supposed to be green.");
	}
	
	private void runFirmwareUpdate() {
		combinedText = "PIXEL FOUND" + "\n"
				+ "Current Firmware Version: " + pixelFirmware;
		mainText.setText(combinedText);
	}
	
	private static void hardReset() throws IOException {
		out_.write(new byte[] { 0x00, 'I', 'O', 'I', 'O' });
		out_.flush();
	}

	private static void writeCommand() throws IOException, ProtocolException,
			NoSuchAlgorithmException {
		checkBootloaderProtocol();
		File file = new File(firmwareFilePath);
		
		ZipFile zip = new ZipFile(file, ZipFile.OPEN_READ);
		try {
			ZipEntry entry = zip.getEntry(platformVersion_ + ".ioio");
			if (entry == null) {
				System.err
						.println("Application bundle does not include an image for the platform "
								+ platformVersion_);
				combinedText = combinedText + "\n" + "**** THIS PIXEL FIRMWARE FILE IS NOT VALID ****\n\n";
			    mainText.setText(combinedText);
				return;
			}

			byte[] fileFp = calculateFingerprint(zip.getInputStream(entry));

			if (!force_) {
				System.err.println("Comparing fingerprints...");
				combinedText = combinedText + "\n" + "Comparing firmware fingerprints...";
			    mainText.setText(combinedText);
				byte[] currentFp = readFingerprint();

				if (Arrays.equals(currentFp, fileFp)) {
					System.err.println("Fingerprint match - skipping write.");
					combinedText = combinedText + "\n" + "Firmware fingerprint match - skipping write - your firmware is already up to date\n\n";
				    mainText.setText(combinedText);
					return;
				} else {
					System.err.println("Fingerprint mismatch.");
					//combinedText = combinedText + "\n" + "Fingerprint mismatch.";
				    //mainText.setText(combinedText);
				}
			}

			System.err.println("Writing image...");
			combinedText = "Writing Firmware...";
		    mainText.setText(combinedText);
			short checksum = writeImage(zip.getInputStream(entry),
					(int) entry.getSize());
			if (readChecksum() != checksum) {
				combinedText = combinedText + "Bad checksum. PIXEL firmware image is possibly corrupt.";
			    mainText.setText(combinedText);
				throw new ProtocolException(
						"Bad checksum. IOIO image is possibly corrupt.");
			}
			System.err.println("Writing fingerprint...");
			Font font = new Font("Verdana", Font.BOLD, 11);
			mainText.setFont(font);
			combinedText = combinedText + "\n" + "Writing Firmware...";
		    mainText.setText(combinedText);
			writeFingerprint(fileFp);
			mainText.setForeground(Color.BLUE); //change the color to green letting the user know all is good
			//TO DO make font bigger
			System.err.println("Done.");
			combinedText = combinedText + "\n" + "DONE\n\n" 
					+  "- Put the back case back on tighening the 4 screws by hand (do not over-tighten or you'll crack the acrylic case)\n"
					+  "- Unplug the USB cable and replace with the Bluetooth dongle\n"
					+  "- Move the toggle switch on the side of PIXEL towards the label 'Bluetooth'\n"
					+  "- Power PIXEL on and off\n\n";
		    mainText.setText(combinedText);
		} finally {
			zip.close();
		}
	}
	
	private static short writeImage(InputStream in, int length)
			throws IOException {
		out_.write(WRITE_IMAGE);

		out_.write((int) ((length >> 0) & 0xff));
		out_.write((int) ((length >> 8) & 0xff));
		out_.write((int) ((length >> 16) & 0xff));
		out_.write((int) (length >> 24) & 0xff);

		out_.flush();

		short checksum = 0;
		int written = 0;
		int progress = -1;
		int i;
		byte[] buffer = new byte[1024];
		while (-1 != (i = in.read(buffer))) {
			for (int j = 0; j < i; ++j) {
				checksum += ((int) buffer[j]) & 0xFF;
			}
			out_.write(buffer, 0, i);
			out_.flush();
			written += i;
			if (written * PROGRESS_SIZE / length != progress) {
				progress = written * PROGRESS_SIZE / length;
				printProgress(progress);
			}
		}
		System.err.println();
		return checksum;
	}

	private static void printProgress(int progress) {
		System.err.print('[');
		combinedText = combinedText + "[";
		mainText.setText(combinedText);
		for (int i = 0; i < PROGRESS_SIZE; ++i) {
			if (i < progress) {
				System.err.print('#');
				combinedText = combinedText + "#";
			    mainText.setText(combinedText);
			} else {
				System.err.print(' ');
				combinedText = combinedText + " ";
			    mainText.setText(combinedText);
			}
		}
		System.err.print(']');
		combinedText = "]";
	    mainText.setText(combinedText);
		System.err.print('\r');
		combinedText =  "\r";
	    mainText.setText(combinedText);
	}

	private static short readChecksum() throws IOException, ProtocolException {
		if (in_.read() != CHECKSUM) {
			throw new ProtocolException("Unexpected response.");
		}
		readExactly(2);
		final int b0 = ((int) buffer_[0]) & 0xff;
		final int b1 = ((int) buffer_[1]) & 0xff;
		return (short) (b0 | b1 << 8);
	}

	private static byte[] calculateFingerprint(InputStream in) throws IOException,
			NoSuchAlgorithmException {
		MessageDigest digester = MessageDigest.getInstance("MD5");
		byte[] bytes = new byte[1024];
		int byteCount;
		while ((byteCount = in.read(bytes)) > 0) {
			digester.update(bytes, 0, byteCount);
		}
		return digester.digest();
	}

	private static void writeFingerprint(byte[] fingerprint) throws IOException {
		assert fingerprint.length == 16;
		out_.write(WRITE_FINGERPRINT);
		out_.write(fingerprint);
		out_.flush();
	}

	private static void fingerprintCommand() throws IOException,
			ProtocolException {
		checkBootloaderProtocol();
		byte[] fingerprint = readFingerprint();
		for (int i = 0; i < 16; ++i) {
			System.out
					.print(Integer.toHexString(((int) fingerprint[i]) & 0xff));
			combinedText = combinedText + Integer.toHexString(((int) fingerprint[i]) & 0xff);
		    mainText.setText(combinedText);
		}
		System.out.println();
		combinedText = combinedText + "\n";
	    mainText.setText(combinedText);
	}

	private static byte[] readFingerprint() throws ProtocolException,
			IOException {
		out_.write(READ_FINGERPRINT);
		out_.flush();
		if (in_.read() != FINGERPRINT) {
			throw new ProtocolException("Unexpected response.");
		}
		readExactly(16);
		byte[] fingerprint = new byte[16];
		System.arraycopy(buffer_, 0, fingerprint, 0, 16);
		return fingerprint;
	}

	private static void checkBootloaderProtocol() throws IOException,
			ProtocolException {
		if (whatIsConnected_ != Protocol.PROTOCOL_BOOTLOADER) {
			
			combinedText = combinedText + "**** PIXEL WAS DETECTED BUT IS NOT IN FIRMWARE UPDATE MODE ****\n\n"
					+ "Enter firmware upgrade mode by:\n"
					+ "- Unplug the Bluetooth dongle and connect PIXEL to your PC or Mac using the supplied USB A-A cable (the cable with the large USB connectors on both ends)\n"
					+ "- Move the toggle switch on the side of PIXEL towards the label 'PC USB'\n"
					+ "- Power off PIXEL and remove the back case by unscrewing the 4 screws by hand\n"
					+ "- While PIXEL is off, hold down the push button on PIXEL's circuit board and power on with the button still held down\n"
					+ "- The green LED on PIXEL's circuit board will be on\n"
					+ "- Release the button and the green LED will blink 3-4 times indicating PIXEL is ready to accept the new firmware\n";
		    mainText.setText(combinedText);
			
			throw new ProtocolException(
					"PIXEL is not in bootloader mode!\n"
							+ "Enter bootloader mode by:\n"
							+ "- Power off the IOIO.\n"
							+ "- Connect the 'boot' pin to 'GND'.\n"
							+ "- Power on the IOIO.\n"
							+ "- The stat LED should be on constantly.\n"
							+ "- Disconnect 'boot' from 'GND'. The stat LED should blink a few times.\n"
							+ "Now, try again.");
		}
	    
		out_.write(CHECK_INTERFACE);
		out_.write("BOOT0001".getBytes());
		out_.flush();
		readExactly(2);
		if (buffer_[0] != CHECK_INTERFACE_RESPONSE) {
			throw new ProtocolException("Unexpected response.");
		}
		if ((buffer_[1] & 0x01) == 0) {
			throw new ProtocolException(
					"Bootloader does not support protocol BOOT0001.");
		}
	}

	private static void versionsCommand() {
		switch (whatIsConnected_) {
		case PROTOCOL_IOIO:
			System.err.println("PIXEL FOUND\n\n");
			combinedText =  "PIXEL FOUND\n";
		    mainText.setText(combinedText);
			break;

		case PROTOCOL_BOOTLOADER:
			System.err.println("PIXEL is in firmware upgrade mode");
			combinedText = "PIXEL is in firmware upgrade mode. Please power PIXEL on and off if you're down with the firmware upgrade and ready to use PIXEL.\n\n";
		    mainText.setText(combinedText);
			break;
		}
		System.err.println();
		
		switch (whatIsConnected_) {
		case PROTOCOL_IOIO:
			System.err.println("Application version: " + applicationVersion_);
			combinedText = combinedText + "FIRMWARE VERSION: " + applicationVersion_ + "\n";
		    mainText.setText(combinedText);
			break;

		case PROTOCOL_BOOTLOADER:
			System.err.println("Platform version: " + platformVersion_);
			combinedText = combinedText + "Platform version: " + platformVersion_ + "\n";
		    mainText.setText(combinedText);
			break;
		}
		
		combinedText = combinedText + "\n";
	    mainText.setText(combinedText);
		System.err.println("Hardware Version: " + hardwareVersion_);
		combinedText = combinedText + "Hardware Version: " + hardwareVersion_ + "\n";
	    mainText.setText(combinedText);
		System.err.println("Bootloader Version: " + bootloaderVersion_);
		combinedText = combinedText + "Bootloader Version: " + bootloaderVersion_ + "\n\n";
	    mainText.setText(combinedText);
	}
	
	private static void connect(String port) throws IOException,
			ProtocolException {
		connection_ = new SerialPortIOIOConnection(port);
		try {
			connection_.waitForConnect();
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			out_ = connection_.getOutputStream();
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			in_ = connection_.getInputStream();
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (in_.read() != ESTABLISH_CONNECTION) {
			throw new ProtocolException("Got and unexpected input.");
		
		}
		readExactly(4);
		if (bufferIsIOIO()) {
			whatIsConnected_ = Protocol.PROTOCOL_IOIO;
			readIOIOVersions();
		} else if (bufferIsBoot()) {
			whatIsConnected_ = Protocol.PROTOCOL_BOOTLOADER;
			readBootVersions();
		} else {
			combinedText = combinedText + "Device is neither a standard PIXEL application nor a PIXEL bootloader." + "\n";
		    mainText.setText(combinedText);
			throw new ProtocolException(
					"Device is neither a standard PIXEL application nor a PIXEL bootloader.");
		}
}

	private static void readIOIOVersions() throws ProtocolException,
			IOException {
		byte[] ver = new byte[8];
		readExactly(ver, 0, 8);
		hardwareVersion_ = new String(ver);
		readExactly(ver, 0, 8);
		bootloaderVersion_ = new String(ver);
		readExactly(ver, 0, 8);
		applicationVersion_ = new String(ver);
	}

	private static void readBootVersions() throws ProtocolException,
			IOException {
		byte[] ver = new byte[8];
		readExactly(ver, 0, 8);
		hardwareVersion_ = new String(ver);
		readExactly(ver, 0, 8);
		bootloaderVersion_ = new String(ver);
		readExactly(ver, 0, 8);
		platformVersion_ = new String(ver);
	}

	private static boolean bufferIsBoot() {
	return buffer_[0] == 'B' && buffer_[1] == 'O' && buffer_[2] == 'O'
			&& buffer_[3] == 'T';
	}

	private static boolean bufferIsIOIO() {
	return buffer_[0] == 'I' && buffer_[1] == 'O' && buffer_[2] == 'I'
			&& buffer_[3] == 'O';
	}

	private static void readExactly(int size) throws ProtocolException,
		IOException {
	readExactly(buffer_, 0, size);
	}

	private static void readExactly(byte[] buffer, int offset, int size)
			throws ProtocolException, IOException {
		while (size > 0) {
			int num = in_.read(buffer, offset, size);
			if (num < 0) {
				throw new ProtocolException("Unexpected connection closure.");
			}
			size -= num;
			offset += num;
		}
	}

	private static class ProtocolException extends Exception {
	private static final long serialVersionUID = 4332923296318917793L;
	
	public ProtocolException(String message) {
		super(message);
	}
	}

	private static class BadArgumentsException extends Exception {
	private static final long serialVersionUID = -5730905669013719779L;
	
	public BadArgumentsException(String message) {
		super(message);
	}
	}

	/*@Override
	public IOIOLooper createIOIOLooper(String connectionType, Object extra) {
		// TODO Auto-generated method stub
		return null;
	}*/
	
	 

	/*@Override
	public IOIOLooper createIOIOLooper(String connectionType, Object extra) {
		return new BaseIOIOLooper() {
			//private DigitalOutput led_;

			@Override
			protected void setup() throws ConnectionLostException,
					InterruptedException {
			//	led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
				pixelFirmware = ioio_.getImplVersion(v.APP_FIRMWARE_VER);
	  			pixelHardwareID = ioio_.getImplVersion(v.HARDWARE_VER);
	  			
	  			runFirmwareUpdate();
			}

			@Override
			public void loop() throws ConnectionLostException,
					InterruptedException {
			//	led_.write(!ledOn_);
				Thread.sleep(10);
			}
		};
	}*/

	
}
