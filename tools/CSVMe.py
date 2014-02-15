#!/usr/bin/env python
import csv
import sys
import datetime
import time

if len(sys.argv) == 1:
	print("Usage: %s file [numtokeep]")
	print("\tConverts an iconics table dump to a simple CSV.")
	print("\tnumtokeep - Every numtokeep'th row will be kept (default: 1)")
	exit(1)

iconics_data = csv.reader(open(sys.argv[1]),delimiter=',', quotechar='"')
iconics_data2 = csv.writer(open(sys.argv[1]+"out.csv",'w'))

try:
	tokeep = int(sys.argv[2])
except Exception, e:
	tokeep = 1
	
iconics_data2.writerow(["FeedId","Time","Value"])
for line in iconics_data:
	# Slice up the rows
	index = line[0]
	
	# First col starts at 5, last is 9
	curr = 0
	for i in range(36):
		curr += 1
		if curr % tokeep != 0:
			continue;
		try:
			tm = line[5 + 5 * i]
			val = line[7 + 5 * i]
			tm = tm.strip().replace("\"","").split('.')[0] # trim, remove quotes, and remove .0 that is the milisecond
			val = float(val.strip().replace("\"",""))
			iconics_data2.writerow([index, tm, val])
		except Exception, e:
			print ("Error: "+str(e))
