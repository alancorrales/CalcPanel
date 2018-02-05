/*
* Autor: Alan Carvalho Corrales
* Título: Software para Cálculo de Orçamentos da VidroArt
*
* Observação 1: para serviços sem milimetragem, o programa só gera saída se o
* campo "Milimetragem" for deixado em branco.
*
* Observação 2: para serviços com milimetragem, o programa só gera saída se a
* milimetragem estiver de acordo com a tabela.
*
* Observação 3: para itens que não dependem da tabela/altura (jateamento/bisoteamento),
* ainda assim é necessário escolher um número da tabela antes de clicar em "Pronto!"
*
*/

//TODO POST-PRESENTATION: Verificar qual é melhor: saída em caixa de diálogo ou arquivo.

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;

public class CalcPanel {
  JTextField horzField;
  JTextField vertField;
  JTextField miliField;
  JList typeGlassList;
  JList tableNumList;
  JButton readyButton;
  float horzSize;
  float vertSize;
  double totSize;
  String tableNumSelected;
  String typeGlassSelected;
  double price;
  int mili;
  String[] typeGlassOptions = {"Comum INC", "Comum VER/FUM",
                            "Temperado INC", "Temperado VER/FUM",
                            "Espelho", "Box INC", "Box FUM", "Box VER",
                            "Jat. Area-linear", "Bisot. Largura-linear", "Vidro CAN 4mm",
                            "Vidro ARAM 4mm"};
  String[] tableNumOptions = {"1", "2", "3"};
  // --------------------------- VIDRO COMUM
  // Valor do m2 do vidro comum incolor
  static double[] comumInc4MM = {200.00,180.00,160.00};
  static double[] comumInc6MM = {240.00,220.00,200.00};
  static double[] comumInc8MM = {300.00,270.00,250.00};
  static double[] comumInc10MM = {350.00,320.00,300.00};
  // Valor do m2 do vidro comum verde/fume
  static double[] comumVerFum4MM = {250.00,220.00,200.00};
  static double[] comumVerFum6MM = {300.00,270.00,250.00};
  static double[] comumVerFum8MM = {360.00,330.00,300.00};
  // --------------------------- VIDRO TEMPERADO
  // Valor do m2 do vidro temperado
  static double[] tempInc6MM = {300.00,270.00,250.00};
  static double[] tempInc8MM = {400.00,370.00,350.00};
  static double[] tempInc10MM = {500.00,470.00,460.00};
  // Valor do m2 do vidro temperado
  static double[] tempVerFum6MM = {390.00,360.00,330.00};
  static double[] tempVerFum8MM = {490.00,460.00,430.00};
  static double[] tempVerFum10MM = {590.00,560.00,530.00};
  // Valor do m2 do box
  static double[] boxInc = {290.00,270.00,250.00};
  static double[] boxFum = {390.00,370.00,350.00};
  static double[] boxVer = {430.00,400.00,370.00};
  // Valor do m2 do espelho
  static double[] esp3MM = {250.00,220.00,200.00};
  static double[] esp4MM = {300.00,270.00,250.00};
  static double[] esp6MM = {430.00,400.00,370.00};
  // Valor do jateamento por m2
  static double jat = 50.00;
  // Valor do bisoteamento por m
  static double bist = 30.00;
  // Valor da furacao de vidro por furo
  static double furc = 10.00;
  // Valor do vidro canelado por m2 com 4mm
  static double[] vidroCan = {200.00,180.00,160.00};
  // Valor do vidro aramado por m2 com 4mm
  static double[] vidroAram = {500.00,480.00,460.00};

  public static void main(String[] args) {
    CalcPanel gui = new CalcPanel();
    gui.go();
  }

  public void go() {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // ---------------------------------- Parte superior
    JPanel upperPanel = new JPanel();

    JLabel glassHorzSize = new JLabel("Largura: ");
    horzField = new JTextField(10);
    JLabel glassVertSize = new JLabel("cm      Altura: ");
    vertField = new JTextField(10);
    JLabel cm = new JLabel("cm");

    horzField.addActionListener(new HorzFieldListener());
    vertField.addActionListener(new VertFieldListener());

    upperPanel.add(glassHorzSize);
    upperPanel.add(horzField);
    upperPanel.add(glassVertSize);
    upperPanel.add(vertField);
    upperPanel.add(cm);

    frame.getContentPane().add(BorderLayout.CENTER, upperPanel);

    // ------------------------------ Parte Inferior
    JPanel lowerPanel = new JPanel();

    JLabel typeGlass = new JLabel("  Tipo do material: ");
    typeGlassList = new JList(typeGlassOptions);
    JLabel tableNum = new JLabel("  Tabela: ");
    tableNumList = new JList(tableNumOptions);
    JLabel miliLabel = new JLabel("  Milimetragem: ");
    miliField = new JTextField(5);

    typeGlassList.setVisibleRowCount(1);
    tableNumList.setVisibleRowCount(1);

    typeGlassList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    tableNumList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

    JScrollPane typeGlassScroller = new JScrollPane(typeGlassList);
    typeGlassScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    typeGlassScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    JScrollPane tableNumScroller = new JScrollPane(tableNumList);
    tableNumScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    tableNumScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    typeGlassList.addListSelectionListener(new TypeGlassListener());
    tableNumList.addListSelectionListener(new TableNumListener());

    readyButton = new JButton("Calcular");
    readyButton.addActionListener(new ReadyButtonListener());

    upperPanel.add(miliLabel);
    upperPanel.add(miliField);
    upperPanel.add(typeGlass);
    upperPanel.add(typeGlassScroller);
    upperPanel.add(tableNum);
    upperPanel.add(tableNumScroller);


    lowerPanel.add(readyButton);

    frame.getContentPane().add(BorderLayout.SOUTH, lowerPanel);

    frame.setSize(640,150);
    frame.setVisible(true);
  }

