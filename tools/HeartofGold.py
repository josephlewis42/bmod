#!/usr/bin/env python
'''

Copyright 2012 Joseph Lewis <joehms22@gmail.com> | <joseph@josephlewis.net>

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

* Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above
  copyright notice, this list of conditions and the following disclaimer
  in the documentation and/or other materials provided with the
  distribution.
* Neither the name of the  nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


'''

import argparse
import datetime
import smartgrid



USER_KEY = "9f8c3d49-fea1-412b-b2b3-52b286665db6"
API_KEY = ""

__author__ = "Joseph Lewis"
__copyright__ = "Copyright 2012, Joseph Lewis"
__license__ = "BSD"
__version__ = ""

userOperations = smartgrid.UserOperations(USER_KEY)
sg_api = smartgrid.SmartGrid("4dac08bc-e731-4b16-b04f-da6adb13167e")

def str_to_datetime(string):
	''' Converts a string to a datetime. '''
	string = string.replace("T", " ")
	# Try local time first
	return datetime.datetime.strptime(string, '%Y-%m-%d %H:%M:%S')
	# Try ISO time


def std_dev(numbers):
	''' Returns the mean and standard deviation of the numbers.
	mean, std_dev
	'''
	mean = float(sum(numbers)) / len(numbers)
	sum_squares = 0
	for x in numbers:
		sum_squares += (x - mean) ** 2
	return mean, (sum_squares / (len(numbers) - 1)) ** .5
	
def get_url(opportunities, loc):
	''' Gets the url representing the feed with the given opportunities at the 
	given location, will display the last five, and the next five, if possible.
	'''
	
	first_loc = loc - 5
	last_loc = loc + 5
	
	if first_loc < 0:
		first_loc = 0
	
	if last_loc >= len(opportunities):
		last_loc = len(opportunities) - 1
	
	first = opportunities[first_loc]['Date']
	last = opportunities[last_loc]['Date']
	feed = opportunities[loc]['FeedID']
	
	return "http://smartgrid.cs.du.edu/feed/%s/plot/%s/%s/" % (feed, str(first).replace(' ', "%20"), str(last).replace(' ', "%20"))
	


def standard_deviation_checker(opportunities):
	''' Checks to see if any jump is more than 3 std deviations outside normal
	jumps.
	'''
	# For date in opportunities
	values = [x['Value'] for x in opportunities]
	
	jumps = []
	for i in range(len(opportunities) - 1):
		jumps.append(values[i] - values[i + 1])
	jumps.append(jumps[-1])
	
	mean, dev = std_dev(jumps)
	
	for i in range(len(opportunities)):
		if jumps[i] < mean - 3 * dev or jumps[i] > mean + 3 * dev:
			print "Potential error in value: %s" % (opportunities[i]['Date'])
			print "\t%s" % get_url(opportunities, i)


if __name__ == "__main__":
	parser = argparse.ArgumentParser()
	parser.add_argument("StartDate", help="Date to start checking in format: YYYY-MM-DD HH:MM:SS")
	parser.add_argument("EndDate", help="Date to end checking in format: YYYY-MM-DD HH:MM:SS")
	parser.add_argument("FeedID", help="the feed to run diagnostics on", type=int)
	args = parser.parse_args()
	
	start_date = args.StartDate
	end_date = args.EndDate
	feed = args.FeedID
	
	opportunities = sg_api.feed_values(feed, start_date, end_date)
	opportunities = sorted(opportunities, key=lambda k: k['Date']) 
	standard_deviation_checker(opportunities)
	
	
	
