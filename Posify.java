import java.io.*;
import java.util.*;
/**
 * @author Colin Wolfe
 */
public class Posify {


    public static void main(String[] args) throws IOException {

        HashMap<String, HashMap<String, Double>> wordCount = new HashMap<>(); // stores words and their values associated with a Tag
        HashMap<String, HashMap<String, Double>> transitionCount = new HashMap<>(); // stores transition ratios between tags
        train(transitionCount, wordCount, "PS5/brown-train-sentences.txt", "PS5/brown-train-tags.txt"); //trains a HMM on the brown train and test
        consoleTesting(transitionCount, wordCount); // Allows for user to enter in a sentence and have it POS tagged via Viterbi
        //System.out.println();
        //fileTesting(transitionCount, wordCount, "PS5/simple-test-sentences.txt", "PS5/simple-test-tags.txt"); // POS tags sentences in the two via Viterbi
        //System.out.println();
        //stringTesting(transitionCount, wordCount, "I hate red apples and I cannot lie ."); // Tests this fixed sentence by tagging via Viterbi
        //stringTesting(transitionCount, wordCount, "I love my roommate ."); // Tests this fixed sentence by tagging via Viterbi

    }


    /**
     * @author Colin Wolfe
     * @param transitionCount //transition ratios between tags
     * @param wordCount //words and their values associated with a Tag
     * @param fileName1 //file of sentences
     * @param fileName2 //file of correct tags
     * @throws IOException
     * This method creates the correct transitionCount and wordCount maps required for the HMM
     */
    public static void train(HashMap<String, HashMap<String, Double>> transitionCount, HashMap<String, HashMap<String, Double>>wordCount, String fileName1, String fileName2) throws IOException {
        BufferedReader in1 = new BufferedReader((new FileReader(fileName1))); //read in from file 1
        BufferedReader in2 = new BufferedReader((new FileReader(fileName2))); //read in from file 2
        String line = in1.readLine();
        String line2 = in2.readLine();
        while(line!=null){ //while there are lines to read in
            line = line.toLowerCase(); //make all the words lowercase
            String[] lineSplit = line.split(" "); //splits into individual words
            String[] line2Split = line2.split(" "); //splits into individual tags
            if(transitionCount.containsKey("start")){  //if we've read in some tags already
                if(transitionCount.get("start").containsKey(line2Split[0])){ //if this transition has already happened
                    double temp = transitionCount.get("start").get(line2Split[0]) + 1; //add one to the transition count
                    transitionCount.get("start").put(line2Split[0], temp);
                }
                else{
                    transitionCount.get("start").put(line2Split[0], 1.0); //make a new transition hashmap
                }
            }
            else{
                HashMap<String, Double> temp = new HashMap<>();
                temp.put(line2Split[0], 1.0);
                transitionCount.put("start", temp); //make a new start map
            }
            for(int i = 1; i<line2Split.length; i++){ //for each word in the line
                if(wordCount.containsKey(line2Split[i])){ //does the exact same thing as the snippet above just instead of start to tag its tag to tag
                    if(wordCount.get(line2Split[i]).containsKey(lineSplit[i])){
                        double temp = wordCount.get(line2Split[i]).get(lineSplit[i]) + 1;
                        wordCount.get(line2Split[i]).put(lineSplit[i], temp);
                    }
                    else{
                        wordCount.get(line2Split[i]).put(lineSplit[i], 1.0);
                    }
                }
                else{
                    HashMap<String, Double> temp = new HashMap<>();
                    temp.put(lineSplit[i], 1.0);
                    wordCount.put(line2Split[i], temp);
                }
                if(i!=lineSplit.length-1){ //prevents index out of bounds error
                    if(transitionCount.containsKey(line2Split[i])){ //does same thing as earlier
                        if(transitionCount.get(line2Split[i]).containsKey(line2Split[i+1])){
                            double temp = transitionCount.get(line2Split[i]).get(line2Split[i+1]) + 1; //add to the transition count if already exists
                            transitionCount.get(line2Split[i]).put(line2Split[i+1], temp);
                        }
                        else{
                            transitionCount.get(line2Split[i]).put(line2Split[i+1], 1.0);
                        }
                    }
                    else{
                        HashMap<String, Double> temp = new HashMap<>();
                        temp.put(line2Split[i+1], 1.0);
                        transitionCount.put(line2Split[i], temp);
                    }
                }
            }
            line = in1.readLine(); //read next line
            line2 = in2.readLine(); //read next line
        }
        //close readers
        in1.close();
        in2.close();

        Set<String> wordSet = wordCount.keySet();
        for(String word: wordSet){
            Set<String> subSet = wordCount.get(word).keySet();
            double total = 0.0; //total number of occurences for this word
            for(String word2: subSet){
                total += wordCount.get(word).get(word2);
            }
            for(String word2: subSet){
                Double ratio = wordCount.get(word).get(word2);
                ratio = ratio/ total; //ratio of this word to the total word
                ratio = Math.log10(ratio); //log of this ratio
                wordCount.get(word).put(word2, ratio); //make this the new value
            }
        }
        //does same thing as above except for transitions
        for(String word: transitionCount.keySet()){
            double total2 = 0.0;
            for(String word2: transitionCount.get(word).keySet()){
                total2 += transitionCount.get(word).get(word2);
            }
            for(String word2: transitionCount.get(word).keySet()){
                Double ratio = transitionCount.get(word).get(word2);
                ratio = ratio/ total2;
                ratio = Math.log10(ratio);
                transitionCount.get(word).put(word2, ratio);
            }
        }
    }