  private boolean calculatePrice() {
    int index = -1;
    for (int i = 0; i < 12; i++) {
      if(typeGlassOptions[i].equals(typeGlassSelected)) {
        index = i;
        break;
      }
    }

    if(index != -1) {
      // Pega indice para o vetor do material selecionado
      int tableIndex = 0;
      try {
        tableIndex = Integer.parseInt(tableNumSelected) - 1;
      } catch(Exception ex) {}
      // Milimetragem requisitada
      if(!miliField.getText().equals("") && (index >= 0 && index <= 4)) {
        // Salva milimetragem
        try {
          mili = Integer.parseInt(miliField.getText());
        } catch(Exception ex) {}
        // Calcula pra vidro comum incolor
        if(typeGlassSelected.equals("Comum INC")) {
          if(mili == 4) {
            price = totSize * comumInc4MM[tableIndex];
          } else if(mili == 6) {
            price = totSize * comumInc6MM[tableIndex];
          } else if(mili == 8) {
            price = totSize * comumInc8MM[tableIndex];
          } else if(mili == 10) {
            price = totSize * comumInc10MM[tableIndex];
          } else return false;
        } else if(typeGlassSelected.equals("Comum VER/FUM")) {
          if(mili == 4) {
            price = totSize * comumVerFum4MM[tableIndex];
          } else if(mili == 6) {
            price = totSize * comumVerFum6MM[tableIndex];
          } else if(mili == 8) {
            price = totSize * comumVerFum8MM[tableIndex];
          } else return false;
        } else if(typeGlassSelected.equals("Temperado INC")) {
          if(mili == 6) {
            price = totSize * tempInc6MM[tableIndex];
          } else if(mili == 8) {
            price = totSize * tempInc8MM[tableIndex];
          } else if(mili == 10) {
            price = totSize * tempInc10MM[tableIndex];
          } else return false;
        } else if(typeGlassSelected.equals("Temperado VER/FUM")) {
          if(mili == 6) {
            price = totSize * tempVerFum6MM[tableIndex];
          } else if(mili == 8) {
            price = totSize * tempVerFum8MM[tableIndex];
          } else if(mili == 10) {
            price = totSize * tempVerFum10MM[tableIndex];
          } else return false;
        } else if(typeGlassSelected.equals("Espelho")) {
          if(mili == 3) {
            price = totSize * esp3MM[tableIndex];
          } else if(mili == 4) {
            price = totSize * esp4MM[tableIndex];
          } else if(mili == 6) {
            price = totSize * esp6MM[tableIndex];
          } else return false;
        } else return false;
        return true;
      // Milimetragem nao requisistada
      } else if(miliField.getText().equals("") && (index >= 5 && index <= 11)) {
        if(typeGlassSelected.equals("Box INC")) {
          price = totSize * boxInc[tableIndex];
        } else if(typeGlassSelected.equals("Box VER")) {
          price = totSize * boxVer[tableIndex];
        } else if(typeGlassSelected.equals("Box FUM")) {
          price = totSize * boxFum[tableIndex];
        } else if(typeGlassSelected.equals("Jat. Area-linear")) {
          price = totSize * jat;
        } else if(typeGlassSelected.equals("Bisot. Largura-linear")) {
          price = horzSize * bist;
        } else if(typeGlassSelected.equals("Vidro CAN 4mm")) {
          price = totSize * vidroCan[tableIndex];
        } else if(typeGlassSelected.equals("Vidro ARAM 4mm")) {
          price = totSize * vidroAram[tableIndex];
        } else return false;
        return true;
      }
    }

    return false;
  }

  class ReadyButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      if(typeGlassList.getSelectedIndex() != -1 && tableNumList.getSelectedIndex() != -1
      && !horzField.getText().equals("") && !vertField.getText().equals("")) {
        try {
          horzSize = Float.parseFloat(horzField.getText());
          vertSize = Float.parseFloat(vertField.getText());
          totSize = (vertSize * horzSize) / 10000.0;
          boolean ret = calculatePrice();
          if(ret) {
            JOptionPane.showMessageDialog(null,"Área: " + totSize + " m2\nPreço: R$" + price,
             "Cálculo", JOptionPane.INFORMATION_MESSAGE);
          }
        } catch(Exception ex) {}
        typeGlassList.clearSelection();
        tableNumList.clearSelection();
        horzField.requestFocus();
      }
    }
  }

  class HorzFieldListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      vertField.requestFocus();
    }
  }

  class VertFieldListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      miliField.requestFocus();
    }
  }

  class TypeGlassListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent event) {
      try {
        if(!event.getValueIsAdjusting() && typeGlassList.getSelectedIndex() != -1) {
          String selection = (String) typeGlassList.getSelectedValue();
          typeGlassSelected = selection;
        }
      } catch(Exception ex) {}
    }
  }

  class TableNumListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent event) {
      try {
        if(!event.getValueIsAdjusting() && tableNumList.getSelectedIndex() != -1) {
          String selection = (String) tableNumList.getSelectedValue();
          tableNumSelected = selection;
        }
      } catch(Exception ex) {}
    }
  }

}
