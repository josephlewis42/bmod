#!/usr/bin/env python

from pyevolve import *
import math
import smartgrid
import datetime

API_KEY = "8cd480dd-9f00-475a-8d85-8ef87d06a928"
print("Setting up smartgrid")
sg = smartgrid.SmartGrid(API_KEY)

error_accum = Util.ErrorAccumulator()

'''
# August
MONTH_HIGH = 87.1
MONTH_AVERAGE = 70.3
MONTH_LOW = 53.5	

'''

PREV_BESTS = [
" gp_add(gp_add(gp_time_after(10, 16, 1), gp_div(gp_add(hours_until_noon, gp_sub(gp_time_after(gp_time_after(gp_div(10, 15), 12, 23), MONTH_LOW, gp_sub(gp_time_after(10, 23, hours_until_noon), MONTH_AVERAGE)), 23)), 9)), gp_sub(MONTH_AVERAGE, gp_div(gp_div(gp_add(gp_time_after(gp_sub(gp_time_after(MONTH_AVERAGE, 10, 23), 5), hours_until_noon, MONTH_AVERAGE), MONTH_AVERAGE), gp_sub(x, gp_time_after(gp_time_after(gp_sub(3, 10), 1, gp_time_after(10, 23, hours_until_noon)), gp_time_after(gp_add(10, hours_until_noon), gp_mul(4, 5), 11), 3))), 9)))",
"gp_add(gp_add(gp_add(gp_sub(gp_mul(gp_mul(hours_until_noon, hours_until_noon), gp_div(hours_until_noon, gp_add(gp_add(hours_until_noon, hours_until_noon), MONTH_LOW))), hours_until_noon), MONTH_LOW), x), x)",
"gp_add(gp_add(MONTH_AVERAGE, gp_div(gp_sub(gp_sub(gp_div(gp_time_after(gp_div(23, hours_until_noon), gp_sub(20, MONTH_HIGH), gp_mul(12, 12)), gp_time_after(20, gp_div(23, MONTH_AVERAGE), hours_until_noon)), gp_time_after(gp_add(gp_div(16, hours_until_noon), 23), gp_time_after(20, 23, 20), gp_mul(gp_time_after(8, 8, 16), gp_div(23, hours_until_noon)))), hours_until_noon), gp_time_after(hours_until_noon, x, hours_until_noon))), x)"
]


MONTH_HIGH = 83.2
MONTH_AVERAGE = 66.2
MONTH_LOW = 49.2	

def gp_add(a, b):
	return a+b
def gp_sub(a,b):
	return a-b
def gp_mul(a, b):
	return a*b
def gp_div(a,b):
	'''
	"Safe" division, if divide by 0, return 1.
	'''
	if b == 0:
		return 1.0
	else:
		return a/(b*1.0)

def gp_time_after(a, b, c):
	if x > a:
		return b
	return c

'''def gp_if_x_gt_eleven(a,b):
	if x > 11:
		return a
	else:
		return b

def gp_pow(a,b):
	try:
		return a ** b
	except OverflowError:
		return float("inf")'''

		

x = 0
hours_until_noon = 0

def eval_func(chromosome, printing=False, year=2012, month=8,day=26):
	global error_accum
	global x
	global hours_until_noon
	error_accum.reset()
	code_comp = chromosome.getCompiledCode()
	if printing:
		msg = "Desired, Actual"
		for x in range(len(PREV_BESTS)):
			msg += ", Prev Best %s" % (x)
		print(msg)
	for x in range(0, 23, 1):
		hours_until_noon = 12 - x
		evaluated = eval(code_comp)
		
		#target = x**2 + x + 1
		target = sg.feed_value(2818, datetime.datetime(year,month,day,x,0,0))
		
		if printing:
			msg = "%s, %s" % (target, evaluated)
			for prev in PREV_BESTS:
				msg += ", %s" % eval(prev)
			print(msg)

		error_accum += (target, evaluated)
		
	for x in range(0, 23, 1):
		hours_until_noon = 12 - x
		evaluated = eval(code_comp)
		
		#target = x**2 + x + 1
		target = sg.feed_value(2818, datetime.datetime(year,month,day - 1,x,0,0))
		
		if printing:
			msg = "%s, %s" % (target, evaluated)
			for prev in PREV_BESTS:
				msg += ", %s" % eval(prev)
			print(msg)

		error_accum += (target, evaluated)

	return error_accum.getRMSE()
'''  
def write_to_img(coords, tour, img_file):
	""" The function to plot the graph """
	padding=20
	coords=[(x+padding,y+padding) for (x,y) in coords]
	maxx,maxy=0,0
	for x,y in coords:
		maxx, maxy = max(x,maxx), max(y,maxy)
	maxx+=padding
	maxy+=padding
	img=Image.new("RGB",(int(maxx),int(maxy)),color=(255,255,255))
	font=ImageFont.load_default()
	d=ImageDraw.Draw(img);
	num_cities=len(tour)
	for i in range(num_cities):
		j=(i+1)%num_cities
		city_i=tour[i]
		city_j=tour[j]
		x1,y1=coords[city_i]
		x2,y2=coords[city_j]
		d.line((int(x1),int(y1),int(x2),int(y2)),fill=(0,0,0))
		d.text((int(x1)+7,int(y1)-5),str(i),font=font,fill=(32,32,32))

	for x,y in coords:
		x,y=int(x),int(y)
		d.ellipse((x-5,y-5,x+5,y+5),outline=(0,0,0),fill=(196,196,196))
	del d
	img.save(img_file, "PNG")
	print "The plot was saved into the %s file." % (img_file,)
'''

def main_run():
	genome = GTree.GTreeGP()
	genome.setParams(max_depth=8, method="ramped")
	genome.evaluator.set(eval_func)
	ga = GSimpleGA.GSimpleGA(genome)

	# x, 1, and the average high, average, and average low temperatures for the given month.
	ga.setParams(gp_terminals = ['x', '1','2','3','4','5','6','7','8','9','10','11','12','13','14','15','16','17','18','19','20','23','22','23', 'hours_until_noon', 'MONTH_LOW','MONTH_AVERAGE','MONTH_HIGH'], gp_function_prefix = "gp")

	ga.setMinimax(Consts.minimaxType["minimize"])
	ga.setGenerations(1000)
	ga.setMutationRate(0.08)
	ga.setCrossoverRate(1.0)
	ga.setPopulationSize(100)
	#ga.setPopulationSize(10)
	ga.evolve(freq_stats=5)
	best = ga.bestIndividual()
	print best
	
	eval_func(best, printing=True)
	

class FakeChromosome:
	def __init__(self, code):
		self.code = code
	def getCompiledCode(self):
		return self.code

if __name__ == "__main__":

	print("Running main")
	main_run()
	
	#eval_func(FakeChromosome("1.0"), printing=True)
