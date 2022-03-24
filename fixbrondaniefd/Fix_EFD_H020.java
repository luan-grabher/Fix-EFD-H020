package fixbrondaniefd;

import fileManager.FileManager;
import fileManager.Selector;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class Fix_EFD_H020 {

    public static void main(String[] args) {
        //Pega arquivo
        File file = Selector.selectFile("C:/Windows/Users/User/Downloads", "Texto(.txt)", "txt");
        String fileText = FileManager.getText(file);
        String[] fileTextLines = fileText.split("\r\n");

        //Novo texto
        StringBuilder newText = new StringBuilder();
        //Aliquotas
        Map<String, BigDecimal> aliqs = new HashMap<>();

        //Percorre linhas para criar arquivo
        
            
        for (int i = 0; i < fileTextLines.length; i++) {
            String fileTextLine = fileTextLines[i];
            
            if (fileTextLine.startsWith("|0200|")) {
                String[] colunas = fileTextLine.split("\\|", -1);
                //Se não tiver aliquota
                if ("".equals(colunas[12])) {
                    colunas[12] = "18";
                }
                
                //Salva aliquota pra mais tarde
                aliqs.put(colunas[2], (new BigDecimal(colunas[12])).divide(BigDecimal.valueOf(100),2, RoundingMode.HALF_UP));
                
                String newLine = String.join("|", colunas);
                newText.append("\r\n").append(newLine);
            } else if (fileTextLine.startsWith("|H010|")) {
                String[] colunas = fileTextLine.split("\\|", -1);
                
                //Adiciona poque não vai precisar alterar
                newText.append("\r\n").append(fileTextLine);
                
                //Se não tiver o 020 cria o 020
                if(!fileTextLines[i+1].startsWith("|H020|")){
                    //|H020|060|18,29|3,29|
                    
                    BigDecimal value = new BigDecimal(colunas[5].replaceAll(",", "."));
                    BigDecimal aliq = aliqs.getOrDefault(colunas[2], BigDecimal.valueOf(0.18)).multiply(value).setScale(2,RoundingMode.HALF_UP);
                    
                    value = value.setScale(2, RoundingMode.HALF_UP);
                    
                    newText.append("\r\n").append("|H020|060|");
                    newText.append(value.toPlainString().replaceAll("\\.", ","));//Valor
                    newText.append("|");
                    newText.append(aliq.toPlainString().replaceAll("\\.", ","));//Valor X Aliq
                    newText.append("|");
                }
                
                
            } else {
                newText.append("\r\n").append(fileTextLine);
            }
        }

        //salva novo arquivo
        FileManager.save(file.getParentFile(), "CORRIGIDO - " + file.getName(), newText.toString());
    }

}