    /**
     * @param transitionCount
     * @param wordCount
     * @param observations
     * @return a List of the predicted values from the HMM
     */
    public static List<String> Viterbi(HashMap<String, HashMap<String, Double>> transitionCount, HashMap<String, HashMap<String, Double>> wordCount, String[] observations){
        try{ //tries instead of just doing because if the tags don't line up then it won't work.
            ArrayList<String> currStates = new ArrayList<>(); //current states
            currStates.add("start"); //add start to it
            HashMap<String, Double> currScores = new HashMap<>(); //current scores
            currScores.put("start", 0.0); //put in start as 0 (as it just starts)
            HashMap<Integer, HashMap<String, String>> backtrace = new HashMap<>(); //this is the backtrace. integer = observation number, hashmap maps string to string that preceeds it
            for(int i = 0; i<observations.length; i++){ //loop through all words
                backtrace.put(i, new HashMap<String, String>()); //create the backtrace for this observation period
                Set<String> nextStates = new HashSet<>();//empty next states
                HashMap<String, Double> nextScores = new HashMap<>(); //next scores
                for(String currState: currStates){ //go through all the current states
                    Set<String> transitionStates = transitionCount.get(currState).keySet();
                    double currScore = currScores.get(currState); //go through all the possible transitions
                    for(String nextState: transitionStates){
                        nextStates.add(nextState); //add the transition to the nextstates list
                        double transC = transitionCount.get(currState).get(nextState); //this is the transition count value
                        if(currState.equals(".") && nextState.equals(".")){
                            transC = -100; //puncuation to puncuation shouldn't happen
                        }
                        double nextScore;
                        if(wordCount.get(nextState).containsKey(observations[i])){ //word found
                            nextScore = transC + currScore + wordCount.get(nextState).get(observations[i]); //calculate next stop value
                        }
                        else{
                            nextScore = transC + currScore - 100; //next score value
                        }

                        if(!nextScores.containsKey(nextState) || nextScore>nextScores.get(nextState)){ //if this is more likely than the other option
                            nextScores.put(nextState, nextScore); //replace
                            backtrace.get(i).put(nextState, currState); //replace
                        }

                    }

                }

                currStates = new ArrayList<>(nextStates); //next
                // states now are the current states (set to prevent reruns)
                currScores = nextScores; //next scores are now current scores

            }
            String biggest = "start";
            Double highest = -2000000000.0; //impossible to be smaller than
            for(String state: currStates){ //find the highest final value
                if(currScores.get(state)>highest){
                    biggest = state;
                    highest = currScores.get(state);
                }
            }
            ArrayList<String> path = new ArrayList<>();
            path.add(biggest);
            for(int i = observations.length-1; i>0; i--){ //go through all observations
                path.add(0, backtrace.get(i).get(biggest)); //add the backtraced
                biggest = backtrace.get(i).get(biggest);
            }
            return path; //return the path
        }
        catch (Exception e){
            System.out.println("The sentences you trained on do not allow for the tags you're attempting to use");
            return null;
        }
    }


