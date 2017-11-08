package utils;

public class AppConstants {

	// -----------type request for insert database------
	final public static int TYPE_FIRST_TOKEN_REQUEST = 1;
	final public static int TYPE_SECOND_TOKEN_REQUEST = 2;
	
	// -----------ports---------------------------
	final public static int CLIENT_PORT = 10083;
	final public static int DEVICE_PORT = 10082;

	//----------------------------------------------
	final public static int DEVICE_TYPE = 0;
	final public static int CLIENT_TYPE = 1;

	// -----------type request---------------------

	final public static int TYPE_COMMAND_REQUEST = 1;
	final public static int TYPE_LOCATION_TRACKER_REQUEST = 2;
	final public static int TYPE_ORIENTATION_REQUEST = 3;

	final public static int TYPE_LOG_REQUEST = 20;
	

	//-----------path for log file (getExternalStorageDirectory)-------------------------
	final public static String PATH_TO_LOG_FILE = "/SecLogFile.txt";
	
}
