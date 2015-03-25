package hu.berzsenyi.exchange.server.game;

import java.util.Arrays;
import java.util.Random;

public class ArrayHelper {
	public static int[] createShuffledIntArray(int N, Random rand) {
		int[] ret = new int[N];
		Arrays.fill(ret, -1);
		for(int i = 0; i < N; i++) {
			int index = rand.nextInt(N-i);
			boolean flag = true;
			int skip = 0;
			for(int j = 0; flag; j++)
				if(ret[j] != -1)
					skip++;
				else if(index <= j-skip) {
					ret[j] = i;
					flag = false;
				}
		}
		return ret;
	}
}
