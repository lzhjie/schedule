package org.lzhjie.schedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.lzhjie.schedule.XmlConfigLoader.ScheduleList;

class MonthInfo {
	MonthInfo(int year, int month) throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month - 1, start_day);
		Schedule.logger.debug("MonthInfo calendar: " + cal.getTime().toString());
		int day=cal.getActualMaximum(Calendar.DATE);
		int week = cal.get(Calendar.DAY_OF_WEEK);
		for(int i = 0; i < day; ++ i) {
			DayInfo dayInfo = new DayInfo(i + 1);
			WeekInfo weekInfo = WeekInfo.getWeekInfoByWeek(week);
			if(weekInfo == null) {
				weekInfo = WeekInfo.getWeekInfoFirst();
				if(weekInfo == null) {
					throw new Exception("require resgist dayType");
				}
				week = weekInfo.getValue();
			}
			++week;
			dayInfo.setWeek(weekInfo);
			dayInfo.setType(weekInfo.getDayType());
			days.add(dayInfo);
		}
		if(++month > 12) {
			++ year;
			month = 1;
		}
		nextMonthString = String.format("%d-%d", year, month);
	}
	String getNextMonthString() {
		return nextMonthString;
	}
	private String nextMonthString = "";
	static class DayType {
		static DayType registType(String id, String weeks) throws Exception {
			DayType type = map.get(id);
			if(type != null) {
				throw new Exception(String.format("id:%s already regist", id));
			}
			Schedule.logger.info(String.format("new type:%s, weeks:%s", id, weeks));
			type = new DayType(id, weeks);
			map.put(id, type);
			return type;
		}
		static DayType find(String id) {
			return map.get(id);
		}
		private DayType(String id, String weeks) {
			this.id = id;
			WeekInfo weekinfo = null;
			for(String temp : weeks.split(",")) {
				if(temp.trim().length() == 0) {
					continue;
				}
				weekinfo = WeekInfo.getWeekInfoByIndex(Integer.parseInt(temp));
				if(weekinfo != null) {
					weekinfo.setDayType(this);
				}
			}
		}
		public String toString() {
			return id;
		}
		void addList(ScheduleList list) {
			lists.add(list);
		}
		List<ScheduleList> getScheduleList() {
			return lists;
		}
		private String id;
		final Set<Integer> weeksSet = new HashSet<Integer> ();
		final List<ScheduleList> lists = new ArrayList<ScheduleList>();
		static final Map<String, DayType> map = new TreeMap<String, DayType>();
	}
	static class WeekInfo {
		static WeekInfo getWeekInfoByIndex(int index) {
			return mapIndex.get(index);
		}
		static WeekInfo getWeekInfoByWeek(int week) {
			return mapWeek.get(week);
		}
		static WeekInfo getWeekInfoFirst() {
			for(Entry<Integer, WeekInfo> entry : mapWeek.entrySet()) {
				return entry.getValue();
			}
			return null;
		}
		void setDayType(DayType type) {
			this.type = type;
			Schedule.logger.info(this);
		}
		DayType getDayType(){
			return type;
		}
		int getValue() {
			return value;
		}
		private WeekInfo(String name, int value) {
			this.name = name;
			this.value = value;
			mapWeek.put(value, this);
		}
		private String getName() {
			return name;
		}
		public String toString() {
			return String.format("%s(%d)  %s", name, value, type);
		}
		private DayType type = null;
		private String name;
		private int value;
		static final Map<Integer, WeekInfo> mapWeek = new TreeMap<Integer, WeekInfo>();
		static final Map<Integer, WeekInfo> mapIndex = new TreeMap<Integer, WeekInfo>();
		static {
			mapIndex.put(1, new WeekInfo("MONDAY", Calendar.MONDAY));
			mapIndex.put(2, new WeekInfo("TUESDAY", Calendar.TUESDAY));
			mapIndex.put(3, new WeekInfo("WEDNESDAY", Calendar.WEDNESDAY));
			mapIndex.put(4, new WeekInfo("THURSDAY", Calendar.THURSDAY));
			mapIndex.put(5, new WeekInfo("FRIDAY", Calendar.FRIDAY));
			mapIndex.put(6, new WeekInfo("SATURDAY", Calendar.SATURDAY));
			mapIndex.put(7, new WeekInfo("SUNDAY", Calendar.SUNDAY));
		}
	}
	public void changeDayType(String id, String days) throws Exception {
		
		DayType type = DayType.find(id);
		if(type == null) {
			throw new Exception("cannot find dayType, id:" + id);
		}
		Schedule.logger.debug("changetype, type:" + type + " days:"+days);
		DayInfo dayinfo = null;
		for(String temp : days.split(",")) {
			if(temp.trim().length() == 0) {
				continue;
			}
			dayinfo = this.days.get(Integer.parseInt(temp) - 1);
			if(dayinfo == null) {
				throw new Exception("invalid day string: " + days);
			}
			dayinfo.setType(type);
		}
	}
	class DayInfo {
		DayInfo(int day) {
			this.day = day;
		}
		public void setType(DayType type) {
			this.type = type;
		}
		public DayType getType() {
			return type;
		}
		public int getDay() {
			return day;
		}
		public WeekInfo getWeek() {
			return week;
		}
		public void setWeek(WeekInfo week) {
			this.week = week;
		}
		public String toString() {
			return String.format("%4d%12s%16s", day, week.getName(), type);
		}
		private DayType type = null;
		private WeekInfo week = null;
		private int day = 0;
	}
	MyIterator iterator() {
		return new MyIterator();
	}
	class MyIterator implements Iterator<DayInfo> {
		MyIterator() {
			currentIndex = start_day;
			-- currentIndex;
		}
		private int currentIndex = 0;
		@Override
		public boolean hasNext() {
			if(currentIndex + 1 >= days.size()) {
				return false;
			}
			return true;
		}
		@Override
		public DayInfo next() {
			// TODO Auto-generated method stub
			if(currentIndex >= days.size()) {
				return null;
			}
			return days.get(currentIndex ++);
		}
		@Override
		public void remove() {
			// TODO Auto-generated method stub
			
		}
	}
	private List<DayInfo> days = new ArrayList<DayInfo>();
	private int start_day;
	

}
