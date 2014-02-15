#!/usr/bin/env python
import os
import csv
import sys
import datetime
import time
import argparse


def get_tags_from_file(tag_path):
	''' Returns a map of the tagids to tag names in the given TAGS file.'''
	tags = {}
	
	with open(tag_path) as tp:
		tags_file = csv.reader(tp, delimiter=',', quotechar='"')
		
		# 0 is the tag name, 2 is the tag id
		for line in tags_file:
			tags[line[2].replace("\"","").strip()] = line[0].replace("\"","").strip()
	
	return tags

# Parse Arguments
parser = argparse.ArgumentParser(description='Breaks Up Iconics Data Stores')
parser.add_argument('DATA_PATH', help='The path to the iconics dump.')
parser.add_argument('OUTPUT_PATH', help='The path of the parsed data.')
args = parser.parse_args()

if not os.path.isdir(args.DATA_PATH) or not os.path.isdir(args.OUTPUT_PATH):
	print "Either the data_path or the output_path do not exist"
	exit()


# For file in data_path, check to see if it is one we want.
notes_files = {}
info_files = {}
data_files = {}
tags_files = {}

created_files = []

for root, dirs, files in os.walk(args.DATA_PATH):
	for f in files:
		fullpath = os.path.join(root, f)
		try:
			shortname = f[:f.rindex("_")]
		except ValueError:
			continue
		
		print "Checking: %s as %s" % (f, shortname) 
		
		if fullpath.endswith("Notes.csv"):
			notes_files[shortname] = fullpath
		elif fullpath.endswith("Info.csv"):
			info_files[shortname] = fullpath
		elif fullpath.endswith("Tags.csv"):
			tags_files[shortname] = fullpath
		else:
			data_files.setdefault(shortname,[]).append(fullpath)

for tag, tag_path in tags_files.items():
	
	# Read the tags file
	tags = get_tags_from_file(tag_path)
	tags_file = csv.reader(open(tag_path),delimiter=',', quotechar='"')
	
	print "Feeds for: %s" % (tag)
	for line in tags_file:
		# 0 is the tag name, 2 is the tag id
		tags[line[2].replace("\"","").strip()] = line[0].replace("\"","").strip()
		print "\t%s -> %s" % (line[2], line[0])
	
	for data_file_path in data_files[tag]:
		print "%s -> %s" % (tag, data_file_path)
	
		outpath = os.path.join(args.OUTPUT_PATH, tag + ".csv")
		created_files.append(outpath)

		iconics_data2 = csv.writer(open(outpath,'a'))
		
		iconics_data = csv.reader(open(data_file_path),delimiter=',', quotechar='"')
	

		for line in iconics_data:
			# Slice up the rows
			index = line[0]
	
			# First col starts at 5, last is 9
			curr = 0
			for i in range(36):
				curr += 1
				
				try:
					tm = line[5 + 5 * i]
					val = line[7 + 5 * i]
					tm = tm.strip().replace("\"","").split('.')[0] # trim, remove quotes, and remove .0 that is the milisecond
					strval = val.strip().replace("\"","")
					val = float(strval)
					iconics_data2.writerow([tags[index], tm, val])
				except ValueError, e:
					pass
				except KeyError, e:
					iconics_data2.writerow(["Unindexed: %s" % index, tm, val])
			
					
open_files = []
def open_csv_cleanup_file(path):
	try:
		tmp = open(path, 'w')
		open_files.append(tmp)
	
		writer = csv.writer(open(path,'w'))
		writer.writerow(["FeedId","Time","Value"])
		return writer
	except IOError, e:
		print open_files
					
# Do some cleanup to make things more human friendly
#for created_file in created_files:
for root, dirs, files in os.walk(args.OUTPUT_PATH):
	for f in files:
		created_file = os.path.join(root, f)
		print "Working with: %s" % created_file

		date_outputs = {}
		created_file_unfinished_path = created_file[:-4] + "_"
		with open(created_file, 'r') as created:
			csv_input = csv.reader(created, delimiter=',', quotechar='"')
		
			for line in csv_input:
				shortdate = line[1][:7]
				if shortdate not in date_outputs.keys():
					date_outputs[shortdate] = open_csv_cleanup_file(created_file_unfinished_path + shortdate + ".csv")
				date_outputs[shortdate].writerow(line)
		# delete the original file
		os.remove(created_file)
		
		# close the opened files
		for path in open_files:
			path.close()
	
		# close all of our opened files
		#for path in date_outputs.values():
		#	path.close()
		
