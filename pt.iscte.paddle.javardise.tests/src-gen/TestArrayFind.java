class TestArrayFind {
	boolean main(int var$0) {
		int[] var$1;
		var$1 = new int[10];
		var$1[0] = 3;
		var$1[1] = 1;
		var$1[3] = 4;
		var$1[4] = -2;
		var$1[5] = 30;
		var$1[6] = 10;
		var$1[7] = 4;
		var$1[8] = -2;
		return exists(var$1, var$0);

	}

	boolean exists(int[] array, int e) {
		boolean found;
		found = false;
		int i;
		i = 0;
		while(!found && i < array.length) {
			if(array[i] == e) {
				found = true;
			}
			i = i + 1;
		}
		return found;

	}

}


