#!/usr/bin/env python
'''
A library that is used to access the SmartGrid website.

Copyright 2012 - Joseph Lewis <joehms22@gmail.com>

Version 2012-12-03 -- API 1.0 support.
'''

import urllib2
import urllib
import csv
import datetime
import httplib
import dateutil.parser

DEFAULT_HOST = "smartgrid.cs.du.edu"
BULK_POINT_FORCE_SUBMIT_NUM = 1000
DEBUGGING = False

class SmartGrid:
	api_key = ""
	host = ""
	
	def __init__(self, api_key, host=DEFAULT_HOST):
		''' Sets up the connection with the given user key. '''
		self.api_key = api_key
		self.host = host
	
	def _fetch_url(self, url, data=None, timeout=30):
		if DEBUGGING:
			print url
		return urllib2.urlopen(url, data, timeout).read()
	
	def lookup_feed(self, feed_name):
		''' Returns the id of the feed with the given name.'''
		return self._fetch_url("http://%s/feed/api/1/lookup/%s/?key=%s" % (self.host, urllib.quote(feed_name), self.api_key))
		
	def feed_compress(self, feed_id):
		''' Suggests the given feed be compressed. '''
		return self._fetch_url("http://%s/feed/api/1/compress/%s/" % (self.host, feed_id))
	
	def feeds_list(self):
		'''Returns: A list of feeds that are accessible. 
		Throws:
			Exception if server can't be contacted, or feed isn't available.
		'''
		
		tmp = self._fetch_url("http://%s/feed/api/1/list_all/csv/?key=%s" % (self.host, self.api_key))

		feeds_cache = []
		for d in csv.DictReader(tmp.split("\n")):
			feeds_cache.append(d)
		
		return feeds_cache
		
	def feed_value(self, feed, time, range_m=180):
		'''Fetches the closest value for this feed at the given datetime.
		
		Params:
			feed - One of the feeds from the "fetch_feeds_list" or a number.
			time - a datetime.datetime or an ISO string in the format "YYYY-MM-DD HH:MM:SS"
			range_m - the range of time to find the closest value in (in minutes)
		Returns:
			A float.
		Throws:
			Exception if server can't be contacted, or feed isn't available.
			
		Note that times will be translated to local server time, currently Mountain.
		'''
		if isinstance(feed, int):
			feed = str(feed)
		elif isinstance(feed, str):
			pass
		else:
			feed = feed['ID']
		
		if not isinstance(time, str):
			time = time.isoformat(' ')

		time = urllib.quote(time)

		tmp = float(self._fetch_url("http://%s/feed/api/1/closest/%s/%s/%s/?key=%s" % (self.host, feed, range_m, time, self.api_key)))
		return tmp
		
	def feed_values(self, feed, time1, time2):
		'''Fetches all of the values for this feed over the given two datetimes.
		Returned as a dict {Time -> Value}.
		
		Params:
			feed - One of the feeds from the "fetch_feeds_list" or a number.
			time1 - a datetime.datetime or an ISO string in the format "YYYY-MM-DD HH:MM:SS"
			time2 - a datetime.datetime or an ISO string in the format "YYYY-MM-DD HH:MM:SS"
		Returns:
			A list of dicts in the format:
			{'Date': <datetime.datetime>, 'FeedID': 745, 'Value': 1.0}
		Throws:
			Exception if server can't be contacted, or feed isn't available.
			
		Note that times will be translated to local server time, currently Mountain.
		'''
		
		if isinstance(feed, int):
			feed = str(feed)
		else:
			feed = feed['ID']
		
		if not isinstance(time1, str):
			time1 = time1.isoformat(' ')

		if not isinstance(time2, str):
			time2 = time2.isoformat(' ')
		
		time1 = urllib.quote(time1)
		time2 = urllib.quote(time2)

		tmp = self._fetch_url("http://%s/feed/%s/csv/%s/%s/?key=%s" % (self.host, feed, time1, time2, self.api_key))
		output = []
		for d in csv.DictReader(tmp.split("\n")):
			d['Date'] = dateutil.parser.parse(d['Date'])
			d['FeedID'] = int(d['FeedID'])
			d['Value'] = float(d['Value'])
			output.append(d)
		return output

feed_ids = {}


