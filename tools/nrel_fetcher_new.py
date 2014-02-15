#!/usr/bin/env python
'''
A fetcher for MyWorkSite raw data.

NOT FOR PUBLIC RELEASE! CONTAINS SENSITIVE API KEYS

Copyright (c) 2013 - Joseph Lewis <joehms22@gmail.com> | <joseph@josephlewis.net>
'''
import smartgrid_api1
import datetime
import urllib2
import re
import collections

NREL_URL_FETCH_FORMAT = "%m/%d/%Y %H:%M"

DEVICE_KEY = "bfd2a317-5762-4555-abcc-34328a4928d2"
NREL_PREFIX = "NREL Aurora: "

def fetch_nrel_feeds(start_date, end_date):
	'''
	Fetches a feed from the NREL site.
	'''
	
	fmt = {
		'end_day':end_date.day,
		'end_month':end_date.month,
		'end_year':end_date.year,
		'start_day':start_date.day,
		'start_month':start_date.month,
		'start_year':start_date.year
	}
	
	url =	("http://midcdmz.nrel.gov/apps/plot.pl?site=STAC&start=20110211&edy={end_day}&emo={end_month}&eyr={end_year}"
			"&zenloc=25&amsloc=27&year={start_year}&month={start_month}&day={start_day}&endyear={end_year}&endmonth={end_month}"
			"&endday={end_day}&time=0&inst=3&inst=4&inst=5" 
			"&inst=6&inst=7&inst=8&inst=9&inst=10&inst=11&inst=12&inst=13&inst=14" 
			"&inst=15&inst=16&inst=17&inst=18&inst=19&inst=20&inst=21&inst=22"  
			"&inst=23&inst=24&inst=25&inst=26&inst=27&type=data&first=3&math=0" 
			"&second=-1&value=0.0&global=-1&direct=-1&diffuse=-1&user=0&axis=1")
			
	url = url.format(**fmt)
	page = urllib2.urlopen(url).read()
	
	
	
	header = None
	feeds = None
	for row in page.split("\n"):
		row = row.split(",")
		time = [x.strip() for x in row[:2]] # clear whitespace in the cells
		cols = [x.strip() for x in row[2:]]
		
		if header == None:
			header = [NREL_PREFIX + x.replace('/','per') for x in cols]
			print header
			feeds = {}
			for title in header:
				feeds[title] = dict()
			continue
		
		
		time = " ".join(time)
		if len(time.strip()) == 0:
			continue
		time = datetime.datetime.strptime(time, NREL_URL_FETCH_FORMAT)
		
		for index, val in enumerate(cols):
			feeds[header[index]][time] = float(val)

	return feeds
		

if __name__ == "__main__":
	poster = smartgrid_api1.Poster(DEVICE_KEY)
	today = datetime.datetime.today()
	yesterday = datetime.datetime.today() - datetime.timedelta(1)
	feeds = fetch_nrel_feeds(yesterday, today)
	
	for name, data in feeds.items():
		print name + " points: " + str(len(data))
		print poster.submit_and_create(name, data)
