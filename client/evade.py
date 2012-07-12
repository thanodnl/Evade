#!/usr/env/env python
#

import json
import uuid
import socket

from pprint import pprint

class Evade():
	def __init__(self, host, port):
		self.__addr = (host, port)
		self.__collections = {}
		self.__buff = '';
		self.__decoder = json.JSONDecoder()
		self.__scounter = 1


		self.__server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		self.__server.connect(self.__addr)

	def read(self,collection, key):
		session = self.__scounter
		self.__scounter = self.__scounter + 1

		req = {'session':session, 'mode':'GET', 'collection':collection, 'key':key}
		self.__server.send(json.dumps(req))
		while True:
			response = self.__read_response()
			if response['session'] == session:
				break

		if 'error' in response:
			# an error occured
			raise Exception(response['error'])

		if 'data' in response:
			return response['data']

		raise Exception("No data in response")

	def __read_response(self):
		while True:
			read = self.__server.recv(10224)
			if len(read) <= 0:    #connection is closed
				break
			self.__buff += read
			try:
				obj, index = self.__decoder.raw_decode(self.__buff)
				self.__buff = self.__buff[index:]
				return obj
			except ValueError:
				pass

		return None



	def __getattr__(self,name):
		if name not in self.__collections:
			self.__collections[name] = EvadeCollection(self, name)
		return self.__collections[name]
		raise AttributeError

class EvadeCollection():
	def __init__(self,evade,name):
		self.__evade = evade
		self.__name = name

	def __getitem__(self, key):
		print "fetching", key, "from", self.__name
		return self.__evade.read(self.__name, key)

def main():
	eva = Evade('localhost',2225)
	pprint(eva.github['0a5b6da1-1ae3-454f-a0d9-7e86b16b2833'])
	pprint(eva.github['577ec352-8cdb-4a54-a3ab-fa9efd6e4c68'])
	pprint(eva.github['poep'])

if __name__ == "__main__":
    main()

