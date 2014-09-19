import java.io.*;
import gnu.io.*;
import java.util.*;

public class SimpleWrite{
	static Enumeration portList;
	static CommPortIdentifier portId; 
	static SerialPort serialPort;
	static OutputStream outputStream;
	static String messageString = "EF01FFFFFFFF010003010005";
	
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
	
	public void CheckCom(){
		portList = CommPortIdentifier.getPortIdentifiers();
		while(portList.hasMoreElements()){
			portId = (CommPortIdentifier)portList.nextElement();
			if(portId.getPortType()==CommPortIdentifier.PORT_SERIAL){
				if(portId.getName().equals("COM1")){
					System.out.println(portId.getName());
					try{
						serialPort = (SerialPort)portId.open("SImpleWriteApp", 1000);
						System.out.println("Opened");
					}
					catch(PortInUseException e){
						e.printStackTrace();
					}
					
					try{
						outputStream = serialPort.getOutputStream();
					}
					catch(IOException e){
						e.printStackTrace();
					}
					
					try{
						serialPort.setSerialPortParams(57600, 
								SerialPort.DATABITS_8,
								SerialPort.STOPBITS_1, 
								SerialPort.PARITY_NONE);
					}
					catch(UnsupportedCommOperationException e){
						e.printStackTrace();
					}
					
					try{
						outputStream.write(hexStringToBytes(messageString));
					}
					catch(IOException e){
						e.printStackTrace();
					}
					
					serialPort.close();
				}
			}
		}
	}
	
	public static void main(String[] args){
		SimpleWrite simpleWrite = new SimpleWrite();
		simpleWrite.CheckCom();
	}
}
