class TestSum {
	double main() {
		double[] var$0;
		var$0 = new double[5];
		var$0[0] = 2.3;
		var$0[1] = 3.1;
		var$0[3] = 0.1;
		var$0[4] = 10.0;
		double var$1;
		var$1 = summation(var$0);
		return var$1;

	}

	double summation(double[] array) {
		double sum;
		sum = 0.0;
		int i;
		i = 0;
		while(i != array.length) {
			sum = sum + array[i];
			i = i + 1;
		}
		return sum;

	}

}


