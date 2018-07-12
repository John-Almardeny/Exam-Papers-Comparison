import java.util.HashSet;
import java.util.Set;

/**
 * Paper class represents an Exam Paper
 * containing a bunch of non-duplicate questions
 * @author Yahya Almardeny
 * @version 2.0
 */
public class Paper {
	
	// Set by nature removes duplicates, that's why we override equals method in Question Class
	private Set<Question> questions;
	
	public Paper(){
		questions = new HashSet<>();
	}
	public Paper(Set<Question> questions){
		this.questions = questions;
	}
	
	public void addQuestion(Question question){
		if(question.getQuestion()!=null){
			this.questions.add(question);
		}
	}
	
	public Set<Question> getQuestions() {
		return questions;
	}
	
	public void setQuestions(Set<Question> questions) {
		this.questions = questions;
	}
	
}
