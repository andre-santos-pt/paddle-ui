class TestSwap {
	void main() {
		int[] var$0;
		var$0 = new int[10];
		var$0[0] = -2;
		var$0[1] = 0;
		var$0[2] = 1;
		var$0[3] = 4;
		var$0[4] = 5;
		var$0[5] = 8;
		var$0[6] = 10;
		var$0[7] = 11;
		var$0[8] = 20;
		var$0[9] = 23;
		swap(var$0, 3, 4);
	}

	void swap(int[] array, int i, int j) {
		int t;
		t = array[i];
		array[i] = array[j];
		array[j] = t;
	}

}


