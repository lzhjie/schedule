package org.lzhjie.schedule;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;

class PersonalInformationList {
	
	static PersonalInformationList get(String file_name) throws Exception {
		PersonalInformationList list = map.get(file_name);
		if(list == null) {
			Schedule.logger.info("load file: " + file_name);
			list = new PersonalInformationList(file_name);
			map.put(file_name, list);
		}
		return list;
	}
	
	public void InitFromFile(String file_name) throws Exception {
		DocumentBuilderFactory docBuilderFac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFac.newDocumentBuilder();
		Document doc = docBuilder.parse(new File(file_name));
		NodeList nodeList = doc.getElementsByTagName("person");
		for(int i = 0; i < nodeList.getLength(); ++ i) {
			Element elem = (Element) nodeList.item(i);
			PersonalInformation personInfo = new PersonalInformation(elem.getAttribute("name"), elem.getAttribute("telephone"));
			AppendPersonalInformation(personInfo);
		}
	}

	public MyIterator iterator(String name) {
		return new MyIterator(name);
	}
	
	
	private PersonalInformationList(String file_name) throws Exception {
		InitFromFile(file_name);
	}
	private static final Map<String, PersonalInformationList> map = new TreeMap<String, PersonalInformationList>();
	
	private List<PersonalInformation> personal_list = new ArrayList<PersonalInformation>();
	public void AppendPersonalInformation(PersonalInformation person_infomation) {
		personal_list.add(person_infomation);
	}
	class MyIterator implements Iterator<PersonalInformation> {
		MyIterator(String name) {
			for(PersonalInformation personInfo : personal_list) {
				if(personInfo.name().equals(name.trim())) {
					currentIndex = personal_list.indexOf(personInfo);
					break;
				}
			}
			-- currentIndex;
		}
		private int currentIndex = 0;
		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return true;
		}
		@Override
		public PersonalInformation next() {
			// TODO Auto-generated method stub
			if(++currentIndex >= personal_list.size()) {
				currentIndex = 0;
			}
			return personal_list.get(currentIndex);
		}
		public int size() {
			return personal_list.size();
		}
		@Override
		public void remove() {
			// TODO Auto-generated method stub
			
		}
	}
}	