    /**
     * @param transitionCount
     * @param wordCount
     * @param fileName1
     * @param fileName2
     * @throws IOException
     * allows for viterbi to be run on files
     */
    public static void fileTesting(HashMap<String, HashMap<String, Double>> transitionCount, HashMap<String, HashMap<String, Double>> wordCount, String fileName1, String fileName2) throws IOException {
        System.out.println("In file: " + fileName1 + " ..."); //prints file name
        //read in the files
        BufferedReader file1 = new BufferedReader((new FileReader(fileName1)));
        BufferedReader file2 = new BufferedReader((new FileReader(fileName2)));
        String sentence = file1.readLine();
        String sentence2 = file2.readLine();
        int correct = 0, incorrect = 0;
        while(sentence!=null){ //while there are more sentences
            String[] sentenceBuilder = sentence.split(" ");
            String[] tagBuilder = sentence2.split(" ");
            List<String> path = Viterbi(transitionCount, wordCount, sentenceBuilder); //run Viterbi on the sentence
            if(path!=null){
                for(int i = 0; i<tagBuilder.length; i++){
                    if(tagBuilder[i].equals(path.get(i))){
                        correct++; //correct tag
                    }
                    else{
                        incorrect++; //incorrect tag
                    }
                }
            }
            sentence2 = file2.readLine();
            sentence = file1.readLine();
        }
        file1.close();
        file2.close();
        System.out.println("HMM predicted: " + correct + " correct tags and " + incorrect + " incorrect tags."); //print out total
    }


    /**
     * @param transitionCount
     * @param wordCount
     */
    public static void consoleTesting(HashMap<String, HashMap<String, Double>> transitionCount, HashMap<String, HashMap<String, Double>> wordCount) {
        //read in sentence through the console
        Scanner in = new Scanner(System.in);
        while(true){
            System.out.println("Enter Your Sentence:");
            String input = in.nextLine();
            if(input.equals("end")){
                break;
            }
            String[] observations = input.split(" "); //observations for Viterbi
            String[] observations2 = input.split(" "); //entered data
            for (int i = 0; i < observations.length; i++) {
                observations[i] = observations[i].toLowerCase();
            }
            List<String> path = Viterbi(transitionCount, wordCount, observations); //Viterbi to get path
            if (path != null) {
                for (int i = 0; i < path.size(); i++) {
                    System.out.print(observations2[i] + "/" + path.get(i) + " "); //print entered / tags
                }
                System.out.println();
            }
        }
        in.close();
    }

    /**
     * @param transitionCount
     * @param wordCount
     * @param line
     * Allows for Viterbi on a fixed string
     */
    public static void stringTesting(HashMap<String, HashMap<String, Double>> transitionCount, HashMap<String, HashMap<String, Double>> wordCount, String line){
        String[]observations = line.split(" ");
        String[]observations2 = line.split(" ");
        for(int i = 0; i<observations.length; i++){
            observations[i] = observations[i].toLowerCase();
        }
        List<String> path = Viterbi(transitionCount, wordCount, observations); //calls viterbi on fixed string
        if(path!=null){
            for(int i = 0; i<path.size(); i++){
                System.out.print(observations2[i]+"/"+path.get(i)+" ");//print entered / tags
            }
            System.out.println();
        }
    }

}
