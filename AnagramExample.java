package com.anagram;

public class AnagramExample {

	public static void main(String[] args) {
		String s1 = "listen";
		String s2 = "silent";
		s1.trim();
		s2.trim();
		if (s1.length() == s2.length()) {
			boolean isPresent = true;
			for (int i = 0; i < s1.length() - 1; i++) {
				for (int j = 0; j < s2.length() - 1; j++) {
					if (s1.charAt(i) == s2.charAt(j)) {
						isPresent = true;
						break;
					} else {
						isPresent = false;
					}
				}

			}
			if (isPresent) {
				System.out.println("anagram");
			} else {
				System.out.println(" not anagram");
			}

		} else {
			System.out.println("not anagram");
		}
	}

}
