#include <stdlib.h>
#include <stdio.h>
#include <string>

using namespace std;

int main(int argc, char* argv[])
{
	string param1, param2;

	if (argc == 3)
	{
		param1 = argv[1];
		param2 = argv[2];
	}

	string dirChange(argv[0]);
	dirChange = dirChange.substr(0,dirChange.find_last_of("/"));
	
	string command = "START javaw -cp lib/swt/swt.jar;. -Djava.library.path=./lib/rxtx;./lib/swt jifi.Jifi";

	command += " " + param1 + " " + param2;

	//printf("%s\n",command.c_str());
	return system(command.c_str());
}