class UserOperations:
	''' Operations that the user does. '''
	user_key = ""
	host = ""
	
	def __init__(self, user_key, host=DEFAULT_HOST):
		self.user_key = user_key
		self.host = host
	
	def _fetch_url(self, url, data=None, timeout=30):
		if DEBUGGING:
			print url
		return urllib2.urlopen(url, data, timeout).read()
		
	def get_device_key(self, device_name):
		''' Gets the key associated with the device name. If the device doesn't 
		exist, creates it.
		'''	
		
		device_name = urllib.quote(device_name)
		return self._fetch_url("http://%s/device/api/1/get_key/%s/%s/" % (self.host, device_name, self.user_key))
	
	def enable_device(self, device_key):
		''' Enables the device with the given key if possible.'''
		self._fetch_url("http://%s/device/api/1/enable/%s/%s/" % (self.host, device_key, self.user_key))
	
	def disable_device(self, device_key):
		''' Enables the device with the given key if possible.'''
		self._fetch_url("http://%s/device/api/1/disable/%s/%s/" % (self.host, device_key, self.user_key))
	
	def own_device(self, device_key):
		''' Checks that you own the given device. '''
		return self._fetch_url("http://%s/device/api/1/owned_by/%s/%s/" % (self.host, device_key, self.user_key))
		
	def device_enabled(self, device_key):
		''' Checks that the given device is enabled. '''
		return self._fetch_url("http://%s/device/api/1/enabled/%s/" % (self.host, device_key))
		
	def create_feed(self, feed_name):
		'''Creates a feed with the given name, if possible.'''
		return self._fetch_url("http://%s/feed/api/1/create/%s/%s/" % (self.host, urllib.quote(feed_name), self.user_key))
	
	def delete_feed(self, feed_id):
		'''Deletes a feed with the given id.'''
		return self._fetch_url("http://%s/feed/api/1/delete/%s/%s/" % (self.host, urllib.quote(feed_id), self.user_key))
	
	def add_device(self, feed_id, device_key):
		''' Allows the feed with the given id, to be edited with the device with the given key. '''
		self._fetch_url("http://%s/device/api/1/add_feed/%s/%s/%s/" % (self.host, device_key, feed_id, self.user_key))
	
	def device_remove_feed(self, feed_id, device_key):
		''' Removes the given feed to the given device. '''
		self._fetch_url("http://%s/device/api/1/remove_feed/%s/%s/%s/" % (self.host, device_key, feed_id, self.user_key))
	
	def device_on_feed(self, feed_id, device_key):
		''' Checks to see if the feed can be edited by the given device. '''
		return self._fetch_url("http://%s/device/api/1/on_feed/%s/%s/" % (self.host, device_key, feed_id))





def chunks(l, n):
    return [l[i:i+n] for i in range(0, len(l), n)]

class Poster:
	'''Poster posts the data feed you have. The first value is the User Id

	with smartgrid.Poster("aaaaaaaa-aaaa-aaaa-b2b3-52b286665db6") as output:

	If a UserOperations class is supplied, then much more can be done, i.e. feeds are
	auto-created and added when they don't exist.

	'''
	device_key = ""
	recnum = 0
	fnum = 0
	reqdict = {}
	userOperations = None
	
	def _fetch_url(self, url, data=None, timeout=30):
		if DEBUGGING:
			print url
			print data
		return urllib2.urlopen(url, data, timeout).read()
	
	def __init__(self, device_key, user_operations=None, host=DEFAULT_HOST):
		self.device_key = device_key
		self.userOperations = user_operations
		self.host = host
		
	def post_data(self, data):
		data = urllib.urlencode(data, doseq=True)
		return self._fetch_url("http://%s/feed/api/1/submit/%s/?&prof=" % (self.host, self.device_key), data)
	
	def submit_and_create(self, feed_name, date_value_dict):
		''' Calls the /feed/api/1/submit_and_create/ method on the server,
		feed_name must be the name of a feed, and date_value_dict should 
		be a dict of datetime->floats or equivilant that are to be submitted.
		
		Example:
		
		>>> submit_and_create("TestFeed 123", {<datetime>:123.45, <datetime2>:372.13...})
		
		'''
		
		url = "http://" + self.host + "/feed/api/1/submit_and_create/?device=%s&feed=%s" % (urllib.quote(self.device_key), urllib.quote(feed_name))
		
		errors = ""
		for chunk in chunks(date_value_dict.items(), BULK_POINT_FORCE_SUBMIT_NUM):
			submit_dict = {}
		
			i = 1
			for date, val in chunk:
				submit_dict['d%s' % (i,)] = datetime.datetime.strftime(date, '%Y-%m-%d %H:%M:%S')
				submit_dict['v%s' % (i,)] = val
				i += 1
		
			data = urllib.urlencode(submit_dict, doseq=True)
			errors += self._fetch_url(url, data)
		return errors
		
	
	def post(self, feed_name, feed_date, feed_value):
		global feed_ids
		
		feed_name = feed_name.replace("/","per").strip()

		if not feed_ids.has_key(feed_name):
			
			print "Looking up: %s" % (feed_name)
			getfeed = "http://"+self.host+"/feed/api/1/lookup/"+urllib.quote(feed_name)+"/?device="+self.device_key
			result = self._fetch_url(getfeed)
			
			if result == "" and self.userOperations != None:
				result = self.userOperations.create_feed(feed_name)
				self.userOperations.add_device(result, self.device_key)
				
			elif result == "":
				raise Exception("The feed: %s doesn't exist, and can't add it." % (feed_name))
				
			feed_ids[feed_name] = result
		
		try:
			float(feed_value)
			self.recnum += 1
			if isinstance(feed_date, datetime.datetime):
				feed_date = datetime.datetime.strftime(feed_date, '%Y-%m-%d %H:%M:%S')
			self.reqdict['d' + str(self.recnum)] = feed_date
			self.reqdict['v' + str(self.recnum)] = feed_value
			self.reqdict['f' + str(self.recnum)] = feed_ids[feed_name]
			#print self.recnum
			
		except Exception, e:
			print e
		
		if self.recnum == BULK_POINT_FORCE_SUBMIT_NUM:
			if DEBUGGING:
				print urllib.urlencode(self.reqdict)
				
			self.post_data(self.reqdict)
			self.reqdict = {}
			self.recnum = 0
		
	
	def __enter__(self):
		print "entering"
		return self
		
	def __exit__(self, type, value, traceback):
		print "posting data"
		self.post_data(self.reqdict)
