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
