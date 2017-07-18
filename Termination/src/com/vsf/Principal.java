package com.vsf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

//import java.util.Vector;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import com.vsf.S15.Termination.*;
import com.vsf.*;

public class Principal {


	// Indices de los campos al deconstruir la linea del fichero
	// Para el evento/Contrato
	static final int f_consumerType			=0; 
	static final int f_eventType			=1; 
	static final int f_eventDate			=2; 
	static final int f_eventContractID		=3; 
	static final int f_contractEndDate		=4; 
	static final int f_terminationReason	=5; 
	static final int f_companyCode			=6; 
	static final int f_FEE_ID_Unico			=7; 
	static final int f_FEE_type				=8; 
	static final int f_FEE_amount			=9; 
	static final int f_FEE_profitCenter		=10;
	static final int f_FEE_referenceAccount	=11;
	static final int f_FEE_POBname			=12;
	static final int f_FEE_currency			=13;

	// Variables de control de flujo
	static String ID_Fee="";
	static String POB_Code="";

	static int NumLinea=0;
	static int NumFichero =0;
	static int NumContratos=0;
	static int NumContratosXfichero=1;
	static long NumSeqInit=0;
	
	static String isConsumer="";
	static String isReplace="";
	
	// Variables fichero de control
	static final String ficheroSalidaControl="";
	static int CtrlNumFees=0;
	static int CtrlContratos=0;
	static int AntCtrlNumFees=0;
	static int AntCNumLinea=0;
	static int AntNumContratos=0;
	static long TiempoInicial;
	static long TiempoFinal;
	static Date fechaInicio=new Date();
	static Date fechaFin=new Date();
	static long AntTiempo;
	static long TiempoActual;
	static Date AntFecha=new Date();
	static Date FechaActual=new Date();
	static long TiempoTranscurrido;
	static long TotalRegistros=58000000;
	
	// Variables de formato de ficheros de salida
	static String CountryCode="es";
	static String EventType="termination";
	static String contractEventTypeBase="Termination";
	static String contractEventType="";
	static String consumerType="";
	static String SeqIDName="";
	static DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	static Date currentDate = new Date();
	static DateFormat timeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	static Calendar cal = Calendar.getInstance();
	static String SourceEvent="CON_Termination";
	static String SourceOpCo="ES";

	// Fichero en el que se escribe la actualización del número de secuencia
	static String SeqSqlFile = "TWU15Y12.sql";

