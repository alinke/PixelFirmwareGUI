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
import java.awt.Container;
import java.awt.FlowLayout;
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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.UIManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

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
   // private JButton button;
     
    private JFileChooser fileChooser;
    
    private static JTextArea mainText;
    private static JTextArea portText; 
     
    private int mode;
    public static final int MODE_OPEN = 1;
    public static final int MODE_SAVE = 2; //save is not used in this program
    
    private JFilePicker filePicker;
    private static JFrame frame;
    private Container contentPane;
    private static JButton versionButton;
    private static JButton upgradeButton;
    
    private static String combinedText;
    private static String firmwareFilePath;

	// Boilerplate main(). Copy-paste this code into any IOIOapplication.
	public static void main(String[] args) throws Exception {
		//new pixelfirmware().go(args);
		setupGUI();
		
	}

//	protected boolean ledOn_;

	
	//protected Window createMainWindow(String args[]) {
	public static void setupGUI() {
		// Use native look and feel.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		frame = new JFrame("PIXEL Firmware Upgrade");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		Container contentPane = frame.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
		
		portText = new JTextArea("Overwrite here with PIXEL's Port");
		portText.setBackground(Color.BLUE);
		portText.setForeground(Color.WHITE);
		contentPane.add(portText);	
		
		mainText = new JTextArea("Searching for PIXEL...");
	    //mainText.setFont(new Font("Serif", Font.ITALIC, 16));
		mainText.setLineWrap(true);
		mainText.setWrapStyleWord(true);
		mainText.setEditable(false);
		contentPane.add(mainText);	
		
		//contentPane.setLayout(new FlowLayout());
		 
        // set up a file picker component
        JFilePicker filePicker = new JFilePicker("Select Firmware File", "Browse...");
        filePicker.setMode(JFilePicker.MODE_OPEN);
        filePicker.addFileTypeFilter(".ioioapp", "Firmware Files");
       // filePicker.addFileTypeFilter(".mp4", "MPEG-4 Videos");
         
        // access JFileChooser class directly
        JFileChooser fileChooser = filePicker.getFileChooser();
        fileChooser.setCurrentDirectory(new File("D:/"));
         
        // add the component to the frame
        contentPane.add(filePicker);
         
       // contentPane.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     //   contentPane.setSize(520, 100);
       //contentPane.setLocationRelativeTo(null);    // center on screen
		
		versionButton = new JButton("Check Firmware Version");
		versionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(versionButton);
		
		upgradeButton = new JButton("Upgrade Firmware");
		upgradeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(upgradeButton);
		//versionButton.setActionCommand(BUTTON_PRESSED);
		
		contentPane.add(Box.createVerticalGlue());
		contentPane.add(versionButton);
		contentPane.add(upgradeButton);
		contentPane.add(Box.createVerticalGlue());

		// Display the window.
		frame.setSize(800, 500);
		frame.setLocationRelativeTo(null); // center it
		frame.setVisible(true);
		
		versionButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
            	/*firmwareFilePath = JFilePicker.getSelectedFilePath();
    			combinedText = combinedText + "\n" + firmwareFilePath + "\n";
    		    mainText.setText(combinedText);*/
    		    
    		   // portName_ = "/dev/tty.usbmodem1411"; 
    		    portName_ = portText.getText();
    		    command_ = Command.FINGERPRINT;
    		    force_ = true;
    		    
    		    upgradeButton.setEnabled(false);
    		    versionButton.setEnabled(false);
    		   
    				try {
						connect(portName_);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						combinedText = combinedText + "Could not find PIXEL" + "\n";
		    		    mainText.setText(combinedText);
					} catch (ProtocolException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						combinedText = combinedText + "Could not find PIXEL" + "\n";
		    		    mainText.setText(combinedText);
					}
    			
    		    versionsCommand();
    		    
    		   // contentPane.showMessageDialog(frame, "thank you for using java");
    
            }
        });
		
		upgradeButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
            	firmwareFilePath = JFilePicker.getSelectedFilePath();
    			combinedText = combinedText + "\n" + firmwareFilePath + "\n";
    		    mainText.setText(combinedText);
    		    
    		    portName_ = "/dev/tty.usbmodem1411"; 
    		    command_ = Command.WRITE;
    		    force_ = true;
    		    
    		    upgradeButton.setEnabled(false);
    		    versionButton.setEnabled(false);
    		   
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
    		    
    		   // contentPane.showMessageDialog(frame, "thank you for using java");
    
            }
        });
		
		
		
		//return frame;
	}
	
	/*public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().equals(BUTTON_PRESSED)) {
			//ledOn_ = ((JToggleButton) event.getSource()).isSelected();
			firmwareFilePath = filePicker.getSelectedFilePath();
			combinedText = combinedText + "\n" + firmwareFilePath + "\n";
		    mainText.setText(combinedText);
		    
		    portName_ = "/dev/tty.usbmodem1411"; 
		    command_ = Command.FINGERPRINT;
		    force_ = true;
		    
		    try {
				connect(portName_);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		    versionsCommand();
		    try {
				hardReset();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		   
		    
		    try {
				connect(portName_);
				switch (command_) {
				case VERSIONS:
					versionsCommand();
					break;

				case FINGERPRINT:
					fingerprintCommand();
					break;

				case WRITE:
					writeCommand();
					break;
				}
				if (reset_) {
					hardReset();
				}
			} catch (IOException e) {
				System.err.println("Caught IOException. Exiting.");
				System.exit(3);
			} catch (ProtocolException e) {
				System.err.println("Protocol error:");
				System.err.println(e.getMessage());
				System.err.println("Exiting.");
				System.exit(4);
			} catch (NoSuchAlgorithmException e) {
				System.err.println("System cannot calculate MD5. Cannot proceed.");
				System.exit(5);
			}
		    
	
		}
	}*/
	
	private void runFirmwareUpdate() {
		combinedText = "PIXEL FOUND" + "\n"
				+ "Current Firmware Version: " + pixelFirmware;
		mainText.setText(combinedText);
	}
	
	private static void printUsage() {
		System.err.println("IOIODude V1.1");
		System.err.println();
		System.err.println("Usage:");
		System.err.println("ioiodude <options> versions");
		System.err.println("ioiodude <options> fingerprint");
		System.err.println("ioiodude <options> write <ioioapp>");
		System.err.println();
		System.err.println("Valid options are:");
		System.err
				.println("--port=<name> The serial port where the IOIO is connected.");
		System.err
				.println("--reset Reset the IOIO out of bootloader mode when done.");
		System.err
				.println("--force Bypass fingerprint matching and force writing.");
	}
	
	private static void hardReset() throws IOException {
		out_.write(new byte[] { 0x00, 'I', 'O', 'I', 'O' });
		out_.flush();
	}

	private static void writeCommand() throws IOException, ProtocolException,
			NoSuchAlgorithmException {
		checkBootloaderProtocol();
		//File file = new File(fileName_);
		File file = new File(firmwareFilePath);
		
		ZipFile zip = new ZipFile(file, ZipFile.OPEN_READ);
		try {
			ZipEntry entry = zip.getEntry(platformVersion_ + ".ioio");
			if (entry == null) {
				System.err
						.println("Application bundle does not include an image for the platform "
								+ platformVersion_);
				combinedText = combinedText + "\n" + "Application bundle does not include an image for the platform"
						+ platformVersion_;
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
					combinedText = combinedText + "\n" + "Firmware fingerprint match - skipping write - your firmware is already up to date \n.";
				    mainText.setText(combinedText);
					return;
				} else {
					System.err.println("Fingerprint mismatch.");
					combinedText = combinedText + "\n" + "Fingerprint mismatch.";
				    mainText.setText(combinedText);
				}
			}

			System.err.println("Writing image...");
			combinedText = combinedText + "\n" + "Writing image...";
		    mainText.setText(combinedText);
			short checksum = writeImage(zip.getInputStream(entry),
					(int) entry.getSize());
			if (readChecksum() != checksum) {
				throw new ProtocolException(
						"Bad checksum. IOIO image is possibly corrupt.");
			}
			System.err.println("Writing fingerprint...");
			combinedText = combinedText + "\n" + "Writing fingerprint...";
		    mainText.setText(combinedText);
			writeFingerprint(fileFp);
			System.err.println("Done.");
			combinedText = combinedText + "\n" + "Done.";
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
		mainText.setText("[");
		for (int i = 0; i < PROGRESS_SIZE; ++i) {
			if (i < progress) {
				System.err.print('#');
				combinedText = combinedText + "#";
			    mainText.setText(combinedText);
			} else {
				System.err.print(' ');
				//combinedText = "\' \'";
			    mainText.setText(" ");
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
			
			combinedText = combinedText + "PIXEL is not in bootloader mode!\n"
					+ "Enter bootloader mode by:\n"
					+ "- Use the USB A-A cable to USB connect PIXEL to your PC or Mac\n"
					+ "- Move the toggle switch to PC USB\n"
					+ "- Power off PIXEL and remove the back case.\n"
					+ "- While PIXEL is off, hold down the button on PIXEL's board and then turn on POWER\n"
					+ "- The green LED should be on constantly.\n"
					+ "- Release the button and the green LED should blink 3 times.\n"
					+ "Now, try again.";
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
			System.err.println("IOIO Application detected.");
			combinedText = combinedText + "PIXEL detected.";
		    mainText.setText(combinedText);
			break;

		case PROTOCOL_BOOTLOADER:
			System.err.println("PIXEL Bootloader detected.");
			combinedText = combinedText + "PIXEL in Bootloader mode detected.";
		    mainText.setText(combinedText);
			break;
		}
		System.err.println();
		combinedText = combinedText + "\n";
	    mainText.setText(combinedText);
		System.err.println("Hardware version: " + hardwareVersion_);
		combinedText = combinedText + "Hardware version: " + hardwareVersion_ + "\n";
	    mainText.setText(combinedText);
		System.err.println("Bootloader version: " + bootloaderVersion_);
		combinedText = combinedText + "Bootloader version: " + bootloaderVersion_ + "\n";
	    mainText.setText(combinedText);
		switch (whatIsConnected_) {
		case PROTOCOL_IOIO:
			System.err.println("Application version: " + applicationVersion_);
			combinedText = combinedText + "Firmware version: " + applicationVersion_ + "\n";
		    mainText.setText(combinedText);
			break;

		case PROTOCOL_BOOTLOADER:
			System.err.println("Platform version: " + platformVersion_);
			combinedText = combinedText + "Platform version: " + platformVersion_ + "\n";
		    mainText.setText(combinedText);
			break;
		}
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
			combinedText = combinedText + "Device is neither a standard IOIO application nor a IOIO bootloader." + "\n";
		    mainText.setText(combinedText);
			throw new ProtocolException(
					"Device is neither a standard IOIO application nor a IOIO bootloader.");
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
