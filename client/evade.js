var jsonsp = require("jsonsp");
var net = require("net");

var parser = new jsonsp.Parser();
parser.on('object',function(obj){
  console.log("got: " + JSON.stringify(obj));
});

var client = net.connect(2225, function (){
client.write("{'session':1, 'mode':'WHERE', 'collection':'names', 'query':['name','bazlangton']}");
});
client.on('data', function(data){
	parser.parse(data);
});
