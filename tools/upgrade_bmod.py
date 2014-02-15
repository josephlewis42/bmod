#!/usr/bin/env python3
'''

Copyright 2013 Joseph Lewis <joehms22@gmail.com> | <joseph@josephlewis.net>

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
import os
import zipfile
import shutil

BUILDNUMBER_IDENTIFIER = "BuildNo"
BUILDING_MODELER_WEB_PATH = "/home/josephl/smartgrid/bmod_collector/apache/STATIC/bmod_app"
VERSION_DOCUMENT_NAME = os.path.join(BUILDING_MODELER_WEB_PATH, "version")
RENAME_PROGRESSION = [os.path.join(BUILDING_MODELER_WEB_PATH, x) for x in ["Bmod_alpha.jar", "Bmod_beta.jar", "Bmod.jar"]]


def get_build_number(currpath):
	'''Gets the build number of the bmod version at the given path'''
	build = ""
	
	# open the existing zipfile
	with zipfile.ZipFile(currpath) as z:
		manifest = z.open("META-INF/MANIFEST.MF")
		
		manifest_text = str(manifest.read(), encoding='utf8')

		for line in manifest_text.split("\r\n"):
			if line.startswith(BUILDNUMBER_IDENTIFIER):
				build = line.split(":",1)[1]
				build = build.strip()
	
	return build


def update_build_number(buildno):
	'''Updates the build number in the version file.'''
	
	with open(VERSION_DOCUMENT_NAME, 'w') as of:
		of.write(buildno)


def upgrade(currpath):
	last = -1
	for i in reversed(range(len(RENAME_PROGRESSION))):
		if last == -1:
			os.remove(RENAME_PROGRESSION[i])
		else:
			print("Moving {} to {}".format(RENAME_PROGRESSION[i], RENAME_PROGRESSION[last]))
			shutil.copy(RENAME_PROGRESSION[i], RENAME_PROGRESSION[last])
			
		last = i
	
	shutil.copy(currpath, RENAME_PROGRESSION[last])
	update_build_number(get_build_number(RENAME_PROGRESSION[-1]))

			
	
	
	
	


if __name__ == "__main__":
	parser = argparse.ArgumentParser()
	parser.add_argument("file", help="the new building modeler file to process")
	
	args = parser.parse_args()
	
	if(args.file):
		upgrade(args.file)
	
	
