#!/usr/bin/env python
'''
Created to change lists like this:
	TERM,CRN,SUBJ,CRSE,TITLE,START DATE,END DATE,BEGIN TIME,END TIME,BLDG,ROOM,M,T,W,R,F,INST LAST NAME,INST FIRST NAME,MAX ENRL,ACTL ENRL
	Autumn Quarter 2011,3099,COMP,1101,Analytical Inquiry I,12-Sep-11,18-Nov-11,1200,1350,OLIN,205,,T,,R,,Edgington,Jeffrey,50,51
	Autumn Quarter 2011,1668,ENGR,1611,Engr Concepts & Practice I,12-Sep-11,18-Nov-11,1000,1050,OLIN,105,,T,,,,Zeles-Hahn,Michelle,72,65
	Autumn Quarter 2011,4474,CHEM,1240,General Chemistry Lab,12-Sep-11,18-Nov-11,1800,2050,OLIN,222,,T,,,,Pegan,Scott,19,14
	...
	
In to this:
	Start DateTime,End DateTime,Room Name,Activity Name, Activity Type,Population
	2011-09-13 12:00:00, 2011-09-13 13:50:00, 205, Analytical Inquiry I 3099 Edgington, Lecture, 52
	2011-09-15 12:00:00, 2011-09-15 13:50:00, 205, Analytical Inquiry I 3099 Edgington, Lecture, 52
	2011-09-20 12:00:00, 2011-09-20 13:50:00, 205, Analytical Inquiry I 3099 Edgington, Lecture, 52
	2011-09-22 12:00:00, 2011-09-22 13:50:00, 205, Analytical Inquiry I 3099 Edgington, Lecture, 52
	2011-09-27 12:00:00, 2011-09-27 13:50:00, 205, Analytical Inquiry I 3099 Edgington, Lecture, 52
	2011-09-29 12:00:00, 2011-09-29 13:50:00, 205, Analytical Inquiry I 3099 Edgington, Lecture, 52
	2011-10-04 12:00:00, 2011-10-04 13:50:00, 205, Analytical Inquiry I 3099 Edgington, Lecture, 52
	2011-10-06 12:00:00, 2011-10-06 13:50:00, 205, Analytical Inquiry I 3099 Edgington, Lecture, 52
	2011-10-11 12:00:00, 2011-10-11 13:50:00, 205, Analytical Inquiry I 3099 Edgington, Lecture, 52
	2011-10-13 12:00:00, 2011-10-13 13:50:00, 205, Analytical Inquiry I 3099 Edgington, Lecture, 52
	2011-10-18 12:00:00, 2011-10-18 13:50:00, 205, Analytical Inquiry I 3099 Edgington, Lecture, 52
	2011-10-20 12:00:00, 2011-10-20 13:50:00, 205, Analytical Inquiry I 3099 Edgington, Lecture, 52
	2011-10-25 12:00:00, 2011-10-25 13:50:00, 205, Analytical Inquiry I 3099 Edgington, Lecture, 52
	2011-10-27 12:00:00, 2011-10-27 13:50:00, 205, Analytical Inquiry I 3099 Edgington, Lecture, 52
	2011-11-01 12:00:00, 2011-11-01 13:50:00, 205, Analytical Inquiry I 3099 Edgington, Lecture, 52
	2011-11-03 12:00:00, 2011-11-03 13:50:00, 205, Analytical Inquiry I 3099 Edgington, Lecture, 52
	2011-11-08 12:00:00, 2011-11-08 13:50:00, 205, Analytical Inquiry I 3099 Edgington, Lecture, 52
	2011-11-10 12:00:00, 2011-11-10 13:50:00, 205, Analytical Inquiry I 3099 Edgington, Lecture, 52
	2011-11-15 12:00:00, 2011-11-15 13:50:00, 205, Analytical Inquiry I 3099 Edgington, Lecture, 52
	2011-11-17 12:00:00, 2011-11-17 13:50:00, 205, Analytical Inquiry I 3099 Edgington, Lecture, 52
	2011-09-13 10:00:00, 2011-09-13 10:50:00, 105, Engr Concepts & Practice I 1668 Zeles-Hahn, Lecture, 66
	...

Meaning that every occurance of a class is turned in to an event a few
hundred classes can easily be turned in to thousands of events making 
this impractical to do by hand.

2011-11-26 Joseph Lewis

'''
import csv
import datetime
import sys

