#!/usr/bin/env python
'''
A fetcher for MyWorkSite raw data.

NOT FOR PUBLIC RELEASE! CONTAINS SENSITIVE API KEYS AND PRIVATE
MYWORKSITE PIDS

Copyright (c) 2013 - Joseph Lewis <joehms22@gmail.com> | <joseph@josephlewis.net>
'''
import smartgrid_api1
import datetime
import urllib2
import re

FEED_ID = 2125
MAX_SAMPLE_POINTS = 10000000
MYWORKSITE_URL_DATE_FORMAT = "%m/%d/%Y"
MYWORKSITE_URL_FETCH_FORMAT = "%m/%d/%Y %I:%M %p"

URL = "http://www.myworksite.com/cascade/excel.asp?siqpid=%s&excelsamples=%s&startbox=%s&endbox=%s"
DEVICE_KEY = "fe42dd19-2bd1-404f-ad7d-65e78ae7ec4d"
FEED_NAME = "MyWorkSite: Olin kwh"

poster = smartgrid_api1.Poster(DEVICE_KEY)

def extract_table(page):
	''' Using splits to pull an HTML page apart, bad usually, but this page
	is EXACTLY the same every time. And this is way easier on the server
	than BeautifulSoup
	'''
	
	tablestart = page.find("</tr>")
	tableend = page.find("</TABLE")
	
	page = page[tablestart + len("</tr>"):tableend]
	page = page.replace("<td>", "")
	page = page.replace("<TR>", "")
	
	return [[s for s in row.split("</td>") if len(s) > 0] for row in page.split("</TR>") if len("".join(row)) != 0]


def fetch_myworksite_feed(feed_id, start_date, end_date):
	'''
	Fetches a feed from the myworksite server and returns a dict of Date/Value points
	'''
	
	start = start_date.strftime(MYWORKSITE_URL_DATE_FORMAT)
	end   = end_date.strftime(MYWORKSITE_URL_DATE_FORMAT)
	
	url = URL % (feed_id, MAX_SAMPLE_POINTS, start, end)
	page = urllib2.urlopen(url).read()
	page = page.replace("\r\n", "")
	
	feed = {}
	
	for row in extract_table(page):
		day = datetime.datetime.strptime(row[0], MYWORKSITE_URL_FETCH_FORMAT)
		value = float(row[1])
		feed[day] = value
	
	return feed

if __name__ == "__main__":
	feed = fetch_myworksite_feed(FEED_ID, datetime.datetime.today() - datetime.timedelta(1), datetime.datetime.today() )
	#feed = fetch_myworksite_feed(FEED_ID, datetime.datetime(2013,02,03,23,45,00), datetime.datetime(2013,03,26,23,45) )
	print poster.submit_and_create(FEED_NAME, feed)
	
	
