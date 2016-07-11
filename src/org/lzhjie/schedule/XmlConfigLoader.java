package org.lzhjie.schedule;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.lzhjie.schedule.MonthInfo.DayType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

final class XmlConfigLoader {
	XmlConfigLoader() throws Exception {
		InitFromFile(configFileName);
	}
	private void buildSchedule(String fileName) throws Exception {
		File file = new File(fileName);
		if(file.exists() == false) {
			file.createNewFile();
		}
		FileWriter fileWrite = new FileWriter(file);
		MonthInfo.MyIterator iter = monthinfo.iterator();
		MonthInfo.DayInfo dayinfo = iter.next();
		while(dayinfo != null) {
			fileWrite.write(String.format("%32s", dayinfo));
			fileWrite.write(buildFromScheduleList(dayinfo.getType().getScheduleList()));
			fileWrite.write("\r\n");
			dayinfo = iter.next();
		}
		fileWrite.close();
	}
	private String buildFromScheduleList(List<ScheduleList> list) {
		StringBuilder result = new StringBuilder(256);
		for(ScheduleList elem : list) {
			result.append(String.format("%32s", elem.buildPersonInfo()));
		}
		// System.out.println(result.toString());
		return result.toString();
	}
	private MonthInfo monthinfo = null;
	private String date = "";
	private static final String outputFileName = "schedule.txt";
	private static final String configFileName = "schedule.xml";
	private void InitFromFile(String file_name) throws Exception {
		DocumentBuilderFactory docBuilderFac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFac.newDocumentBuilder();
		Document doc = docBuilder.parse(new File(file_name));
		Pattern pattern = Pattern.compile("([0-9]+)-([0-9]+)");
		date = getPropertyValueById(doc, "date", "");
		Matcher matcher = pattern.matcher(date);
		if(matcher.find() != true) {
			throw new Exception(date + ": invalid date format, example:2016-1\r\n" );
		}
		
		Element elem = null;
		Node node = doc.getElementsByTagName("dayType").item(0);
		NodeList nodeList =  node.getChildNodes();
		for(int i = 0; i < nodeList.getLength(); ++ i) {
			node = nodeList.item(i);
			if(node.getNodeName().equals("property")) {
				elem = (Element) node;
				MonthInfo.DayType.registType(elem.getAttribute("id"), elem.getAttribute("value"));
			}
		}
		
		monthinfo = new MonthInfo(Integer.valueOf(matcher.group(1)), Integer.valueOf(matcher.group(2)));
		
		elem = doc.getElementById(matcher.group(1) + "-" + matcher.group(2));
		if(elem != null) {
			NamedNodeMap nameNodeMap = elem.getAttributes();
			for(int i = 0; i < nameNodeMap.getLength(); ++ i) {
				node = nameNodeMap.item(i);
				if(node.getNodeName().equals("id") == false) {
					monthinfo.changeDayType(node.getNodeName(), node.getNodeValue());
				}
			}
		}
		
		nodeList = doc.getElementsByTagName("list");
		for(int i = 0; i < nodeList.getLength(); ++ i) {
			elem = (Element) nodeList.item(i);
			ScheduleList slist = new ScheduleList(elem.getAttribute("file"), elem.getAttribute("startCursor"));
			for(String type : elem.getAttribute("type").split(",")) {
				slist.setType(type.trim());
			}
			slist.setPerson(Integer.parseInt(elem.getAttribute("person")));
		}
		buildSchedule(outputFileName);
		
		File folder = new File("history");
		if(!folder.exists() || !folder.isDirectory())  {
			folder.mkdir();
		}
		copyFile(outputFileName, "history/schedule_" + date + ".txt");
		copyFile(configFileName, "history/schedule_" + date + ".xml");
		setPropertyValueById(doc, "date", monthinfo.getNextMonthString());
		for(int i = 0; i < nodeList.getLength(); ++ i) {
			elem = (Element) nodeList.item(i);
			elem.setAttribute("startCursor", scheduleLists.get(i).nextName());
		}
		Source xmlSource = new DOMSource(doc);
		TransformerFactory factory = TransformerFactory.newInstance();  
		Transformer transformer = factory.newTransformer(); 
		transformer.setOutputProperty(OutputKeys.ENCODING, "GB2312");
		transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doc.getDoctype().getSystemId());
		
		OutputStream stream = new ByteArrayOutputStream();
		Result result = new StreamResult(stream);
		transformer.transform(xmlSource, result);
		System.out.println(stream.toString());
		result = new StreamResult(new File(configFileName)); 
		transformer.transform(xmlSource, result); // 保存dom至目的文件
	}

	class ScheduleList {
		ScheduleList (String file, String cursor) throws Exception {
			PersonalInformationList list = PersonalInformationList.get(file);
			iter = list.iterator(cursor);
			this.cursor = cursor;
			scheduleLists.add(this);
		}
		String buildPersonInfo() {
			StringBuilder info = new StringBuilder(64);
			PersonalInformation personInfo = null;
			for(int i = 0; i<getPersonNum(); ++i) {
				if(i != 0) {
					info.append(",");
				}
				personInfo = iter.next();
				info.append(personInfo.name());
				if(personInfo.tel().length() > 0) {
					info.append(" " + personInfo.tel());
				}
			}
			return info.toString();
		}
		void setPerson(int num) {
			if(num > 0 && num <= iter.size()) {
				personNum = num;
			}
		}
		void setType(String type) throws Exception {
			if(type.length() == 0) {
				return ;
			}
			Schedule.logger.debug("type:" + type);
			DayType dayType = DayType.find(type);
			if(dayType == null) {
				throw new Exception(type + " not define");
			}
			dayType.addList(this);
		}
		int getPersonNum() {
			return personNum;
		}
		String nextName() {
			return iter.next().name();
		}
		String cursor = "";
		private int personNum = 1;
		private PersonalInformationList.MyIterator iter = null;
	}

	static String getPropertyValueById(Document doc, String id, String defaultValue) {
		Element elem = doc.getElementById(id);
		if(elem == null) {
			return defaultValue;
		}
		String value = elem.getAttribute("value");
		if(value == null) {
			return defaultValue;
		}
		return value;
	}
	static void setPropertyValueById(Document doc, String id, String value) {
		Element elem = doc.getElementById(id);
		if(elem != null) {
			elem.setAttribute("value", value);
		}
	}
	private List<ScheduleList> scheduleLists = new ArrayList<ScheduleList>();
	
	static public void copyFile(String oldPath, String newPath) { 
       try { 
           int byteread = 0; 
           File oldfile = new File(oldPath); 
           if (oldfile.exists()) { //文件存在时 
               InputStream inStream = new FileInputStream(oldPath); //读入原文件 
               FileOutputStream fs = new FileOutputStream(newPath); 
               byte[] buffer = new byte[1444]; 
               while ( (byteread = inStream.read(buffer)) != -1) { 
                   fs.write(buffer, 0, byteread); 
               } 
               inStream.close(); 
               fs.close();
           } 
       } 
       catch (Exception e) { 
           System.out.println("复制单个文件操作出错"); 
           e.printStackTrace(); 
       } 
   } 
}
