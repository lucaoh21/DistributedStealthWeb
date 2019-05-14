import http.client as h
import sys
import datetime
import threading
import random
servers = ["3.94.170.64",  "3.83.10.22", "3.14.64.40", "13.229.131.254", "35.183.5.27","13.233.35.167","13.233.83.84","3.14.67.164","52.24.143.203","3.94.170.64","54.153.0.234","54.193.125.205", "34.221.171.231"]
docs = ["/doc2.html", "/doc3.html","/doc4.html", "/doc5.html", "/doc6.html","/doc7.html","/doc8.html","/doc9.html","/doc10.html","/doc11.html","/doc12.html","/doc13.html", "/doc14.html"] 
def makeRequest():
	#idx = random.randint(0,len(servers)-1)
	#print(servers[idx])
	start = datetime.datetime.now()
	num_times = 50
	
	for i in range(num_times):	
		doc_idx = random.randint(0, len(docs)-1)
		conn = h.HTTPConnection("3.94.170.64:8080")
		conn.request("GET", docs[doc_idx])
		r1 = conn.getresponse()
		if r1.status != 200:
			print(r1.status, "ERRORRRRR")
	
	end = datetime.datetime.now()
	dif = end - start
	print(dif.total_seconds())
def conncurrent(num):
	for i in range(num):
		t = threading.Thread(target=makeRequest)
		t.start()
def run_exp():
	for i in range(40):
		makeRequest(10 * i)
if __name__ == "__main__":
	conncurrent(int(sys.argv[1]))
