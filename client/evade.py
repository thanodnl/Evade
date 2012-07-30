#!/usr/env/env python
#
import json
import uuid
import socket

from collections import namedtuple

from pprint import pprint

DocumentEntry = namedtuple("DocumentEntry", ["id", "doc"])

class EvadeException(Exception):
  def __init__(self, value):
    self.__value = value

  def __str__(self):
    return self.__value

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
    self.__scounter = 1
    self.__decoder = json.JSONDecoder()
    self.__store = {}
    self.__connect()

  def __connect(self):
    reraise = None
    for host,port in self.__hosts:
      for res in socket.getaddrinfo(host, port, socket.AF_UNSPEC, socket.SOCK_STREAM, 0, socket.AI_PASSIVE):
        try:
          family, socktype, proto, canonname, sockaddr = res

          print "Connecting to", sockaddr
          #self.__server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
          self.__server = socket.socket(family, socktype)
          self.__server.connect(sockaddr)
          print "Connected to", sockaddr
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

  def read(self, collection, key):
    session = self.__get_session()

    req = {'session':session, 'mode':'GET', 'collection':collection, 'key':str(key)}
    while True:
      try:
        self.__server.send(json.dumps(req))
        response = self.__read_response(session)

        if 'error' in response:
          # an error occured
          raise EvadeException(response['error'])

        if 'data' in response:
          return DocumentEntry(uuid.UUID(response['id']), response['data'])
        raise KeyError(key)
      except socket.error:
        # reconnect when connection was lost
        self.__connect()
      except EvadeConnectionLost:
        # reconnect when connection was lost
        self.__connect()

    raise EvadeException("No data in response")

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
            yield DocumentEntry(uuid.UUID(response['id']), response['data'])
          else:
            raise EvadeException("No data in response")
      except socket.error:
        #error while sending
        self.__connect()
      except EvadeConnectionLost:
        #error while receiving
        self.__connect()

  def put(self, collection, key, data):
    session = self.__get_session()
    req = {'session':session, 'mode':'PUT', 'collection':collection, 'key':str(key), 'data':data}

    while True:
      try:
        self.__server.send(json.dumps(req))

        response = self.__read_response(session)
        if 'error' in response:
          # an error occured
          raise EvadeException(response['error'])
        if 'ok' in response:
          return True
        return False
      except socket.error:
        #error while sending
        self.__connect()
      except EvadeConnectionLost:
        #error while receiving
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
          if not obj['session'] in self.__store:
            self.__store[obj['session']] = []
          self.__store[obj['session']].append(obj)
      except ValueError:
        pass
      read = self.__server.recv(1024)
      if len(read) <= 0:  #connection is closed
        raise EvadeConnectionLost()
      else:
        self.__buff += read

    return None

  def __getattr__(self, name):
    if name not in self.__collections:
      self.__collections[name] = EvadeCollection(self, name)
    return self.__collections[name]
    raise AttributeError

class EvadeCollection():
  def __init__(self, evade, name):
    self.__evade = evade
    self.__name = name

  def put(self, key, data):
    return self.__evade.put(self.__name, key, data)

  def where(self, field, value, limit=None):
    return self.__evade.where(self.__name, [field, value], limit)

  def __getitem__(self, key):
    return self.__evade.read(self.__name, key)

def main():
  eva = Evade()
#  pprint(eva.github['0a5b6da1-1ae3-454f-a0d9-7e86b16b2833'])
#  pprint(eva.github['577ec352-8cdb-4a54-a3ab-fa9efd6e4c68'])
  pprint(eva.names['063242ef-1e46-4c77-9353-ba6ab94bbf03'])
  pprint(eva.names[uuid.uuid1()])
#  for found in eva.names.where("name", "z"):
#    pprint(eva.names[found.id])
#    pprint(found.id)
#    pprint(found.doc)
#    print

if __name__ == "__main__":
  main()

