import java.io.*;
import gnu.io.*;
import java.util.*;

public class ReadCom{
	static Enumeration portList;
	static CommPortIdentifier portId; 
	static SerialPort serialPort;
	static OutputStream outputStream;
	
	public void CheckCom(){
		portList = CommPortIdentifier.getPortIdentifiers();
		while(portList.hasMoreElements()){
			portId = (CommPortIdentifier)portList.nextElement();
			if(portId.getPortType()==CommPortIdentifier.PORT_SERIAL){
				if(portId.getName().equals("COM1")){
					System.out.println(portId.getName());
					try{
						serialPort = (SerialPort)portId.open("SimpleWriteApp", 2000);
					}
					catch(PortInUseException e){}
					try{
						outputStream = serialPort.getOutputStream();
					}
					catch(IOException e){}
					try{
						serialPort.setSerialPortParams(57600, 
								SerialPort.DATABITS_8,
								SerialPort.STOPBITS_1,
								SerialPort.PARITY_NONE);	
					}
					catch(UnsupportedCommOperationException e){}
					try{
						outputStream.write("yes".getBytes());
					}
					catch(IOException e){}
					serialPort.close();
				}
			}
		}
	}
	
	public static void main(String[] args){
		ReadCom test = new ReadCom();
		test.CheckCom();
	}
}
