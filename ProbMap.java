import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;


public class ProbMap extends TreeMap<String, Float> {

	private int denominator;
	public TreeMap<String, Float> map;

	public ProbMap() {
		this.map = new TreeMap<String, Float>();
		this.denominator = 0;
		// TODO Auto-generated constructor stub
	}

	public TreeMap<String, Float> getMap() {
		// TODO Auto-generated method stub
		return map;
	}
	
	public void incrementDenominator() {
		this.denominator = this.denominator + 1;
	}
	
	public int getDenominator() {
		return denominator;
	}
}