def determine_pop(line):
	'''Returns the population of a class, 1 plus the number of actual
	enrolled. (prof + students)
	
	'''
	return str( int( line['ACTL ENRL'] ) + 1 )


def determine_type(line):
	'''Returns a string for the type of class that occurs in the 
	room, currently Lab or Lecture
	
	'''
	words = line['TITLE'].lower().split()
	
	if "lab" in words:
		return "Lab"
	return "Lecture"
	
	
def determine_name(line):
	'''Returns a standard (unique) name for a course.
	'''
	return line['TITLE'] + " " + line['CRN'] + " " + line['INST LAST NAME']


def determine_room(line):
	return line['ROOM']
	

def determine_dates(line):
	'''Returns start and end times in ISO formatted tuples for every 
	date this class was held i.e.
	
	[("2011-11-11 11:11:00","2011-11-11 13:00:00"),
	...
	]
	
	
	'''
	
	#Get the days this class was held on:
	days_held = []
	
	if line['M']:
		days_held.append(0)
	if line['T']:
		days_held.append(1)
	if line['W']:
		days_held.append(2)
	if line['R']:
		days_held.append(3)
	if line['F']:
		days_held.append(4)
	
	all_days = []
	
	first_date = datetime.datetime.strptime(line["START DATE"], "%d-%b-%y")
	last_date  = datetime.datetime.strptime(line["END DATE"], "%d-%b-%y")	
		
	for i in range(0, (last_date - first_date).days):
		potential_day = first_date + datetime.timedelta(i)
		
		if potential_day.weekday() in days_held:
			all_days.append(potential_day)
	
	# find the start and end hours.
	st_hour = line['BEGIN TIME'][:2]
	st_min  = line['BEGIN TIME'][2:4]
	
	end_hour = line['END TIME'][:2]
	end_min   = line['END TIME'][2:4]
	
	
	times = []
	for t in all_days:
		simpletime = t.strftime("%Y-%m-%d ")
		times.append((simpletime + st_hour + ":" + st_min + ":00", simpletime + end_hour + ":" + end_min + ":00"))
		
	return times


def main(input_file_path, output_file_path):
	""" Reads the file at the input path, and writes csv information to
	the output file, which sould be an open file.
	"""
	
	# Read from stdin if we don't have a path
	input_file = sys.stdin
	if input_file_path != "" and input_file_path is not None:
		input_file = open(input_file_path)
	
	# Print to stdout if we don't have a path
	output_file = sys.stdout
	if output_file_path is not None:
		output_file = open(output_file_path, 'w')
	
	
	# Print the file header information
	output_file.write("Start DateTime,End DateTime,Room Name,Activity Name,Activity Type,Population\n")
	
	# Each line represents a single class in the schedule.
	for line in csv.DictReader(input_file):
		
		# Set up the common fields for each line
		#Room Name,Activity Name, Activity Type,Population
		common_data = determine_room(line) + "," + \
					  determine_name(line) + "," + \
					  determine_type(line) + "," + \
					  determine_pop(line)
		
		for starttime, endtime in determine_dates(line):
			output_file.write(starttime + "," + endtime + "," + common_data + "\n")

def get_path(string):
	'''Gets a path from the given string.'''
	if string == None:
		return None
		
	if string.startswith("'") or string.startswith('"'):
		return string[1:-1]
	
	return string


if __name__ == "__main__":
	try:
		in_path = get_path(sys.argv[1])
	except IndexError:
		print("Usage: parser.py inputpath [outputpath]\n\t If no output path is specified stdout is used.")
		exit(2)
	
	try:
		out_path = get_path(sys.argv[2])
	except IndexError:
		out_path = None
		
	main(in_path, out_path)
