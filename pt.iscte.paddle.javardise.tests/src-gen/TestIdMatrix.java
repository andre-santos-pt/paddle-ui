class TestIdMatrix {
	int[][] idMatrix(int n) {
		int[][] id;
		id = new int[n][n];
		int i;
		i = 0;
		while(i != n) {
			id[i][i] = 1;
			i = i + 1;
		}
		return id;

	}

}


