#!/usr/bin/env python
'''
Fetches information from the eMonitor page.

45 fields

The fields are stored in the following way:
var ct = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0] ;
var power = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0] ;
var va = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0] ;
var statusW = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0] ;
var channelLabel = ["","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""] ;
'''

#http://130.253.25.131/energy.htm?podID=1

import urllib2
import time
import optparse

def parse_num_array(line):
	'''Removes an array of numbers from the given line of text.'''
	last = line.rindex("]")
	first = line.index("[") + 1
	
	return [int(tmp) for tmp in line[first:last].split(",")]

def parse_str_array(line):
	'''Removes an array of strings from the given line of text.'''
	last = line.rindex("]")
	first = line.index("[") + 1
	
	return [tmp[1:-1] for tmp in line[first:last].split(",")]


def main(addr, podID, outputfile):
	values_page = urllib2.urlopen("http://%s/energy.htm?podID=%s" % (addr, podID))
	
	ct = power = va = statusW = channelLabel = None
	
	for line in values_page:
		line = line.strip()
		
		if line.startswith("var ct"):
			ct = parse_num_array(line)
		elif line.startswith("var power"):
			power = parse_num_array(line)
		elif line.startswith("var va"):
			va = parse_num_array(line)
		elif line.startswith("var statusW"):
			statusW = parse_num_array(line)
		elif line.startswith("var channelLabel"):
			channelLabel = parse_str_array(line)
		
	values_page.close()
	
	if not ct or not power or not va or not statusW or not channelLabel:
		raise Exception("Variables not found on fetched page!")
	
		
	t = time.localtime()
	timestamp = time.strftime('%Y-%m-%d %H:%M:%S', t)
	
	
	with open(outputfile, 'a') as output:
		for i in range(len(channelLabel)):
			lbl = channelLabel[i]
			if not lbl:
				lbl = "eMonitor podID%s connection: %s at: %s" % (podID, i, addr)
			output.write("%ss %s,%s,%s\n" % (lbl, "ct", timestamp, ct[i]))
			output.write("%ss %s,%s,%s\n" % (lbl, "power", timestamp, power[i]))
			output.write("%ss %s,%s,%s\n" % (lbl, "va", timestamp, va[i]))
			output.write("%ss %s,%s,%s\n" % (lbl, "statusW", timestamp, statusW[i]))


if __name__ == "__main__":
	usage = "HOSTNAME, podID, OUTPUT"
	parser = optparse.OptionParser(description='Fetches values from an eMonitor', usage=usage)
	#parser.add_option('HOSTNAME', help='the ip/hostname of the eMonitor i.e. 127.0.0.1')
	#parser.add_option('podID', help='the id of the monitor you want (0 usually)')
	#parser.add_option('OUTPUT', help='the path of the file to append output to')
	options, args = parser.parse_args()
	
	main(args[0], args[1], args[2])

