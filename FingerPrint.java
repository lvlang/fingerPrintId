import java.sql.*;
import java.io.*;
import java.util.*;
import gnu.io.*;

public class FingerPrint implements Runnable,SerialPortEventListener{
	static CommPortIdentifier portId;
	static Enumeration portList;
	OutputStream outputStream;
	InputStream inputStream;
	SerialPort serialPort;
	Thread readThread;
	int tplNum = 0;
	int strLen = 0;
	static String feedBackString;
	String lastCmd = "";
	String powerOn = ""; 
	String schTplNum = "EF01FFFFFFFF0100031D0021";
	String upChar = "EF01FFFFFFFF0100040801000E";
	String autoLogin = "";
	String readUpChar = "";
	
	String driver = "com.mysql.jdbc.Driver";
	String url = "jdbc:mysql://127.0.0.1:3306/fingerprint?useUnicode=true&characterEncoding=utf-8";
	String usr = "root";
	String pwd = "1234";
	Connection conn;
	Statement statement;
	String name = "";
	String sql = "";
	
	
	public static void main(String args[])throws SQLException{
		portList = CommPortIdentifier.getPortIdentifiers();
		while(portList.hasMoreElements()){
			portId = (CommPortIdentifier)portList.nextElement();
			if(portId.getPortType()==CommPortIdentifier.PORT_SERIAL){
				if(portId.getName().equals("COM1")){
					System.out.println("Found "+portId.getName());
					FingerPrint fingerPrint = new FingerPrint();
				}
			}
		}
	}
	
	public FingerPrint()throws SQLException{
		try{
			Class.forName(driver);
		}
		catch(ClassNotFoundException e){
			System.out.println("Driver error");
		}
		conn = DriverManager.getConnection(url, usr, pwd);
		if(!conn.isClosed()){
			System.out.println("Success connected MySQL");
			statement = conn.createStatement();
		}
		
		try{
			serialPort = (SerialPort)portId.open("Sunder", 2000);
		}
		catch(PortInUseException e){
			System.out.println(e);
		}
		try{
			inputStream = serialPort.getInputStream();
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
			serialPort.setSerialPortParams(57600,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
		}
		catch(UnsupportedCommOperationException e){
			System.out.println(e);
		}
		try{
			outputStream = serialPort.getOutputStream();
		}
		catch(IOException e){
			System.out.println(e);
		}
		readThread = new Thread(this);
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
			byte[] readBuffer = new byte[1024];
			int numBytes = 0;
			try{
				while(inputStream.available()>0){
					numBytes = inputStream.read(readBuffer);					
				}
				feedBackString = printHexString(readBuffer);
				if(lastCmd=="upChar"){
					readUpChar += feedBackString.substring(0, numBytes*2);
					if(readUpChar.length()==1136){
						readUpChar = readUpChar.substring(24, 1136);
						int i = 0;
						sql = "INSERT INTO fingerprint VALUES('"+i+"', '左手大拇指', '"+readUpChar+"')";
						try{
							statement.executeUpdate(sql);
							System.out.println("指纹已存入数据库");
						}
						catch(SQLException e){
							System.out.println("插入数据失败"+e.getMessage());
						}
					}
				}
				if(lastCmd=="autoLogin"){
					if(feedBackString.charAt(18)=='0'&&feedBackString.charAt(19)=='0'){
						System.out.println("自动登录成功");
						senMsg(upChar, 2);
					}
					else if(feedBackString.indexOf("02")==18){
						System.out.println("传感器上没有指纹");
					}
					else if(feedBackString.indexOf("06")==18){
						System.out.println("图像太乱，录取失败");
					}
					else if(feedBackString.indexOf("07")==18){
						System.out.println("特征点太少，录取失败");
					}
					else if(feedBackString.indexOf("0a")==18){
						System.out.println("合并失败，不是同一个手指");
					}
					else if(feedBackString.indexOf("0b")==18){
						System.out.println("存储序号超过有效范围");
					}
					else if(feedBackString.indexOf("56")==18){
						System.out.println("第一次采集指纹成功");
					}
					else if(feedBackString.indexOf("57")==18){
						System.out.println("第二次采集指纹成功");
					}
					else if(feedBackString.indexOf("24")==18){
						System.out.println("表示重复而登记失败");
					}
				}
				if(lastCmd=="schTplNum"){
					if(feedBackString.indexOf("01")==18){
						System.out.println("Template number read error!");
					}
					else{
						tplNum = Integer.parseInt(feedBackString.substring(20, 24), 16);
						System.out.println("指纹库中共有"+tplNum+"枚指纹!");
						if(tplNum==1000){
							tplNum = 0;
						}
					}
				}
				if(lastCmd=="schTplNum"){
					autoLogin = "EF01FFFFFFFF010008543602" + addHex(tplNum, 0) + "00" + addHex(tplNum, 0x95);
					senMsg(autoLogin, 1);
				}
				if(feedBackString.indexOf("55")==0){
					System.out.println("模块已上电！！！");
					senMsg(schTplNum, 0);
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
		    String hex = Integer.toHexString(b[i]&0xFF); 
		    if (hex.length()==1){ 
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
            d[i] = (byte)(charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }
	
    private static byte charToByte(char c){
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
    
    private void senMsg(String messageString, int typeCmd){
		try{
			outputStream.write(hexStringToBytes(messageString));
			inputStream = serialPort.getInputStream();
			switch(typeCmd){
	    	case 0:
	    		lastCmd = "schTplNum";
	    		System.out.println("schTplNum command written");
	    		break;
	    	case 1:
	    		lastCmd = "autoLogin";
	    		System.out.println("autoLogin command written");
	    		break;
	    	case 2:
	    		lastCmd = "upChar";
	    		System.out.println("upChar command written");
	    		break;
	    	default:
    			break;
	    	}
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void closeSerialPort(){
		serialPort.close();
	}
	
	public static String addHex(int x, int y){
		String str = Long.toHexString(x+y);
		String xStr = "";
		if(str.length()<4){
			for(int i=0; i<4-str.length(); i++){
				xStr += "0";
			}
			str = xStr + str;
		}
		return str;
	}
}
