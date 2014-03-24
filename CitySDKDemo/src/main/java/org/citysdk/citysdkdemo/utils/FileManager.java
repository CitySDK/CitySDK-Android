package org.citysdk.citysdkdemo.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import citysdk.tourism.client.poi.lists.POIS;
import citysdk.tourism.client.poi.single.POI;

import android.content.Context;
import android.util.Log;

final public class FileManager {

	public static final String SEPARATOR = "Foo-";

	public static List<String> listFiles(Context context) {
		
		List<String> returnList = new ArrayList<String>();
		for(String s : Arrays.asList(context.fileList())) {
			if(s.startsWith(SEPARATOR)) {
				returnList.add(s);
			}
		}
		return returnList;
	}

	public static void removeFiles(Context context, String filename) {
		context.getApplicationContext().deleteFile(filename);
	}

	public static void writePoisToFile(Context context, String name, POIS<POI> poi) {
		try {
			
			if(poi == null || poi.size() <= 0) {
				return;
			}
			Log.d("","Write Filename:" + name);
//			FileOutputStream fos = context.openFileOutput(name, Context.MODE_PRIVATE);
//			fos.write(serializeObject(poi));
//			fos.close();
			FileOutputStream fos = context.openFileOutput(name, Context.MODE_PRIVATE);
			ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(poi);
			os.close();
		}
		catch (IOException e) {
			Log.e("Exception", "File write failed: " + e.toString());
		} 
	}

	public static POIS<POI> readPoisFromFile(Context context, String name) {

	//	byte[] b;
		try {
			Log.d("","Read Filename:" + name);

//			FileInputStream fis = context.openFileInput(name);
//			b = org.apache.commons.io.IOUtils.toByteArray(fis);
//			return ((POIS<POI>)deserializeObject(b));		
			FileInputStream fis = context.openFileInput(name);
			ObjectInputStream is = new ObjectInputStream(fis);
			POIS<POI> simpleClass = (POIS<POI>) is.readObject();
			is.close();
			return simpleClass;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 			
		return null;

	}

//	private static byte[] serializeObject(Object o) { 
//		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
//
//		try { 
//			ObjectOutput out = new ObjectOutputStream(bos); 
//			out.writeObject(o); 
//			out.close(); 
//
//			byte[] buf = bos.toByteArray(); 
//
//			return buf; 
//		} catch(IOException ioe) { 
//			Log.e("serializeObject", "error", ioe); 
//
//			return null; 
//		} 
//	} 
//
//
//	private static Object deserializeObject(byte[] b) { 
//		try { 
//			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b)); 
//			Object object = in.readObject(); 
//			in.close(); 
//
//			return object; 
//		} catch(ClassNotFoundException cnfe) { 
//			Log.e("deserializeObject", "class not found error", cnfe); 
//
//			return null; 
//		} catch(IOException ioe) { 
//			Log.e("deserializeObject", "io error", ioe); 
//
//			return null; 
//		} 
//	} 


}
