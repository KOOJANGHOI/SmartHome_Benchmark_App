#include <iostream>
#include <vector>
#include "StructC.hpp"

int main(int argc, char *argv[]) {

	data testdata;
	testdata.name = "Rahmadi";
	testdata.value = 0.123;
	testdata.year = 2016;

	/*cout << "Name: " << testdata.name << endl;
	cout << "Value: " << testdata.value << endl;
	cout << "Year: " << testdata.year << endl;*/

	vector<data> dataset;
	data testdata2;
	testdata2.name = "Trimananda";
	testdata2.value = 0.223;
	testdata2.year = 2017;
	
	dataset.push_back(testdata);
	dataset.push_back(testdata2);

	for (data dat : dataset) {

		cout << "Name: " << dat.name << endl;
		cout << "Value: " << dat.value << endl;
		cout << "Year: " << dat.year << endl;
	}

	return 0;
}
