#!/usr/bin/env python
'''
A fetcher for NREL raw data.


NOT FOR PUBLIC RELEASE! CONTAINS SENSITIVE API KEYS
'''
import smartgrid
import subprocess
import os
import urllib2
import datetime

FEED_INFO_PREFIX = "NREL Aurora: "
USER_KEY = "9f8c3d49-fea1-412b-b2b3-52b286665db6"


userOperations = smartgrid.UserOperations(USER_KEY)

'''
# Now we have a list of definitions, we need to fetch our new data.
curr_date = datetime.date.today()

url = "http://midcdmz.nrel.gov/apps/plot.pl?site=STAC&start=20110211\
&edy=" + str(curr_date.day) + "&emo=" + str(curr_date.month) + \
"&eyr="+str(curr_date.year)+"&zenloc=25&amsloc=27&year=" + str(curr_date.year) +\
"&month=" + str(curr_date.month) + "&day=" + str(curr_date.day - 1) + \
"&endyear=" + str(curr_date.year) + "&endmonth=" + str(curr_date.month)\
+ "&endday=" + str(curr_date.day - 1) + "&time=0&inst=3&inst=4&inst=5\
&inst=6&inst=7&inst=8&inst=9&inst=10&inst=11&inst=12&inst=13&inst=14\
&inst=15&inst=16&inst=17&inst=18&inst=19&inst=20&inst=21&inst=22\
&inst=23&inst=24&inst=25&inst=26&inst=27&type=data&first=3&math=0\
&second=-1&value=0.0&global=-1&direct=-1&diffuse=-1&user=0&axis=1"
'''

url = "http://midcdmz.nrel.gov/apps/plot.pl?site=STAC&start=20110211&edy=1&emo=9&eyr=2012&zenloc=25&amsloc=27&year=2012&month=1&day=1&endyear=2012&endmonth=9&endday=1&time=1&inst=5&inst=6&type=data&first=3&math=0&second=-1&value=0.0&global=-1&direct=-1&diffuse=-1&user=0&axis=1"

device_key = userOperations.get_device_key("NREL Fetcher Script")

header = []
firstline = True
with smartgrid.Poster(device_key, user_operations=userOperations) as output:
	for line in open('/home/joseph/Desktop/20110211.csv'): #urllib2.urlopen(url):
		if firstline:
			header = line.split(",")
			header = ["%s%s" % (FEED_INFO_PREFIX, x) for x in header]
			firstline = False
		else:
			line = line.split(",")
			date = line[0].split("/")
			year = int(date[2])
			month = int(date[0])
			day = int(date[1])
			time = line[1]
			fulldate = "%04i-%02i-%02i %s:00" % (year, month, day, time)

			for i in range(2,len(header)):
				output.post(header[i], fulldate, line[i].strip())
