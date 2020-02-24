class TestBinarySearch {
	boolean main(int var$0) {
		int[] var$1;
		var$1 = new int[10];
		var$1[0] = -2;
		var$1[1] = 0;
		var$1[2] = 1;
		var$1[3] = 4;
		var$1[4] = 5;
		var$1[5] = 8;
		var$1[6] = 10;
		var$1[7] = 11;
		var$1[8] = 20;
		var$1[9] = 23;
		return binarySearch(var$1, var$0);

	}

	boolean binarySearch(int[] array, int e) {
		int l;
		l = 0;
		int r;
		r = array.length - 1;
		while(l <= r) {
			int m;
			m = l + r - l / 2;
			if(array[m] == e) {
				return true;

			}
			if(array[m] < e) {
				l = m + 1;
			}
			else {
				r = m - 1;
			}
		}
		return false;

	}

}


