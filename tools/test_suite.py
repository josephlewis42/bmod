#!/usr/bin/env python

'''
A suite of tests that should all PASS for the given SmartGrid data store
implementation.

All of these ASSUME that the smartgrid.py file works

'''

import unittest
import smartgrid

USER_KEY = "798d5e4f-0da4-497e-8c70-e6dffab2f4eb"
TEST_HOST = "testsmartgrid.cs.du.edu"
API_KEY = "ecb52298-88c5-4b04-bd97-680cbd34102f"

SUCCESS_NOTICE = "The operation was a success."
RANDOM_FEED_NAME = "____RESERVED____"

smartgrid.DEBUGGING = True

class TestFeedAPI(unittest.TestCase):
	DEVICE_NAME = "test_feed_api_device_name"
	
	def setUp(self):
		self.uo = smartgrid.UserOperations(USER_KEY, host=TEST_HOST)
		self.sg = smartgrid.SmartGrid(API_KEY, host=TEST_HOST)
		

	def test_create(self):
		
		
		# delete feed if it exists
		feed_id = self.sg.lookup_feed(RANDOM_FEED_NAME)
		if feed_id != "":
			self.uo.delete_feed(feed_id)
		
		# Make sure the feed doesn't exist already.
		self.assertEqual(self.sg.lookup_feed(RANDOM_FEED_NAME), "")
		
		# Make sure we can create a feed.
		feed_id = self.uo.create_feed(RANDOM_FEED_NAME)
		
		# Make sure the looked up id and the created are the same.
		self.assertEqual(feed_id, self.sg.lookup_feed(RANDOM_FEED_NAME))
		
		# Make sure we can delete the feed.
		self.uo.delete_feed(feed_id)
		self.assertEqual(self.sg.lookup_feed(RANDOM_FEED_NAME), "")
	

	def test_device(self):
		# Make sure we can create a device.
		dev_key = self.uo.get_device_key(self.DEVICE_NAME)
		
		# Make sure we can enable the device.
		self.uo.enable_device(dev_key)
		self.assertEqual(self.uo.device_enabled(dev_key), "True")
		
		# Make sure we can disable the device.
		self.uo.disable_device(dev_key)
		self.assertEqual(self.uo.device_enabled(dev_key), "False")
		
		# Make sure we own the device.
		self.assertEqual(self.uo.own_device(dev_key), "True")
		
		# Make sure we don't own other random devices.
		self.assertEqual(self.uo.own_device("other_random_device"), "False")
		
		# Make sure we can get the device API key
		self.assertEqual(dev_key, self.uo.get_device_key(self.DEVICE_NAME))
	

	def test_device_feed(self):

		feed_id = self.uo.create_feed(RANDOM_FEED_NAME)
		
		dev_key = self.uo.get_device_key(self.DEVICE_NAME)
		
		self.uo.add_device(feed_id, dev_key)
		self.assertEqual(self.uo.device_on_feed(feed_id, dev_key), "True")
		
		self.uo.device_remove_feed(feed_id, dev_key)
		self.assertEqual(self.uo.device_on_feed(feed_id, dev_key), "False")
		
		# Clean up afterward
		self.uo.delete_feed(feed_id)
	

	def test_smartgrid_feed_list(self):
		
		# Check number of feeds.
		orig = self.sg.feeds_list()
		
		# Create a feed
		feed_id = self.uo.create_feed(RANDOM_FEED_NAME)
		
		# make sure it shows up
		new = self.sg.feeds_list()
		
		self.assertTrue(len(orig) == len(new) - 1)
		self.uo.delete_feed(feed_id)
		
	def test_smartgrid_feed_value(self):
		PERSISTANT_FEED_NAME = "__PERSISTANT__"
		# create a test feed.
		feed_id = self.sg.lookup_feed(PERSISTANT_FEED_NAME)
		if feed_id != "":
			self.uo.delete_feed(feed_id)
		
		feed_id = self.uo.create_feed(PERSISTANT_FEED_NAME)
		
		# create a device to add items to the feed.
		dev_key = self.uo.get_device_key(self.DEVICE_NAME)
		self.uo.add_device(feed_id, dev_key)
		
		# post some data
		data = {"2012-11-26 00:00:00":0, "2012-11-26 00:30:00":0,  "2012-11-26 01:00:00":1, "2012-11-26 02:00:00":0 }
		
		with smartgrid.Poster(dev_key, host=TEST_HOST) as output:
			for date, value in data.items():
				output.post(PERSISTANT_FEED_NAME, date, value)
		
		
		# now make sure the given feed values are expected.
		for date, value in data.items():
			self.assertTrue(float(self.sg.feed_value(feed_id, date)) == value)
		
		# now compress the feed values
		self.sg.feed_compress(feed_id)
		
		# now make sure the given feed values are expected, again.
		for date, value in data.items():
			self.assertTrue(float(self.sg.feed_value(feed_id, date)) == value)


if __name__ == '__main__':
	unittest.main()
