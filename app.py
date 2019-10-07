# -*- coding: utf-8 -*-
"""
Created on Sun Oct  6 16:40:27 2019

@author: 59474
"""

import os
import time
import sys

if(len(sys.argv) == 2): 
    input_file = sys.argv[1]
    
output_filename = "output.txt"
output_file = "qwe"

#print(os.getcwd())
for i in range(0,50):
    time.sleep(1)
    output_file += str(i)
    #print(output_file)
    print output_file
with open(output_filename, mode='w+') as file:
    file.write(output_file)
