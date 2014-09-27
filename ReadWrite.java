import java.io.*;
import java.util.*;
import gnu.io.*;

public class Test implements Runnable,SerialPortEventListener{
	static CommPortIdentifier portId;
	static Enumeration portList;
	OutputStream outputStream;
	InputStream inputStream;
	static String feedBackString;
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
		try{
			outputStream=serialPort.getOutputStream();
		}
		catch(IOException e){
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
					senMsg();
				}
				if(feedBackString.equals("ef01ffffffff07000302000c0000000000000000")){
					System.out.println("传感器上无指纹！！！");
				}
				if(feedBackString.equals("ef01ffffffff07000300000a0000000000000000")){
					System.out.println("传感器上有指纹！！！");
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
	
	public static byte[] hexStringToBytes(String hexString){
        if (hexString == null || hexString.equals("")){
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length()/2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }
	
    private static byte charToByte(char c){
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
    
    private void senMsg(){
		String messageString = "EF01FFFFFFFF010003010005";
		try{
			outputStream.write(hexStringToBytes(messageString));
			inputStream = serialPort.getInputStream();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void closeSerialPort(){
		serialPort.close();
	}
}
