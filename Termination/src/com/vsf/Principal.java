package com.vsf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
//import java.util.Vector;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import com.vsf.S15.Termination.*;
import com.vsf.*;

public class Principal {

	static int NumLinea=0;
	// Indices de los campos al deconstruir la linea del fichero
	// Para el evento/Contrato
	static final int f_eventType			=0;
	static final int f_eventDate			=1;
	static final int f_eventContractID		=2;
	static final int f_contractEndDate		=3;	
	static final int f_terminationReason	=4;
	static final int f_companyCode			=5;
	static final int f_FEE_ID_Unico			=6;
	static final int f_FEE_type				=7;
	static final int f_FEE_amount			=8; 
	static final int f_FEE_profitCenter		=9; 
	static final int f_FEE_referenceAccount	=10;
	static final int f_FEE_POBname			=11;
	static final int f_FEE_currency			=12;

	static String ID_Fee="";
	static String POB_Code="";

	// Para Windows
	static final String ficheroSalidaXML="C:\\Users\\cesar.fuentes\\workspace\\Termination\\Termination";
	static final String ficheroSalidaControl="C:\\Users\\cesar.fuentes\\workspace\\Termination\\";

	// PAra unix
	//	static final String ficheroSalidaControl="";
	//	static final String ficheroSalidaXML="";

	static int NumFichero =0;
	static int NumContratos=0;
	static int NumContratosXfichero=2;
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

	public static void main(String[] args) throws IOException, DatatypeConfigurationException, JAXBException, InstantiationException, IllegalAccessException  {
		// TODO Auto-generated method stub
		String Fichero=args[0];
		String linea;
		String[]Campos;
		// Inicializamos objetos
		Terminations S15Termination = new Terminations();
		TerminationsComplexType lstTerminations = new TerminationsComplexType();
		TerminationComplexType tdTermination = new TerminationComplexType();
		FeeListComplexType vListaFee = new FeeListComplexType();
		FeeComplexType Fee = new FeeComplexType();
		// Variable para guardar y detectar el cambio de contrato
		String aContratcID="";
		System.out.println("Comenzando generacion XML...");
		NumLinea=1;
		TiempoInicial=fechaInicio.getTime();
		AntTiempo=TiempoInicial;
		// Lectura del Fichero
		try {
			BufferedReader reader =	new BufferedReader(new	FileReader(Fichero));
			while((linea = reader.readLine())!=null) {
				Campos = linea.split(";"); // Deconstruyo el registro en campos
				// Si cambia el contrato, generamos nuevo grupo
				if (!aContratcID.equals(Campos[f_eventContractID].trim())){
					NumContratos ++;
					// Comprueba cuantos contratos se van a mandar en cada fichero. Controla que sea cuando finaliza el evento.
					if (NumContratosXfichero<NumContratos)
					{
						AntNumContratos=CtrlContratos;
						CtrlContratos=AntNumContratos+NumContratos;
						Utiles.GeneraTerminations(S15Termination, ficheroSalidaXML, NumFichero); // Comentado en pruebas
						NumFichero ++; // Incrementamos para saber el numero de ficheros generados
						NumContratos=1;
						// Escribe el fichero de control de exportacion
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
						lstTerminations = new TerminationsComplexType();
					}
					tdTermination = new TerminationComplexType();
					lstTerminations.getTermination().add(tdTermination);
					S15Termination.setData(lstTerminations);
					vListaFee = new FeeListComplexType();
					tdTermination.setFeeList(vListaFee);
					Utiles.addTerminationContractAtrib(tdTermination, 
							Campos[f_eventType], 
							Campos[f_eventDate], 
							Campos[f_eventContractID], 
							Campos[f_contractEndDate], 
							Campos[f_terminationReason], 
							Campos[f_companyCode]);
				}
				aContratcID=Campos[f_eventContractID].trim();
				if (0==0){ //Para en debug no ejecutar este cacho
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
				NumLinea ++;

				///////////////////////////
				/// FIN LECTURA FICHERO //
				///////////////////////////
			} // Fin lectura lineas fichero
			reader.close();
			//NumFichero ++;
			// Cuando acaba con el fichero imprime el resto.
			if (NumContratosXfichero>=NumContratos ){
				Utiles.GeneraTerminations(S15Termination, ficheroSalidaXML, NumFichero); // Comentado en pruebas
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
			e.printStackTrace();
		}
		TiempoFinal=fechaFin.getTime();
		//		Utiles.EscribeHoraFileControl(Fichero, TiempoFinal);
		System.out.println("Finalizada extraccion !!!!");	
	}
}