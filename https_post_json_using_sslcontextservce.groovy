//headers
import org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets
import java.net.ssl.*
import java.io.*
import java.net.URL
import org.apache.nifi.ssl.SSLContextService
import org.apache.nifi.ssl.SSLContextService.ClientAuth
import org.apache.nifi.controller.ControllerService


//get flowfile of previous processor
def flowFile = session.get()
if(!flowFile) return

//init dynamic parameters of executescript processor
def url = httpsUrl.value
def jsonMessage = jsonMessage.value //https request json message
def serviceName = sslContextService.value //name of the controller ssl context service 

try {
  flowFile = session.write(flowFile,{inputStream,outputStream -> 
    //get ssl context controller service
    def lookup = context.controllerServiceLookup 
    def serviceId = lookup.getControllerServiceIdentifiers(ControllerService).find {
      cs -> lookup.getControllerServiceName(cs) == serviceName
    }
    def sslContextService = lookup.getControllerService(servcieId)

    //init ssl context using ssl context controller service
    def sslContext = sslContextService.createSSlContext(ClientAuth.NONE) //passing ClientAuth.REQUIRED as paramter also works 

    //init java https connection object
    def conn = (HttpsURLConnection) new URL(url).openConnection()

    //disable hostname verification (optional)
    conn.setHostnameVerifier(new HostnameVerifier() {
      @override 
      public boolean verify(STring s,SSLSession sslSesssion) {
        return true;
      }})

    //set ssl socketfactory 
    conn.setSSLSocketFactory(sslContext.getSocketFactory())

    //write json message to https request body 
    conn.setRequestMethod("POST")
    conn.setDoOutput(true)
    conn.setRequestProperty("Content-Type","application/json")
    conn.getOutputStream().write(jsonMessage.getBytes("UTF-8"))

    //check and process response from https server 
    def responseCode = conn.getResponseCode()
    def responseBody = null
    if(responseCode == 200 || responseCode == 202) //success 
    {
      resposneBody = IOUtils.toString(conn.getOutputStream(),StandardCharsets.UTF_8)
    }
    else
    {
      responseBody = IOUtils.toString(conn.getErrorStream(),StandardCharsets.UTF_8)
    }

    //write https response body to next processor via flowfile
    outputStream.Write(responseBody.getBytes(StandardCharsets.UTF_8))
    
  } as StreamCallback
  //redirect to success flow 
  session.transfer(flowFile,REL_SUCCESS)
                           
}
catch(e) {
  //store error message in attributes 
  flowFile = session.putAttribute(flowFile,"script_error_message",e.getMessage())
  flowFile = session.putAttribute(flowFile,"script_error",e.toString())
  
  //redirect to failure flow
  session.transfer(flowFile,REL_FAILURE)
}

