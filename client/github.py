#!/usr/env/env python
#
import os
import json
import gzip

PATH = '/Users/nilsdijk/githubdata/'

def parse(content):
	decode = json.JSONDecoder()
	idx = 0
	try:
		while True:
			obj,idx = decode.raw_decode(content, idx)
			yield obj
	except:
		pass

def parse_json(fn):
	with open(fn) as f:
		return parse(f.read())

def parse_gzip(fn):
	print 'opening', fn
	f = gzip.open(fn)
	data = f.read()
	f.close()
	return parse(data)

def github_events():
	files = os.listdir(PATH)
	for fn in files:
		if fn.endswith('.gz'):
			for event in parse_gzip(PATH + fn):
				yield event
		if fn.endswith('.json'):
			for event in parse_json(PATH + fn):
				yield event

def count(it):
	c = 0
	for e in it:
		c += 1
	return c

def main():
	print count(github_events()), "github events"

if __name__ == "__main__":
	main()

