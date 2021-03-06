#run.py
#usage - run.py <apkName> 

import os
import sys

jarfiles = 'soot-trunk.jar:soot-infoflow.jar:soot-infoflow-android.jar:slf4j-api-1.7.5.jar:slf4j-simple-1.7.5.jar:axml-2.0.jar soot.jimple.infoflow.android.TestApps.Test'
platformsDir = "/Users/justinpeterson/Android/android-sdk-macosx/platforms"

def runOnDirectory(dir):
	apkFileDir = dir
	for apkFile in os.listdir(apkFileDir):
		
		apkFile = os.path.join(dir, apkFile)

		apkFilename = os.path.basename(apkFile)
		apkFiletxt = 'output/' + os.path.splitext(apkFilename)[0] + '.txt'

		file = open(apkFiletxt, 'w')
		file.close()	

		os.system('java -cp ' + jarfiles + ' "' + apkFile + '" ' + platformsDir + '> ' + apkFiletxt)

runOnDirectory(sys.argv[1])