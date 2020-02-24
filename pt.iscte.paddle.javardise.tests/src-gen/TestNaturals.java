class TestNaturals {
	int[] naturals(int n) {
		int[] array;
		array = new int[n];
		int i;
		i = 0;
		while(i < n) {
			array[i] = i + 1;
			i = i + 1;
		}
		return array;

	}

}


