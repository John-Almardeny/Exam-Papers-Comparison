/**
 * This Class Represents a question
 * that is a complete read line from the given file
 * @author Yahya Almardeny
 * @version 2.0
 */
public class Question {

	private String question;
	
	/**
	 * Constructor accept a string question
	 * and assign it to the question field after 
	 * purifying it from non-needed chars
	 * @param question
	 */
	public Question(String question){
		this.question = purifyQuestion(question);
	}
	
	/**
	 * override equals methods
	 * to customize the equality rule
	 */
	@Override
	public boolean equals(Object q){
		
		if(!(q instanceof Question) || q==null){return false;}
		
		String first = ((Question)q).question.toLowerCase();
		String second = this.question.toLowerCase();
		// if any of these questions is contained in the other
		// consider them equal
		if(first.equals(second) || first.contains(second) || second.contains(first)){
			return true;
		}
		
		// if they are already concatenated questions coming from the comparison algorithm
		// then check if each is combined separately. If so, compare each part in one question 
		// with other parts in second question and so on
		String [] firstSub = first.split("\\*\\*\\[\\*\\*");
		String [] secondSub = second.split("\\*\\*\\[\\*\\*");
		if(firstSub.length==2 && secondSub.length==2){
			String first1 = firstSub[0].trim();
			String first2 = firstSub[1].replace("\\*\\*\\]\\*\\*", "").trim();
			String second1 =secondSub[0].trim();
			String second2 =secondSub[1].replace("\\*\\*\\]\\*\\*", "").trim();
			if(first1.equals(second1) || first1.equals(second2)||
			   first2.equals(second1) || first2.equals(second2)){
					return true;
			}
		}
		
		return false;	
	}
	
	/**
	 * override the hashCode method
	 * to make all objects of this class to have the same hash code
	 * in this case the equals method will be the only reference of equality
	 */
	@Override
	public int hashCode(){
		return 0; 
	}
	
	@Override
	public String toString(){
		return this.question;
	}
	

	
	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}
	
	
	/**
	 * remove the enumeration and all special chars from the question
	 * @param question
	 * @return purifiedQuestion
	 */
	private static String purifyQuestion(String question){
		// different enumerations usually don't exceed 2 chars + the dot or bracket
		// in some cases if the Latin enumeration is used, it may very rarely reaches up to 5 chars  + the dot or bracket
		// usually there is a space as a splitter between the enumeration and first word of the question
		// in some very rare cases it may not be a space
		// special characters may make two questions different although they are same
		
		String purifiedQuestion = null;
		String[] dotSplit = question.split("\\."); //search for a dot
		String enumerator = (dotSplit.length>0)? dotSplit[0] : "";
		if(!enumerator.isEmpty() && enumerator.length()>0 && enumerator.length()<=3){ // check if it's a possible enumerator and not a normal word
			try{purifiedQuestion = question.substring(enumerator.length()+1).trim();}
			catch(Exception e){}
		}
		else{ // if dot not found, repeat the procedure for a bracket
			String[] bracketSplit = question.split("\\)"); 
			String enumerator1 =(bracketSplit.length>0)? bracketSplit[0] : "";
			if(!enumerator1.isEmpty() &&  enumerator1.length()>0 && enumerator1.length()<=3){
				try{purifiedQuestion = question.substring(enumerator1.length()+1).trim();}
				catch(Exception e){}
			}
		}
		
		if (purifiedQuestion!=null){ // if any found
			purifiedQuestion = purifiedQuestion.replaceAll("[!\"&\\(\\),;\\?@\\[\\]_\\{\\}'’]", ""); // get rid of the special chars
			if(purifiedQuestion.replace(" ", "").isEmpty()){ // confirm what left of question is not empty space
				return null;
			}
			if(purifiedQuestion.substring(purifiedQuestion.length()-1).equals(".")){ // get rid of the dot at the end of each qustion
				return purifiedQuestion.substring(0, purifiedQuestion.length()-1);
			}
			return purifiedQuestion;
		}
		
		return question.trim();
	}
	
}
