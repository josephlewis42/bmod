#!/usr/bin/env python
'''
Creates a test feed and populate
'''
import smartgrid
import random
import datetime
import math

USER_KEY = "798d5e4f-0da4-497e-8c70-e6dffab2f4eb"
TEST_HOST = "testsmartgrid.cs.du.edu"
DEVICE_NAME = "TEST_FEED_GENERATOR_DEVICE"
START_DATE = datetime.datetime(2000,01,01,0,0,0)
END_DATE = datetime.datetime(2000,01,30,0,0,0) # full month range
OFFSET = datetime.timedelta(0, 60 * 15) # fifteen minutes

TEST_FEEDS = {"TEST FEED - SIN": lambda dt: math.sin((dt.hour + (dt.minute / 60.0)) / 24.0),
			  "TEST FEED - LINE": lambda dt: -1 if dt.hour < 12 else 1,
			  "TEST_FEED - STOCHASTIC": lambda dt: random.randint(0,100)}


uo = smartgrid.UserOperations(USER_KEY, TEST_HOST)

device_key = uo.get_device_key(DEVICE_NAME)
uo.enable_device(device_key)




all_times = []
currdate = START_DATE
while currdate < END_DATE:
	all_times.append(currdate)
	currdate = currdate + OFFSET


for title, fun in TEST_FEEDS.items():
	feed_id = uo.create_feed(title)
	
	uo.add_device(feed_id, device_key)
	
	try:
		with smartgrid.Poster(device_key, host=TEST_HOST) as output:
			for time in all_times:
				output.post(title, time, fun(time))
	except Exception, e:
		print "error on submission"
		print str(e)

print "Each feed should have %s data points now" % (len(all_times))
