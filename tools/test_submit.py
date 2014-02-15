#!/usr/bin/env python

'''


'''

import unittest
import smartgrid_api1 as smartgrid

USER_KEY = "798d5e4f-0da4-497e-8c70-e6dffab2f4eb"
TEST_HOST = "testsmartgrid.cs.du.edu"
API_KEY = "ecb52298-88c5-4b04-bd97-680cbd34102f"

SUCCESS_NOTICE = "The operation was a success."
RANDOM_FEED_NAME = "____RESERVED____"

smartgrid.DEBUGGING = True


DEVICE_NAME = "test_feed_api_device_name"
	

uo = smartgrid.UserOperations(USER_KEY, host=TEST_HOST)
sg = smartgrid.SmartGrid(API_KEY, host=TEST_HOST)


		

PERSISTANT_FEED_NAME = "__PERSISTANT__"
# create a test feed.
feed_id = sg.lookup_feed(PERSISTANT_FEED_NAME)
if feed_id != "":
	uo.delete_feed(feed_id)

feed_id = uo.create_feed(PERSISTANT_FEED_NAME)

# create a device to add items to the feed.
dev_key = uo.get_device_key(DEVICE_NAME)
uo.add_device(feed_id, dev_key)

# post some data
data = {"2012-11-26 00:00:00":0, "2012-11-26 00:30:00":0,  "2012-11-26 01:00:00":1, "2012-11-26 02:00:00":0, "2012-11-26 03:00:00":1, "2012-11-26 04:00:00":0,"2012-11-26 05:00:00":1, "2012-11-26 06:00:00":0  }

output = smartgrid.Poster(dev_key, host=TEST_HOST)
for date, value in data.items():
	output.post(PERSISTANT_FEED_NAME, date, value)

print output.post_data(output.reqdict)

