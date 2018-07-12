import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.poi.hwpf.HWPFDocument; 
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
/**
 * This is the implementation class of the exam papers comparison program
 * in addition to the GUI
 * @author Yahya Almardeny
 * @version 2.0
 */

public class ExamPapersComparison extends Application{
	private File folder;
	private static int flex, num=1;
	private Paper finalResult = new Paper();
	private Stage stage;
	// do not consider preposition and "the" article as words to compare, to avoid false positive results
	final private static List<String> prepositions = new ArrayList<String>(Arrays.asList("as", "at", "from", "of", "to", "in", "for", "on", "by", "but", "and", "or", "the"));
	@FXML
	Button start, browse;
	@FXML
	Spinner<Integer> flexibility;
	
	public static void main(String[] args) {
		launch(args);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void start(Stage ps) {
		try{
			StackPane root = FXMLLoader.load(ExamPapersComparison.class.getResource("papersComparison.fxml"));
			root.getChildren().add(0, new ImageView(new Image(ExamPapersComparison.this.getClass().getResourceAsStream("/bg.jpg"))));
			start = (Button) root.lookup("#start");
			browse = (Button) root.lookup("#browse");
			flexibility = (Spinner<Integer>) root.lookup("#flexibility");
			Scene s = new Scene(root,450,325);
			ps.getIcons().add(new Image(ExamPapersComparison.class.getResourceAsStream("/icon.png")));
			ps.setResizable(false);
			ps.setTitle("Main Menu");
			ps.setScene(s);
			ps.setOnCloseRequest(e->{if(stage!=null && stage.isShowing()){stage.close();}});
		    SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 50);
		    flexibility.setValueFactory(valueFactory);
			
			Alert notification = new Alert(AlertType.INFORMATION);
	      	notification.setTitle("Information");
	      	notification.setHeaderText("For Best Result - Before You Start");
	        notification.setContentText("Please make sure the files only contain questions and remove any irrelevant pages (e.g. first intro page..etc).");
	            
			ps.show();
			
      	    notification.showAndWait();
            
		} catch (Exception e) {e.printStackTrace();}
		
		
		// browse button functionality
		browse.setOnAction(e->{
			DirectoryChooser dc = new DirectoryChooser();
			folder = dc.showDialog(null);
		});
		

	    // start working when user hits start button
	    start.setOnAction(e->{
	    	if(folder != null){
	    		
	    		showProgressBar();
	    		
	    	    Task<Void> comparison = new Task<Void>(){
	    			@Override
	    			protected Void call() throws Exception {startComparison(); return null;}};
	    			
	    		new Thread(comparison).start();
	    		
	    		comparison.setOnSucceeded(event->{
	    			Platform.runLater(new Runnable(){
						@Override
						public void run() {
							// Close progress bar and resume buttons functionality
							if(stage.isShowing()){stage.close();}
							browse.setDisable(false);
							start.setDisable(false);
							
			    			/// SAVE WORK ///
			    	      	FileChooser fileChooser = new FileChooser();
			    			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
			    	        fileChooser.getExtensionFilters().add(extFilter);
			    	        File resultFile = fileChooser.showSaveDialog(ps);
			    	        
			    	        if(resultFile != null){
			    	        	try {
				    	        	PrintWriter out = new PrintWriter(resultFile);
				    	        	for(Question repeatedQ : finalResult.getQuestions()){
				    	        		out.println((num++) + ". " + repeatedQ.getQuestion());	
				    	        	}
				    	        	out.close();
			    	        	} catch (IOException e1) {e1.printStackTrace();}
			    	      	    
			    	      	    
			    	      	    /////Notify Success//////    	    
			    	      	    Alert notification = new Alert(AlertType.INFORMATION);
			    	      	    notification.setTitle("Information");
			    	      	    notification.setHeaderText("Your Result has been Saved Successfully");
			    	            notification.setContentText("Done with " + finalResult.getQuestions().size() + " Repeated Line!");
			    	            notification.setOnCloseRequest(e->{ps.close();});
			    	            notification.showAndWait();
			    	          }
			    	        else{
			    	        	folder = null; num=1; finalResult = new Paper();
			    	        }
			    	     }
					}); //END OF PLATFORM RUNNABLE
	    		}); // END OF SET ON SUCCEEDED
	    	} // END OF CHECKING FOLDER NOT NULL	
	    }); // END OF START SET ON ACTION

	    
	    	
	}

	
	/**
	 * compare two papers line by line
	 * @param p1
	 * @param p2
	 * @return paper
	 */
	private Paper compareTwoPapers(Paper p1, Paper p2){
		
		Paper paper = new Paper(); // to hold all repeated questions
		
		for(Question q1 : p1.getQuestions()){
			for(Question q2 : p2.getQuestions()){
				Question repeatedQ = compareTwoQuestions(q1,q2);
				if(repeatedQ!=null){ // which means there's a match according to the flexibility set by user
					paper.addQuestion(repeatedQ);
					break; // go to next question
				}
			}
		}
		return paper;
	}
	
	
	/**
	 * compare two question, word by word
	 * @param q1
	 * @param q2
	 * @return question
	 */
	private Question compareTwoQuestions(Question q1, Question q2){
		// attempt to make sure it's a real question
		// this doesn't guarantee that the given line is not a question
		// differers according to the exam schema
		// one suggestion is to leave it to the user to set it via a UI
		// or to let users provide a schema then we can avoid these assumptions
		String firstQLower = q1.getQuestion().toLowerCase();
		String secondQLower = q2.getQuestion().toLowerCase();
		String[] firstQwords = firstQLower.split(" ");
		String[] secondQwords = secondQLower.split(" ");
		int firstQL = firstQwords.length;
		int secondQL = secondQwords.length;
		
		if( (firstQL<=4 || secondQL<=4) || ((firstQL<=6 || secondQL<=6)  && 
										   (firstQLower.contains("mark") || firstQLower.contains("marks") ||
										    firstQLower.contains("question") || firstQLower.contains("questions") ||
										    secondQLower.contains("mark") || secondQLower.contains("marks") ||
										    secondQLower.contains("question") || secondQLower.contains("questions"))) 
		   ){
			
			return null;
		}
		
		// check if they are similar and return the bigger				
		if (q1.equals(q2)){ 
			return (firstQL>=secondQL) ? q1 : q2;
		}
		
		// if reached this point, work on the flexibility thing
		// to compare between every two given questions
		int count = 0;
	
		for(String word1 : firstQwords){
			for(String word2 : secondQwords){
				if(word1.equalsIgnoreCase(word2) && !prepositions.contains(word1)){
					count++;
					break;
				}
			}
		}

		// calculate the required number of words according to the bigger question
		float requiredSimilarity = (firstQL>=secondQL) ? firstQL * ((float)(100-flex)/100) :  secondQL * ((float)(100-flex)/100);
		
		// if it's within the required flexibility, then returned a concatenation of both questions
		// let the bigger starts first
		return  (count>=Math.round(requiredSimilarity)) ? 
				((firstQL>=secondQL) ? new Question(q1.getQuestion() + " **[** " + q2.getQuestion() + " **]**") : 
					new Question(q2.getQuestion() + " **[** " + q1.getQuestion()+ " **]**")) : null;
	}
	
	
	/**
	 * To get the file extension from a given file name
	 * @param file
	 * @return fileExtension
	 */
	private String getFileExtension(File file) {
		int dotIndex = file.getName().lastIndexOf('.');
	    return (dotIndex == -1) ? "" : file.getName().substring(dotIndex + 1);
	}
	
	
	/**
	 * for the annoying unseen special chars in MS-Word
	 * @param paragraph
	 * @return true or false
	 */
	private boolean ok(String paragraph){
       	for(char c : paragraph.replace(" ", "").toCharArray()){
       		if(Character.isLetterOrDigit(c)){
       				return true;
       		}
       	}
       	return false;
	 }
	
	
	private void startComparison(){
		flex = flexibility.getValue();
    	
		// create a list of papers 
		List<Paper> papers = new ArrayList<>(folder.list().length);
	
		Scanner read = null;
		WordExtractor extractor = null;
		//////////////Read All Files/////////////////
	
      	File[] files = folder.listFiles();
      	for (File file : files) {
      		if (!file.isDirectory()) {
      				 String type = getFileExtension(file);
      				 Paper paper = null;
      				 if(type.equalsIgnoreCase("docx")){
      					try {  
      						FileInputStream fis=new FileInputStream(file.getAbsolutePath());
      						XWPFDocument document = new XWPFDocument(fis);
      						List<XWPFParagraph> paragraphs = document.getParagraphs();
      						paper = new Paper();
      		                for (XWPFParagraph para : paragraphs) {
      		                	String line = para.getText();
      		                	if(ok(line)){
      		                		paper.addQuestion(new Question(line));
      		                	}
      		                }
      		                document.close();
      					} 
      					catch(Exception ex){ ex.printStackTrace();} 		
      				 }
      				 if(type.equalsIgnoreCase("doc")){ // then it's MS-Word File
      					 
      					try {  
      						FileInputStream fis=new FileInputStream(file.getAbsolutePath());
      						HWPFDocument document = new HWPFDocument(fis);
      						extractor = new WordExtractor(document); 
      						String [] content = extractor.getParagraphText();
      						if(content!=null){
      							paper = new Paper();
      							for(String line : content){
      								if(ok(line)){
      									paper.addQuestion(new Question(line));
      								}
	      						}
      						}	
      					} 
      					catch(Exception ex){ ex.printStackTrace();} 		
      				 }
      				 
      				 else if (type.equalsIgnoreCase("txt")){ // then it's a normal text file
      					 try {
							read = new Scanner(file);
      					 } catch (FileNotFoundException e1) {e1.printStackTrace();}
      					 paper = new Paper();
      					 while(read.hasNext()){
      						 String line = read.nextLine();
      						 if(ok(line)){
      							 paper.addQuestion(new Question(line));
      						 }
      					 }
      				 }
      				 
      				 if(paper!=null){papers.add(paper);}
      				 
      		} // END OF IF FILE IS NOT DIRECTORY 
      	} // END OF LOOP
      	
      	///////START COMPARISON/////////
    	for(int i=0; i<papers.size(); i++){
    		for(int j=i+1; j<papers.size(); j++){
    			Paper result = compareTwoPapers(papers.get(i), papers.get(j)); // return the repeated questions between two given papers
    			for(Question repeatedQuestion : result.getQuestions()){
    				finalResult.addQuestion(repeatedQuestion);
    			}		
    		}
    	}
    }


	/**
	 * run and show a progress bar
	 * indicating the program is processing
	 */
	private void showProgressBar(){
		Platform.runLater(new Runnable(){
			@Override
			public void run() {
				stage = new Stage();
				stage.initStyle(StageStyle.UNDECORATED);
				VBox root = new VBox();
				root.setBackground(new Background(new BackgroundFill(Color.GREY, null, null)));
				root.setAlignment(Pos.CENTER);
				Label text = new Label("Processing...");
				text.setTextFill(Color.WHITE);
			    ProgressBar pb = new ProgressBar();
			    root.getChildren().addAll(text, pb);
			    Scene scene = new Scene(root, 200, 75);
			    stage.setScene(scene);
			    stage.setResizable(false);
			    start.setDisable(true);
			    browse.setDisable(true);
			    stage.show();
			}
			
		});
			
	}
	
}