	public static void main(String[] args) throws IOException, DatatypeConfigurationException, JAXBException, InstantiationException, IllegalAccessException  {
		// TODO Auto-generated method stub
		
		// Obtenemos los parámetros: fichero csv, ruta de salida, fichero de log, fichero con los números de secuencia, ruta donde dejar el sql de secuencia
		String Fichero=args[0];
		String OutPath=args[1];
		OutPath=OutPath+"/";
		String log_file=args[2];
		String seq_file=args[3];
		String sqldir=args[4];
		
		// Obtención de los números de secuencia actuales
		SeqSqlFile = sqldir+"/TWU15Y12.sql";
		HashMap<String, Integer> SeqIDs = Utiles.getSeqID(seq_file);
		HashMap<String, Integer> NewSeqIDs = Utiles.getSeqID(seq_file);
		NumContratosXfichero=SeqIDs.get("Contratos_X_Fichero");
		
		// Inicializamos objetos
		String linea;
		String[]Campos;
		Terminations S15Termination = new Terminations();
		FileHeaderComplexType fileHeader = new FileHeaderComplexType();
		FileFooterComplexType fileFooter = new FileFooterComplexType();
		TermiantionsComplexType lstTerminations = new TermiantionsComplexType();
		TerminationComplexType tdTermination = new TerminationComplexType();
		FeeListComplexType vListaFee = new FeeListComplexType();
		FeeComplexType Fee = new FeeComplexType();
		
		// Variable para guardar y detectar el cambio de contrato
		String aContratcID="";
		System.out.println("Comenzando generacion XML...");
		
		NumLinea=1;
		
		TiempoInicial=fechaInicio.getTime();
		AntTiempo=TiempoInicial;
		//File FicheroControl=new File ("Fichero.txt");
		//Utiles.EscribeHoraFileControl(Fichero, TiempoInicial);
		
		// Lectura del Fichero
		try {
			BufferedReader reader =	new BufferedReader(new	FileReader(Fichero));
			while((linea = reader.readLine())!=null) {
				Campos = linea.split(";", -1); 
				if (!aContratcID.equals(Campos[f_eventContractID].trim())){
					NumContratos ++;
					
					// Si se supera el número de contratos máximo por fichero o cambia el tipo de consumer se genera un nuevo fichero
					if (NumContratosXfichero<NumContratos || (!isConsumer.equals(Campos[f_consumerType].trim()) && NumLinea>1))
					{
						AntNumContratos=CtrlContratos;
						CtrlContratos=AntNumContratos+NumContratos;
						NumFichero ++;
						// Se añaden los atributos de la cabecera
						Utiles.AddHeaderAttrib(fileHeader, consumerType, CountryCode, EventType, currentDate, cal, NumSeqInit+NumFichero, SourceEvent, SourceOpCo);
						// Se actualizan los números de secuencia
						NewSeqIDs.put(SeqIDName, SeqIDs.get(SeqIDName)+NumFichero);
						// Se establece el número de registros del fichero en el footer
						fileFooter.setNumberRecords(BigInteger.valueOf(NumContratos-1));
						// Llamada a la generación del fichero xml
						Utiles.GeneraTerminations(S15Termination, OutPath+fileHeader.getFileName(), log_file);
						NumContratos=1;
						TiempoActual=new Date().getTime();
						long Tiempo=Utiles.RestaFechas(AntTiempo, TiempoActual);
						long msecTranscurridos=Utiles.RestaFechas(TiempoInicial, TiempoActual);
						TotalRegistros=TotalRegistros-NumLinea;
						String Estimado1=Utiles.EstimaTimepoRestante(TotalRegistros,NumLinea-AntCNumLinea,Tiempo,
								NumLinea,msecTranscurridos );
						Utiles.AppendFicheroControl(NumFichero, ficheroSalidaControl, NumFichero, 
								NumLinea, CtrlContratos, CtrlNumFees,
								NumLinea-AntCNumLinea,CtrlContratos-AntNumContratos,CtrlNumFees-AntCtrlNumFees,Tiempo,msecTranscurridos,Estimado1);
						AntCtrlNumFees=CtrlNumFees;
						AntCNumLinea=NumLinea;
						AntTiempo=TiempoActual;
						S15Termination = new Terminations();
						lstTerminations = new TermiantionsComplexType();
						fileHeader = new FileHeaderComplexType();
						fileFooter = new FileFooterComplexType();
					}
					
					// Se establecen las variables de nombrado de ficheros en función del consumer type
					if (Campos[f_consumerType].equals("1")) {
						contractEventType="CON_"+contractEventTypeBase;
						SourceEvent=contractEventType;
						SeqIDName=contractEventType;
						consumerType="consumer";
					} else {
						contractEventType="ENT_"+contractEventTypeBase;
						SourceEvent=contractEventType;
						SeqIDName=contractEventType;
						consumerType="enterprise";
					}
					// Obtención del número de secuencia inicial
					NumSeqInit=SeqIDs.get(SeqIDName);
					// Si cambia el tipo de consumer se establece NumFichero a 0 para empezar desde el número de secuencia correspondiente
					if ((!isConsumer.equals(Campos[f_consumerType].trim()) && NumLinea>1)) // || (!isReplace.equals(Campos[f_replaceType].trim()) && NumLinea>1))
					{
						NumFichero=0;
					}
					
					// Creacción del objeto Update y carga de atributos de contrato
					tdTermination = new TerminationComplexType();
					lstTerminations.getTermination().add(tdTermination);
					S15Termination.setData(lstTerminations);
					S15Termination.setHeader(fileHeader);
					S15Termination.setFooter(fileFooter);
					vListaFee = new FeeListComplexType();
					Utiles.addTerminationContractAtrib(tdTermination, 
							contractEventType, 
							Campos[f_eventDate], 
							Campos[f_eventContractID], 
							Campos[f_contractEndDate], 
							Campos[f_terminationReason], 
							Campos[f_companyCode]);
				}
				
				/// PARTE DE POBS 
				// Fees
				if (Campos[f_FEE_ID_Unico].length()!=0){ 
					tdTermination.setFeeList(vListaFee);
					Fee = new FeeComplexType();
					Utiles.addFeeAtrib(Fee, 
							Campos[f_FEE_ID_Unico], 
							Campos[f_FEE_type], 
							Campos[f_FEE_amount], 
							Campos[f_FEE_profitCenter], 
							Campos[f_FEE_referenceAccount], 
							Campos[f_FEE_POBname], 
							Campos[f_FEE_currency]);
					vListaFee.getFee().add(Fee);
					CtrlNumFees ++;
					ID_Fee=Campos[f_FEE_ID_Unico];
				}	
				
				aContratcID=Campos[f_eventContractID].trim();
				isConsumer=Campos[f_consumerType];
				NumLinea ++;

			} // Fin lectura lineas fichero
			
			reader.close();
			// Cuando acaba con el fichero imprime el resto.
			if (NumContratosXfichero>=NumContratos && NumLinea>1){
				NumFichero ++;
				Utiles.AddHeaderAttrib(fileHeader, consumerType, CountryCode, EventType, currentDate, cal, NumSeqInit+NumFichero, SourceEvent, SourceOpCo);
				NewSeqIDs.put(SeqIDName, SeqIDs.get(SeqIDName)+NumFichero);
				fileFooter.setNumberRecords(BigInteger.valueOf(NumContratos));
				Utiles.GeneraTerminations(S15Termination, OutPath+fileHeader.getFileName(), log_file); // Comentado en pruebas
				// Escribe el fichero de control de exportacion
				AntNumContratos=CtrlContratos;
				CtrlContratos=AntNumContratos+NumContratos;
				TiempoActual=new Date().getTime();
				long Tiempo=Utiles.RestaFechas(AntTiempo, TiempoActual);
				long msecTranscurridos=Utiles.RestaFechas(TiempoInicial, TiempoActual);
				TotalRegistros=TotalRegistros-NumLinea;
				String Estimado1=Utiles.EstimaTimepoRestante(TotalRegistros,NumLinea-AntCNumLinea,Tiempo,
						NumLinea,msecTranscurridos );
				Utiles.AppendFicheroControl(NumFichero, ficheroSalidaControl, NumFichero, 
						NumLinea, CtrlContratos, CtrlNumFees,
						NumLinea-AntCNumLinea,CtrlContratos-AntNumContratos,CtrlNumFees-AntCtrlNumFees,Tiempo,msecTranscurridos,Estimado1);
				AntCtrlNumFees=CtrlNumFees;
				AntCNumLinea=NumLinea;
				AntNumContratos=NumContratos;
				AntTiempo=TiempoActual;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("File "+Fichero+" not found.");
			//e.printStackTrace();		
		}
		
		// Se escribe el update en el sql de los números de secuencia
		Utiles.updateSeqID(SeqSqlFile, SeqIDs, NewSeqIDs);
		
		TiempoFinal=fechaFin.getTime();
		//		Utiles.EscribeHoraFileControl(Fichero, TiempoFinal);
		System.out.println("Finalizada extraccion !!!!");	
	}
}