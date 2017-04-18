package com.vsf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import com.vsf.S15.Termination.FeeComplexType;
import com.vsf.S15.Termination.TerminationComplexType;
import com.vsf.S15.Termination.Terminations;
import com.vsf.S15.Termination.*;

public class Utiles {


	public static boolean PrimeraLinea (int NumLinea){
		if (NumLinea==1){
			return true;
		}else {
			return false;
		}
	}


	// Devuelve la fecha de text como tipo XMLGregorianCalendar
	public static XMLGregorianCalendar FechaXML(String FechaTxt) throws DatatypeConfigurationException{
		DateFormat dateFormat = new SimpleDateFormat("yyyymmdd");
		Date Fecha = null ;
		// Temporal
		//if (FechaTxt.equals("")){
		//	FechaTxt="17540303";
		//}
		//
		try {
			Fecha=dateFormat.parse(FechaTxt);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(Fecha);
		XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		return date2;
	}

	public static BigDecimal DecimalXML(String ImporteTxt){
		// Temporal
		//	if (ImporteTxt.equals("")){
		//		ImporteTxt="0";
		//	}
		//
		BigDecimal Importe = new BigDecimal(ImporteTxt);
		return Importe;
	}

	public static BigInteger IntXML(String ImporteTxt){
		BigInteger Valor = new BigInteger(ImporteTxt);
		return Valor;
	}

	// Comprueba si es un servicio o un dispositivo
	public static boolean EsServicio(String Contenido){
		String auxContenido=Contenido.trim();
		if (auxContenido.equals("S")){
			return true;
		} else{
			return false;
		}
	}
	// Comprueba si es un servicio o un dispositivo
	public static boolean EsDispositivo(String Contenido){
		String auxContenido=Contenido.trim();
		if (auxContenido.equals("D")){
			return true;
		} else{
			return false;
		}
	}


	public static void GeneraTerminations ( Terminations s15Terminations, String ficheroSalidaXML, int numfichero) throws JAXBException{

		String sufijofileXML=String.valueOf(numfichero);
		String XMLFile=ficheroSalidaXML+"_"+sufijofileXML+".xml";
		File ficheroXML= new File(XMLFile);
		JAXBContext contexto = JAXBContext.newInstance(s15Terminations.getClass() );
		Marshaller marshaller = contexto.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
				Boolean.TRUE);
		marshaller.marshal(s15Terminations,ficheroXML);	
	}

	public static boolean CambioContrato(String ContractA, String ContractB){
		if (ContractA.trim().equals(ContractB.trim()))
		{
			return true;
		} else {
			return false;
		}
	}


	public static void addTerminationContractAtrib(TerminationComplexType Contrato, 
			String f_eventType, String f_eventDate, String f_eventContractID, String f_contractEndDate,
			String f_terminationReason, String f_companyCode) throws DatatypeConfigurationException{

		Contrato.setEventType(f_eventType);
		Contrato.setEventDate(FechaXML(f_eventDate));
		Contrato.setEventContractID(f_eventContractID);
		Contrato.setContractEndDate(FechaXML(f_contractEndDate));
		Contrato.setTerminationReason(f_terminationReason);
		Contrato.setCompanyCode(f_companyCode);
	}


	public static void addFeeAtrib(FeeComplexType Fee, 
			String f_FEE_id, String f_FEE_type, String f_FEE_amount, 
			String f_FEE_profitCenter, String f_FEE_referenceAccount, 
			String f_FEE_POBname, String f_FEE_currency) throws DatatypeConfigurationException{

		BigDecimal Importe = new BigDecimal(f_FEE_amount);

		Fee.setFeeID(f_FEE_id);
		Fee.setFeeType(f_FEE_type);
		Fee.setFeeAmount(Importe);
		Fee.setProfitCenter(f_FEE_profitCenter);
		Fee.setReferenceAccount(f_FEE_referenceAccount);
		Fee.setPOBName(f_FEE_POBname);
		Fee.setCurrency(CurrencyType.fromValue(f_FEE_currency));

	}

	public static void EscribeHoraFileControl(String ficheroSalidaXML, long TiempoInicial){
		BufferedWriter out = null;
		try {
			//	out=new BufferedWriter(new FileWriter(Ruta+"Control_"+String.valueOf(Sufijo)+".txt", true));
			out=new BufferedWriter(new FileWriter(ficheroSalidaXML+"Control_Ejecucion.txt", true));
			out.write("#### Comienzo: " +String.valueOf(TiempoInicial)+"\r\n");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (out !=null){
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	public static long RestaFechas (long TiempoInicial, long TiempoFinal ){
		long Resta;
		Resta=TiempoFinal-TiempoInicial;
		//	return Resta/(1000);
		return Resta;
	}

	public static String EstimaTimepoRestante(long TotalRegistros, int IncLineas, long msecsInc, int TotalLineas, long msecsTotal){
		String Texto="";
		if ((msecsInc!=0 )&&(IncLineas!=0)&&(msecsTotal!=0)&&(TotalLineas!=0)&&(TotalRegistros!=0))
		{
			//		long Restante1=(TotalRegistros/IncLineas/msecsInc);
			if (msecsInc<0.0000001){
				msecsInc=(long) 0.0000001;
			}
			double mseclinea=IncLineas/msecsInc;
			double Totalsecs=TotalRegistros/mseclinea/1000;
			int Totalmins=(int) (Totalsecs/60);
			int TotalHoras=Totalmins/60;

			//		 Texto="     ->Estim. Final: "+String.valueOf(Totalmins)+ " mins ("+String.valueOf(TotalHoras)+" horas)";
			Texto="     ->Estim. Final: "+String.valueOf(Totalmins);

		}
		return Texto;

	}

	public static void AppendFicheroControl (int Fichero, String Ruta, int Sufijo, int Lineas,int Contratos, int Pobs,
			int IncLineas, int IncContratos, int IncPobs, long Tiempo, long msecTranscurridos,
			String Estimado){

		BufferedWriter out = null;
		try {
			//	out=new BufferedWriter(new FileWriter(Ruta+"Control_"+String.valueOf(Sufijo)+".txt", true));
			out=new BufferedWriter(new FileWriter(Ruta+"Control_Ejecucion.txt", true));
			out.write("File: " +String.valueOf(Fichero));
			out.write(" -Lin: " + String.valueOf(Lineas) +"(+"+String.valueOf(IncLineas)+") ");
			out.write(" -Cont: " +String.valueOf(Contratos) +"(+"+String.valueOf(IncContratos)+")");
			out.write(" -Pobs: " +String.valueOf(Pobs) +"(+"+String.valueOf(IncPobs)+") ");
			out.write(" # (msecs): " +String.valueOf(Tiempo) );
			out.write(" # Transcurrido: " +String.valueOf(msecTranscurridos)+" msecs - "+String.valueOf(msecTranscurridos/(1000*60))+" mins" );
			out.write(Estimado +"\r\n" );


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (out !=null){
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}


	}

	//	public static void addDeviceAtrib(DeviceComplexType Device, String s_CD_POB, String s_ID_Unico, String s_StartDate, String s_EndDate) throws DatatypeConfigurationException{

	//		Device.setDeviceCode(s_CD_POB);
	//		Device.setDeviceID(s_ID_Unico);
	//		Device.setDeviceTransferDate(FechaXML(s_StartDate));



	//}














}
