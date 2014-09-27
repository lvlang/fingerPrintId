import java.io.*;
import java.util.*;
import gnu.io.*;

public class Test implements Runnable,SerialPortEventListener{
	static CommPortIdentifier portId;
	static Enumeration portList;
	OutputStream outputStream;
	InputStream inputStream;
	String feedBackString;
	SerialPort serialPort;
	Thread readThread;
	
	public static void main(String[] args){
		portList=CommPortIdentifier.getPortIdentifiers();
		while(portList.hasMoreElements()){
			portId=(CommPortIdentifier)portList.nextElement();
			if(portId.getPortType()==CommPortIdentifier.PORT_SERIAL){
				if(portId.getName().equals("COM1")){
					System.out.println("COM1 start!");
					Test reader=new Test();
				}
			}
		}
	}
	
	public Test(){
		try{
			serialPort=(SerialPort)portId.open("Sunder",2000);
		}
		catch(PortInUseException e){
			System.out.println(e);
		}
		try{
			inputStream=serialPort.getInputStream();
		}
		catch(IOException e){
			System.out.println(e);
		}
		try{
			serialPort.addEventListener(this);
		}
		catch(TooManyListenersException e){
			System.out.println(e);
		}
		serialPort.notifyOnDataAvailable(true);
		try{
			serialPort.setSerialPortParams(57600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
		}
		catch(UnsupportedCommOperationException e){
			System.out.println(e);
		}
		readThread=new Thread(this);
		readThread.start();
	}
	
	public void run(){
		try{
			Thread.sleep(20000);
		}catch(InterruptedException e){
			System.out.println(e);
		}
	}
	public void serialEvent(SerialPortEvent event){
		switch(event.getEventType()){
		case SerialPortEvent.BI:
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY: break;
		case SerialPortEvent.DATA_AVAILABLE:
			byte[] readBuffer=new byte[20];
			try{
				while(inputStream.available()>0){
					int numBytes=inputStream.read(readBuffer);
				}
				feedBackString = printHexString(readBuffer);
				System.out.println(feedBackString);
				if(feedBackString.indexOf("55")==0){
					System.out.println("模块已上电！！！");
				}
			}
			catch(IOException e){
				System.out.println(e);
			}
			break;
		}
	}
	
	public String printHexString(byte[] b){
		String a = "";
		for (int i = 0; i < b.length; i++) { 
		    String hex = Integer.toHexString(b[i] & 0xFF); 
		    if (hex.length() == 1){ 
		    	hex = '0' + hex; 
		    }
		    
		    a = a+hex;
		} 
		return a;
	}
	
	public void closeSerialPort(){
		serialPort.close();
	}
}
