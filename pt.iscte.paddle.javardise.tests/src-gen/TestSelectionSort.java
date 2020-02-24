class TestSelectionSort {
	void main() {
		int[] var$0;
		var$0 = new int[10];
		var$0[0] = -2;
		var$0[1] = 10;
		var$0[2] = 1;
		var$0[3] = 0;
		var$0[4] = 5;
		var$0[5] = 8;
		var$0[6] = 120;
		var$0[7] = 211;
		var$0[8] = 20;
		var$0[9] = 13;
		sort(var$0);
	}

	void sort(int[] array) {
		int i;
		i = 0;
		while(i < array.length - 1) {
			int min;
			min = i;
			int j;
			j = i + 1;
			while(j < array.length) {
				if(array[j] < array[min]) {
					min = j;
				}
				j = j + 1;
			}
			swap(array, i, min);
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


