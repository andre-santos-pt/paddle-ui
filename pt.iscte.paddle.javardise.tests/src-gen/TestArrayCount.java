class TestArrayCount {
	int main() {
		int[] var$0;
		var$0 = new int[10];
		var$0[0] = -2;
		var$0[1] = 3;
		var$0[2] = 1;
		var$0[3] = 4;
		var$0[4] = 5;
		var$0[5] = 3;
		var$0[6] = 10;
		var$0[7] = 11;
		var$0[8] = 20;
		var$0[9] = 3;
		return count(var$0, 3);

	}

	int count(int[] array, int e) {
		int c;
		c = 0;
		int i;
		i = 0;
		while(i < array.length) {
			if(array[i] == e) {
				c = c + 1;
			}
			i = i + 1;
		}
		return c;

	}

}


