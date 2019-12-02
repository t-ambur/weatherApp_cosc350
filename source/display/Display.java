package source.display;

/*
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
*/
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.*;

import javax.swing.*;

public class Display {
	
	private JFrame frame;
	
	private String catFact;
	private JLabel catBox;
	
	private JLabel zipcodeLabel;
	private JTextField zipcodeBox;
	private JButton submitZipButton;
	private boolean submitClicked;
	private boolean[] radioB;
	
	private int currentZip;
	private String attemptedZip;
	
	private String[] weatherData;
	private JTextArea weatherArea;
	
	private JRadioButton radioF;
	private JRadioButton radioC;
	private JRadioButton radioK;
	
	private String title;
	private int width, height;
	
	//protected static Dimension mapSize;
	
	public Display(String title, int width, int height, int zip){
		this.title = title;
		this.width = width;
		this.height = height;
		this.currentZip = zip;
		radioB = new boolean[3];
		attemptedZip = Integer.toString(zip);
		submitClicked = false;
		catFact = "This is where the cat fact goes";
		init();
	}
	
	private void init(){
		frame = new JFrame(title);
		frame.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		frame.setSize(width, height);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);
		frame.setVisible(true);
		
		catBox = new JLabel("Your daily cat fact:   " + catFact);
		
		zipcodeLabel = new JLabel("Zip Code:");
		zipcodeLabel.setPreferredSize(new Dimension(55,20));
		
		zipcodeBox = new JTextField(Integer.toString(currentZip));
		zipcodeBox.setPreferredSize(new Dimension(50,20));
		zipcodeBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				changeZip();
			}
		});
		
		submitZipButton = new JButton("Submit");
		submitZipButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				changeZip();
			}
		});
		
		weatherArea = new JTextArea();
		
		radioF = new JRadioButton("Fahrenheit");
		radioF.setSelected(true);
		radioF.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				radioB[0] = true;
			}
		});
		radioC = new JRadioButton("Celsius");
		radioC.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				radioB[1] = true;
			}
		});
		radioK = new JRadioButton("Kelvin");
		radioK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				radioB[2] = true;
			}
		});
		
		ButtonGroup bG = new ButtonGroup();
		bG.add(radioF);
		bG.add(radioC);
		bG.add(radioK);
		
		//layout manager
		c.fill = GridBagConstraints.HORIZONTAL;
		
		c.gridx = 0;
		c.gridy= 0;
		frame.add(catBox, c);
		
		c.gridx = 1;
		c.gridy = 1;
		frame.add(zipcodeLabel, c);
		
		c.gridx = 2;
		c.gridy = 1;
		frame.add(zipcodeBox, c);
		
		c.gridx = 3;
		c.gridy = 1;
		frame.add(submitZipButton, c);
		
		c.gridx = 1;
		c.gridy = 2;
		frame.add(radioF, c);
		c.gridx = 2;
		c.gridy = 2;
		frame.add(radioC, c);
		c.gridx = 3;
		c.gridy = 2;
		frame.add(radioK, c);
		
		c.gridx = 0;
		c.gridy = 3;
		frame.add(weatherArea, c);
		
		frame.pack();
		
		zipcodeBox.requestFocusInWindow();
		zipcodeBox.selectAll();
	}
	
	public void displayWeather()
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < weatherData.length; i++)
		{
			sb.append(weatherData[i] + "\n");
		}
		weatherArea.setText(sb.toString());
	}
	
	public void setWeather(String[] w)
	{
		weatherData = new String[w.length];
		
		for (int i = 0; i < w.length; i++)
			weatherData[i] = w[i];
		displayWeather();
	}
	
	public void pack()
	{
		frame.pack();
	}
	
	public void changeZip()
	{
		attemptedZip = zipcodeBox.getText();
		if (attemptedZip.length() == 5)
		{
			try
			{
				currentZip = Integer.parseInt(attemptedZip);
				setZipText("");
				submitClicked = true;
			}
			catch (NumberFormatException e)
			{
				setZipText("must be numbers");
			}
		}
		else
		{
			setZipText("must be 5 digits");
		}
	}
	
	public JFrame getFrame(){
		return frame;
	}
	
	public void setCatFact(String fact)
	{
		catFact = fact;
		catBox.setText("Your daily cat fact:   " + catFact);
	}
	
	public String getCatFact()
	{
		return catFact;
	}
	
	public void setZipCode(int zip)
	{
		currentZip = zip;
	}
	
	public int getZipCode()
	{
		return currentZip;
	}
	
	public boolean isSubmitClicked()
	{
		return submitClicked;
	}
	
	public int isRadioChanged()
	{
		for (int i = 0; i < radioB.length; i++)
		{
			if (radioB[i] == true)
			{
				radioB[i] = false;
				return i;
			}
		}
		return -1;
	}
	
	public void resetClick()
	{
		submitClicked = false;
	}
	
	public void setZipText(String s)
	{
		zipcodeBox.setText(s);
		zipcodeBox.selectAll();
	}
}
