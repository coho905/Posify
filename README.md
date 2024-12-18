# Posify
*An NLP tool to tag parts of speech in sentences*

## How it Works
+ Posify utilizes a hidden markov model and the Viterbi state algorithm to tag each word/punctuation in a sentence.
+ Achieved ~92% accuracy on test corpus.
+ Has options for command line, files, and manually embedded sentences.

## Usage
+ First run train on your corpus. The default is:
  ```java
  train(transitionCount, wordCount, "Posify/brown-train-sentences.txt", "Posify/brown-train-tags.txt"); //may need to change path depending on file structure
  ```
+ Option 1: Console testing. Runs an infinite loop in the console until you enter 'end' as your sentence.
  ```java
  consoleTesting(transitionCount, wordCount);
  ```
+ Option 2: Reading from files. Reads the files and handles it one line (each line is treated as an independent sentence) at a time. First arg is what to test on. Second is for validating accuracy
  ```
  java fileTesting(transitionCount, wordCount, "Posify/simple-test-sentences.txt", "Posify/simple-test-tags.txt"); //change path to yours.
  ```
+ Option 3: Sentences embedded in the code
  ```java
  stringTesting(transitionCount, wordCount, "I hate red apples and I cannot lie .");
  ```

## Note:
+ Very easy to run the command on files without validating accuracy, you just need to slightly tweak the fileTesting method.
+ Let me know if there are any unexpected errors.
+ Feel free to make pull requests.

## Supported
Tag	Meaning			Examples
---	-------			--------
</br>
ADJ	adjective		new, good, high, special, big, local</br>
ADV	adverb			really, already, still, early, now</br>
CNJ	conjunction		and, or, but, if, while, although</br>
DET	determiner		the, a, some, most, every, no</br>
EX	existential		there, there's</br>
FW	foreign word		dolce, ersatz, esprit, quo, maitre</br>
MOD	modal verb		will, can, would, may, must, should</br>
N	noun			year, home, costs, time, education</br>
NP	proper noun		Alison, Africa, April, Washington</br>
NUM	number			twenty-four, fourth, 1991, 14:24</br>
PRO	pronoun			he, their, her, its, my, I, us</br>
P	preposition		on, of, at, with, by, into, under</br>
TO	the word to		to</br>
UH	interjection		ah, bang, ha, whee, hmpf, oops</br>
V	verb			is, has, get, do, make, see, run</br>
VD	past tense		said, took, told, made, asked</br>
VG	present participle	making, going, playing, working</br>
VN	past participle		given, taken, begun, sung</br>
WH	wh determiner		who, which, when, what, where, how</br>

Education-Restricted License 2024, Colin Wolfe 
