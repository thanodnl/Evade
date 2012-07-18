#
#!/usr/env/env python

import json
import uuid
import socket

from pprint import pprint

class EvadeConnectionLost(Exception):
	def __init__(self, value="Connection has been lost"):
		self.__value = value

	def __str__(self):
		return self.__value

class Evade():
	def __init__(self, hosts=[('localhost', 2225)]):
		self.__hosts = hosts
		self.__collections = {}
		self.__buff = '';
		self.__decoder = json.JSONDecoder()
		self.__scounter = 1
		self.__store = {}
		self.__connect()

	def __connect(self):
		reraise = None
		for host in self.__hosts:
			try:
				print "Connecting to", host
				self.__server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
				self.__server.connect(host)
				print "Connected to", host
				return
			except socket.error as ball:
				reraise = ball
				continue
		if reraise:
			raise reraise

	def __get_session(self):
		session = self.__scounter
		self.__scounter = self.__scounter + 1
		return session

	def read(self,collection, key):
		session = self.__get_session()

		req = {'session':session, 'mode':'GET', 'collection':collection, 'key':key}
		self.__server.send(json.dumps(req))
		response = self.__read_response(session)

		if 'error' in response:
			# an error occured
			raise Exception(response['error'])

		if 'data' in response:
			return response['data']

		raise Exception("No data in response")

	def where(self, collection, query, limit=None):
		session = self.__get_session()

		req = {'session':session, 'mode':'WHERE', 'collection':collection, 'query':query}
		if limit:
			req['limit'] = limit

		while True:
			try:
				self.__server.send(json.dumps(req))
				while True:
					response = self.__read_response(session)

					if 'error' in response:
						# an error occured
						raise Exception(response['error'])

					if 'eof' in response:
						if response['eof']:
							return

					if 'data' in response:
						yield response['data']
					else:
						raise Exception("No data in response")
			except socket.error:
				self.__connect()
			except EvadeConnectionLost:
				self.__connect()

	def __read_response(self, session):
		if session in self.__store:
			if len(self.__store[session]) > 0:
				ret = self.__store[session][0]
				self.__store[session] = self.__store[session][1:]
				if len(self.__store[session]) == 0:
					del self.__store[session]
				return ret

		while True:
			try:
				while True:
					obj, index = self.__decoder.raw_decode(self.__buff)
					self.__buff = self.__buff[index:]
					if obj['session'] == session:
						return obj
					if not session in self.__store:
						self.__store[session] = []
					self.__store[session].append(obj)
			except ValueError:
				pass
			read = self.__server.recv(10224)
			if len(read) <= 0:    #connection is closed
				raise EvadeConnectionLost()
			else:
				self.__buff += read

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

	def where(self, field, value, limit=None):
		return self.__evade.where(self.__name, [field, value], limit)

	def __getitem__(self, key):
		print "fetching", key, "from", self.__name
		return self.__evade.read(self.__name, key)

def main():
	eva = Evade()
	pprint(eva.github['0a5b6da1-1ae3-454f-a0d9-7e86b16b2833'])
	pprint(eva.github['577ec352-8cdb-4a54-a3ab-fa9efd6e4c68'])
	pprint(eva.names['063242ef-1e46-4c77-9353-ba6ab94bbf03'])
	for found in eva.names.where("name","z",limit=10):
		pprint(found)

if __name__ == "__main__":
    main()

