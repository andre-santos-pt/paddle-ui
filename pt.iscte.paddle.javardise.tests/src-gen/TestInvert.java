class TestInvert {
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
		int[] var$1;
		var$1 = new int[9];
		var$1[0] = -2;
		var$1[1] = 0;
		var$1[2] = 1;
		var$1[3] = 4;
		var$1[4] = 5;
		var$1[5] = 8;
		var$1[6] = 10;
		var$1[7] = 11;
		var$1[8] = 20;
		invert(var$0);
		invert(var$1);
	}

	void invert(int[] array) {
		int i;
		i = 0;
		while(i < array.length / 2) {
			swap(array, i, array.length - 1 - i);
			i = i + 1;
		}
	}

	void swap(int[] var$0, int var$1, int var$2) {
		int var$3;
		var$3 = var$0[var$1];
		var$0[var$1] = var$0[var$2];
		var$0[var$2] = var$3;
	}

}


